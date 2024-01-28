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
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.example.shuffle_showdown.Animations
import com.example.shuffle_showdown.MainActivity
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.databinding.ActivityGuestMultiplayerGameBinding
import com.example.shuffle_showdown.databinding.ActivityHostMultiplayerGameBinding
import com.example.shuffle_showdown.messages.MessagesViewModel
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import java.sql.Array
import javax.inject.Inject

@AndroidEntryPoint
class GuestMultiplayerGameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuestMultiplayerGameBinding

    @Inject
    lateinit var messagesViewModel: MessagesViewModel

    private var currentAnswer: String = "placeholder"

    private var initialTime: Long = 0
    private var totalTimeTaken: Long = 0
    private var remainingCards = 0

    private var isMultipleChoice = false
    private var fakeAnswers = ArrayList<String>()

    private var firstFlip = true

    // Flag that keeps track of needing to disconnect for onDestroy(). Not needed if moving on
    // to game over activity
    private var needToDisconnect  = false

    private val observer: Observer<String?> = Observer { it ->
        if (it != null) {
            val msg = it.split("-")

            when (msg[0]) {
                "rounds" -> {
                    remainingCards = msg[1][0].digitToInt()
                    setInitialRounds()
                }

                "question" -> {
                    val question = msg[1]
                    currentAnswer = msg[2]
                    isMultipleChoice = msg[3].toBoolean()

                    if (isMultipleChoice) {
                        fakeAnswers.clear()
                        fakeAnswers.add(msg[4])
                        fakeAnswers.add(msg[5])
                        fakeAnswers.add(msg[6])
                    }

                    updateQuestion(question)
                }

                "score" -> {
                    updateScore(msg[1], msg[2], msg[3])
                }

                "gameOver" -> {
                    endGame(msg[1], msg[2])
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuestMultiplayerGameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        binding.guestBtnAnswer.setOnClickListener {
            totalTimeTaken = System.currentTimeMillis() - initialTime
            val timeTakenAsString = totalTimeTaken.toString()

            // indicate to user that they have submitted
            binding.guestBtnAnswer.isEnabled = false
            binding.guestWaitingMsg.visibility = View.VISIBLE

            val rightOrWrong = checkIfCorrect().toString()
            binding.guestAnswerInput.text?.clear()

            // send to host if player 2 got correct answer and time taken for host to calculate
            messagesViewModel.sendMessage("answer-$rightOrWrong-$timeTakenAsString\n")
        }

        // disable answer button if no text on editText
        binding.guestAnswerInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // enable submit if not empty, if empty disable button
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.guestBtnAnswer.isEnabled = s?.isEmpty() != true
            }
            override fun afterTextChanged(p0: Editable?) {}
        })


        // observe messages
        messagesViewModel.messagesLiveData.observe(this, observer)

        // observe disconnects
        messagesViewModel.connected.observe(this) {
            if (!it) {
                needToDisconnect = true
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        // create callback for back button to go back to properly disconnect
        // source: https://www.droidcon.com/2023/02/22/handling-back-press-in-android-13-the-correct-way/
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                needToDisconnect = true
                finish()
            }
        })

        // start timer for overall game
        startTimer()
    }

    // *NOTE: onDestroy() is not guaranteed to be called if the app is force closed, therefore
    // sometimes disconnection is not immediate and relies on timeout (DEFAULT_TIMEOUT IN MessagesViewModel).
    override fun onDestroy() {
        super.onDestroy()
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


    // if time specified, start timer (just for screen, host actually ends the game)
    private fun startTimer() {
        val timeAsString = intent.getStringExtra("time") ?: "0"
        if (timeAsString != "Unlimited Time") {

            // convert to milliseconds
            val timeAsMs : Long = timeAsString[0].digitToInt() * 60L * 1000L
            object : CountDownTimer(timeAsMs, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    binding.guestTime.text = (millisUntilFinished / 1000).toString() + " Seconds Remaining"
                }

                // nothing should be done here because host will send a message saying the game should end
                override fun onFinish() {}
            }.start()
        }
    }

    private fun setInitialRounds() {
        binding.guestRoundsRemainingText.text = "$remainingCards Rounds Remaining"
    }


    private fun updateQuestion(question: String) {
        binding.guestQuestionText.text = question

        binding.guestRoundsRemainingText.text = "$remainingCards Rounds Remaining"
        binding.guestBtnAnswer.isEnabled = false
        binding.guestWaitingMsg.visibility = View.INVISIBLE
        binding.guestAnswerInput.isClickable = true
        binding.guestAnswerInput.isFocusable = true

        remainingCards--

        if (isMultipleChoice) {
            binding.guestAnswerInput.isClickable = true
            binding.guestAnswerInput.isFocusable = false
            binding.guestAnswerInput.hint = "Tap here to answer"
            binding.guestAnswerInput.setOnClickListener {
                openMultipleChoiceDialog()
            }
        } else {
            binding.guestAnswerInput.isClickable = true
            binding.guestAnswerInput.isFocusable = true
            binding.guestAnswerInput.hint = "What is the answer?"
            binding.guestAnswerInput.isFocusableInTouchMode = true
            binding.guestAnswerInput.setOnClickListener(null)
        }


        if (firstFlip) {
            firstFlip = false
        } else {
            var cardContainer: RelativeLayout = findViewById(R.id.cardContainer)
            var animator = Animations(this)
            animator.flipCard(cardContainer)
        }

        // start timer as soon as player can see the question
        initialTime = System.currentTimeMillis()
    }

    private fun openMultipleChoiceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_multiple_choice, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        val options = ArrayList<String?>();
        options.add(fakeAnswers[0])
        options.add(fakeAnswers[1])
        options.add(fakeAnswers[2])
        options.add(currentAnswer)
        options.shuffle()

        val optionACard = dialogView.findViewById<MaterialCardView>(R.id.optionACard)
        val optionATextView = dialogView.findViewById<TextView>(R.id.optionAText)
        optionATextView.text = options[0]
        optionACard.setOnClickListener {
            binding.guestAnswerInput.setText(optionATextView.text)
            dialog.dismiss()
        }

        val optionBCard = dialogView.findViewById<MaterialCardView>(R.id.optionBCard)
        val optionBTextView = dialogView.findViewById<TextView>(R.id.optionBText)
        optionBTextView.text = options[1]
        optionBCard.setOnClickListener {
            binding.guestAnswerInput.setText(optionBTextView.text)

            dialog.dismiss()
        }

        val optionCCard = dialogView.findViewById<MaterialCardView>(R.id.optionCCard)
        val optionCTextView = dialogView.findViewById<TextView>(R.id.optionCText)
        optionCTextView.text = options[2]
        optionCCard.setOnClickListener {
            binding.guestAnswerInput.setText(optionCTextView.text)

            dialog.dismiss()
        }

        val optionDCard = dialogView.findViewById<MaterialCardView>(R.id.optionDCard)
        val optionDTextView = dialogView.findViewById<TextView>(R.id.optionDText)
        optionDTextView.text = options[3]
        optionDCard.setOnClickListener {
            binding.guestAnswerInput.setText(optionDTextView.text)
            dialog.dismiss()
        }
        dialog.show()
    }

    // Updates score and displays appropriate message
    private fun updateScore(hostScore: String, currentPlayerScore: String, whoGotItRight: String) {
        when (whoGotItRight) {
            "host" -> {
                Toast.makeText(this, "Host answered correctly faster!", Toast.LENGTH_SHORT).show()
            }

            "player2" -> {
                Toast.makeText(this, "You answered correctly faster !", Toast.LENGTH_SHORT).show()
            }

            "nobody" -> {
                Toast.makeText(this, "No one got it right!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.guestScoreText.text = "You: $currentPlayerScore"
        binding.guestPlayer2ScoreText.text = "Host: $hostScore"

        // reset the submit button and turn the waiting... text invisible
        binding.guestWaitingMsg.visibility = View.INVISIBLE
    }

    // send player to end screen once game has finished
    private fun endGame(finalPlayerScore : String, otherScore: String) {
        val deckName = intent.getStringExtra("deckName") ?: "null"

        val intent = Intent(this, MultiplayerGameOverActivity::class.java)
        intent.putExtra("hostScore", finalPlayerScore.toInt())
        intent.putExtra("player2Score", otherScore.toInt())
        intent.putExtra("guestOrHost", "guest")
        intent.putExtra("deckName", deckName)

        // needed so that when this activity relaunches, observer doesn't immediately trigger
        // with last answer that was sent
        messagesViewModel.messagesLiveData.value = null
        messagesViewModel.messagesLiveData.removeObserver(observer)

        startActivity(intent)
        finish()
    }


    private fun checkIfCorrect(): Boolean {

        // get the input of user as a string
        var userInput = binding.guestAnswerInput.text.toString()

        // check if the users response matches the cards answer (not case sensitive)
        return if (userInput.equals(currentAnswer, ignoreCase = true)) {

            Toast.makeText(this, "CORRECT!", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(this, "INCORRECT!", Toast.LENGTH_SHORT).show()
            false
        }
    }
}