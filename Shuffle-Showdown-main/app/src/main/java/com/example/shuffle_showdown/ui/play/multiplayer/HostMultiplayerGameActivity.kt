package com.example.shuffle_showdown.ui.play.multiplayer

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.example.shuffle_showdown.Animations
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.cueCard.CueCard
import com.example.shuffle_showdown.cueCard.CueCardRepository
import com.example.shuffle_showdown.databinding.ActivityHostMultiplayerGameBinding
import com.example.shuffle_showdown.messages.MessagesViewModel
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

// This is the host. The host is responsible for sending the questions/answers to player 2
// and for receiving messages from the other player to calculate who gets the point

@AndroidEntryPoint
class HostMultiplayerGameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHostMultiplayerGameBinding

    @Inject
    lateinit var messagesViewModel: MessagesViewModel

    @Inject
    lateinit var cueCardRepository: CueCardRepository

    private lateinit var rounds: String

    private lateinit var activeCards: Queue<CueCard>
    private lateinit var currentCard: CueCard
    private var remainingCards = 0
    private var currentScore = 0
    private var otherPlayerScore = 0
    private var readyToCalculatePoint = false
    private var initialTime: Long = 0
    private var totalTimeTaken: Long = 0
    private var countDownTimer : CountDownTimer? = null

    private var hostWasCorrect = false
    private var otherPlayerResult: Boolean = false
    private var otherPlayerTime: Long = 0

    // Flag that keeps track of needing to disconnect for onDestroy(). Not needed if moving on
    // to game over acitivty
    private var needToDisconnect = false
    private var firstFlip = true

    private val observer: Observer<String?> = Observer { it ->
        if (it != null) {
            val msg = it.split("-")
            val token = msg[0]

            if (token == "answer") {
                otherPlayerResult = msg[1].toBoolean()
                otherPlayerTime = msg[2].toLong()

                // if host already submitted their answer, calculate who gets the point
                readyToCalculatePoint = if (readyToCalculatePoint) {
                    calculateWhoGetsThePoint(hostWasCorrect, otherPlayerResult, otherPlayerTime)
                    false
                } else {
                    true
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHostMultiplayerGameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        rounds = intent.getStringExtra("rounds") ?: ""
        activeCards = LinkedList()
        loadCards()

        binding.hostBtnAnswer.setOnClickListener {
            // get time took to answer
            totalTimeTaken = System.currentTimeMillis() - initialTime

            // indicate to user that they have submitted
            binding.hostBtnAnswer.isEnabled = false
            binding.hostWaitingMsg.visibility = View.VISIBLE
            binding.hostAnswerInput.isClickable = false
            binding.hostAnswerInput.isFocusable = false

            hostWasCorrect = checkIfCorrect()
            binding.hostAnswerInput.text?.clear()

            // if other player already sent their results, calculate who gets the point
            readyToCalculatePoint = if (readyToCalculatePoint) {
                calculateWhoGetsThePoint(hostWasCorrect, otherPlayerResult, otherPlayerTime)
                false
            } else {
                true
            }
        }

        // disable answer button if no text on editText
        binding.hostAnswerInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // enable submit if not empty, if empty disable button
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.hostBtnAnswer.isEnabled = s?.isEmpty() != true
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        // observe messages
        messagesViewModel.messagesLiveData.observe(this, observer)

        // observe disconnects
        messagesViewModel.connected.observe(this) {
            if (!it) {
                val intent = Intent(this, HostLobbyActivity::class.java)
                needToDisconnect = true
                startActivity(intent)
                finish()
            }
        }

        // create callback for back button to go back to the properly disconnect
        // source: https://www.droidcon.com/2023/02/22/handling-back-press-in-android-13-the-correct-way/
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                needToDisconnect = true
                finish()
            }
        })

        // start time to answer question as soon as view is created
        initialTime = System.currentTimeMillis()

        // start overall timer for the game
        startTimer()

    }

    // *NOTE: onDestroy() is not guaranteed to be called if the app is force closed, therefore
    // sometimes disconnection is reliant on timeout (DEFAULT_TIMEOUT IN MessagesViewModel sec).
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()

        if (needToDisconnect) {
            messagesViewModel.disconnect()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                needToDisconnect = true
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // if time specified, start timer
    private fun startTimer() {
        val timeAsString = intent.getStringExtra("time") ?: "0"
        if (timeAsString != "Unlimited Time") {

            // convert to milliseconds
            val timeAsMs: Long = timeAsString[0].digitToInt() * 60L * 1000L
            countDownTimer = object : CountDownTimer(timeAsMs, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    binding.hostTime.text =
                        (millisUntilFinished / 1000).toString() + " Seconds Remaining"
                }

                override fun onFinish() {
                    endGame(currentScore, otherPlayerScore)
                }
            }.start()
        }
    }

    // Load the deck and load the next question. Sends needed info to player 2
    private fun loadCards() {
        val deckId = intent.getStringExtra("deckId").toString()

        // SOURCE: Coroutine demo and CHATGPT for async calling and transferring to main thread
        CoroutineScope(Dispatchers.IO).launch {
            val cueCards = cueCardRepository.getCueCards(deckId)

            remainingCards = cueCards.size
            withContext(Dispatchers.Main) {
                activeCards.addAll(cueCards)

                // set the amount of cards equal to the number of rounds specified
                remainingCards = if (rounds == "Entire Deck") {
                    activeCards.size

                } else {
                    val numOfRounds = rounds[0].digitToInt()
                    numOfRounds
                }

                // send the number of rounds to player 2
                messagesViewModel.sendMessage("rounds-$remainingCards\n", 100)

                loadNextQuestion()
                sendQuestion()
            }
        }
    }

    // Load the next question, sets the text
    private fun loadNextQuestion() {
        if (activeCards.isNullOrEmpty()) {
            return
        }

        binding.hostRoundsRemainingText.text = "$remainingCards Rounds Remaining"

        binding.hostBtnAnswer.isEnabled = false
        binding.hostWaitingMsg.visibility = View.INVISIBLE
        binding.hostAnswerInput.isClickable = true
        binding.hostAnswerInput.isFocusable = true


        remainingCards--
        currentCard = activeCards.poll()

        if (currentCard.isMultipleChoice == true) {
            binding.hostAnswerInput.isClickable = true
            binding.hostAnswerInput.isFocusable = false
            binding.hostAnswerInput.hint = "Tap here to answer"
            binding.hostAnswerInput.setOnClickListener {
                openMultipleChoiceDialog()
            }
        } else {
            binding.hostAnswerInput.isClickable = true
            binding.hostAnswerInput.isFocusable = true
            binding.hostAnswerInput.hint = "What is the answer?"
            binding.hostAnswerInput.isFocusableInTouchMode = true
            binding.hostAnswerInput.setOnClickListener(null)
        }

        if (firstFlip) {
            firstFlip = false
        } else {
            var cardContainer: RelativeLayout = findViewById(R.id.cardContainer)
            var animator = Animations(this)
            animator.flipCard(cardContainer)
        }

        //set the text of the next question
        binding.hostQuestionText.text = currentCard.term
    }

    private fun openMultipleChoiceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_multiple_choice, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        val options = ArrayList<String?>();
        options.add(currentCard.fakeAnswer1)
        options.add(currentCard.fakeAnswer2)
        options.add(currentCard.fakeAnswer3)
        options.add(currentCard.answer)
        options.shuffle()

        val optionACard = dialogView.findViewById<MaterialCardView>(R.id.optionACard)
        val optionATextView = dialogView.findViewById<TextView>(R.id.optionAText)
        optionATextView.text = options[0]
        optionACard.setOnClickListener {
            binding.hostAnswerInput.setText(optionATextView.text)
            dialog.dismiss()
        }

        val optionBCard = dialogView.findViewById<MaterialCardView>(R.id.optionBCard)
        val optionBTextView = dialogView.findViewById<TextView>(R.id.optionBText)
        optionBTextView.text = options[1]
        optionBCard.setOnClickListener {
            binding.hostAnswerInput.setText(optionBTextView.text)

            dialog.dismiss()
        }

        val optionCCard = dialogView.findViewById<MaterialCardView>(R.id.optionCCard)
        val optionCTextView = dialogView.findViewById<TextView>(R.id.optionCText)
        optionCTextView.text = options[2]
        optionCCard.setOnClickListener {
            binding.hostAnswerInput.setText(optionCTextView.text)

            dialog.dismiss()
        }

        val optionDCard = dialogView.findViewById<MaterialCardView>(R.id.optionDCard)
        val optionDTextView = dialogView.findViewById<TextView>(R.id.optionDText)
        optionDTextView.text = options[3]
        optionDCard.setOnClickListener {
            binding.hostAnswerInput.setText(optionDTextView.text)
            dialog.dismiss()
        }
        dialog.show()
    }

    // Checks if the answer is correct (not that they get the point)
    private fun checkIfCorrect(): Boolean {

        // get the input of user as a string
        var userInput = binding.hostAnswerInput.text.toString()

        // check if the users response matches the cards answer (not case sensitive)
        return if (userInput.equals(currentCard.answer, ignoreCase = true)) {

            Toast.makeText(this, "CORRECT!", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(this, "INCORRECT!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun sendQuestion() {
        if (currentCard.isMultipleChoice == true) {
            messagesViewModel.sendMessage(
                "question-" + currentCard.term + "-" + currentCard.answer + "-" +
                        currentCard.isMultipleChoice + "-" + currentCard.fakeAnswer1 + "-" +
                        currentCard.fakeAnswer2 + "-" + currentCard.fakeAnswer3 + "\n",
                300
            )

        } else {
            messagesViewModel.sendMessage(
                "question-" + currentCard.term + "-" + currentCard.answer + "-" + currentCard.isMultipleChoice + "\n",
                300
            )
        }
    }

    // Calculates between host and player 2 who got the point (who has the correct answer +
    // was the quickest)
    private fun calculateWhoGetsThePoint(
        hostWasCorrect: Boolean,
        otherResult: Boolean,
        otherTime: Long
    ) {
        var whoGotThePoint: String

        // both players are correct, update score based on who's quicker
        if (hostWasCorrect && otherResult) {
            if (totalTimeTaken < otherTime) {
                currentScore++
                binding.hostScoreText.text = "You: $currentScore"
                whoGotThePoint = "host"
                Toast.makeText(this, "You answered correctly faster!", Toast.LENGTH_SHORT).show()
            } else {
                otherPlayerScore++
                binding.hostPlayer2ScoreText.text = "Player 2: $otherPlayerScore"
                whoGotThePoint = "player2"
                Toast.makeText(this, "Player2 answered correctly faster!", Toast.LENGTH_SHORT)
                    .show()
            }

            // only host was quicker
        } else if (hostWasCorrect) {
            currentScore++
            binding.hostScoreText.text = "You: $currentScore"
            whoGotThePoint = "host"
            Toast.makeText(this, "You answered correctly faster!", Toast.LENGTH_SHORT).show()

            // only player 2 was quicker
        } else if (otherResult) {
            otherPlayerScore++
            binding.hostPlayer2ScoreText.text = "Player 2: $otherPlayerScore"
            whoGotThePoint = "player2"
            Toast.makeText(this, "Player2 answered correctly faster!", Toast.LENGTH_SHORT).show()

            // both were wrong
        } else {
            whoGotThePoint = "nobody"
            Toast.makeText(this, "No one got it right!", Toast.LENGTH_SHORT).show()
        }

        messagesViewModel.sendMessage("scores-$currentScore-$otherPlayerScore\n")

        // termination point
        if (activeCards.isNullOrEmpty() || remainingCards <= 0) {
            endGame(currentScore, otherPlayerScore)

        } else {
            loadNextQuestion()
            sendQuestion()
            messagesViewModel.sendMessage(
                "score-$currentScore-$otherPlayerScore-$whoGotThePoint\n",
                200
            )
        }
    }

    private fun endGame(finalPlayerScore: Int, otherScore: Int) {
        messagesViewModel.sendMessage("gameOver-$currentScore-$otherPlayerScore\n")

        val deckName = intent.getStringExtra("deckName") ?: "null"

        val intent = Intent(this, MultiplayerGameOverActivity::class.java)
        intent.putExtra("hostScore", finalPlayerScore)
        intent.putExtra("player2Score", otherScore)
        intent.putExtra("guestOrHost", "host")
        intent.putExtra("deckName", deckName)

        // needed so that when this activity relaunches, observer doesn't immediately trigger
        // with last answer that was sent
        messagesViewModel.messagesLiveData.value = null
        messagesViewModel.messagesLiveData.removeObserver(observer)

        startActivity(intent)
        finish()
    }
}