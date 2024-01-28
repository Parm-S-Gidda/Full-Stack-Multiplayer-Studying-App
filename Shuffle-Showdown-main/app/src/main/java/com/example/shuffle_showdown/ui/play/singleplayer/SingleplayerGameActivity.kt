package com.example.shuffle_showdown.ui.play.singleplayer

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shuffle_showdown.Animations
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.cueCard.CueCard
import com.example.shuffle_showdown.cueCard.CueCardRepository
import com.example.shuffle_showdown.databinding.ActivitySingleplayerGameBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue

@AndroidEntryPoint
class SingleplayerGameActivity : AppCompatActivity() {

    val database = FirebaseFirestore.getInstance()
    private val cueCardRepository = CueCardRepository(database)
    private lateinit var nextCard: CueCard
    private var currentScore: Int = 0
    private var totalCardsRemaining: Int = 0
    private var totalCards: Int = 0
    private var deckName: String = ""
    private var firstFlip = true

    private lateinit var binding: ActivitySingleplayerGameBinding
    private val scope = CoroutineScope(Dispatchers.Main)

    // Sets up the game's cards to be displayed
    private lateinit var activeCards: Queue<CueCard>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleplayerGameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        activeCards = LinkedList()
        loadCards()

        //on click listener for when the user presses the submit button
        binding.btnAnswer.setOnClickListener(){

            if(binding.answerInput.text.toString().isNullOrBlank()){
                Toast.makeText(this, "Answers cannot be blank", Toast.LENGTH_SHORT).show()
            }
            else {


                //check if user's answer was correct and then set the editText box to be blank
                checkIfCorrect()
                binding.answerInput.setText("")


                //if there are no more cards after this next submit launch the game over screen activity and then finsih
                if (totalCardsRemaining == 0) {

                    val intent = Intent(this, singleplayerGameOverScreenActivity::class.java)
                    intent.putExtra("score", currentScore)
                    intent.putExtra("total", totalCards)
                    intent.putExtra("deckName", deckName)
                    startActivity(intent)
                    finish()
                } else {
                    //load the next question for the user to answer
                    loadNextQuestion()
                }
            }

        }

    }

    private fun loadCards() {
        val deckId = intent.getStringExtra("deckId").toString()
            deckName = intent.getStringExtra("deckName").toString()
        // SOURCE: Coroutine demo and CHATGPT for async calling and transfering to main thread
        CoroutineScope(Dispatchers.IO).launch {
            val cueCards = cueCardRepository.getCueCards(deckId)

            //set the 2 total card values which track how many cards are left and how many we started with
            totalCardsRemaining = cueCards.size
            totalCards = cueCards.size
            withContext(Dispatchers.Main) {
                activeCards.addAll(cueCards)
                loadNextQuestion()
            }
        }

        loadNextQuestion()
    }

    private fun loadNextQuestion() {
        if (activeCards.isNullOrEmpty()) {
            binding.questionText.text = "Loading $deckName..."
            binding.answerInput.visibility = View.INVISIBLE
            binding.btnAnswer.visibility = View.INVISIBLE
            return
        }


        // Refresh UI
        binding.btnAnswer.visibility = View.VISIBLE
        binding.answerInput.visibility = View.VISIBLE

        //display how many cards are left
        binding.cardsRemainingText.text = (totalCardsRemaining - 1).toString() + " Cards Remaining"
        nextCard = activeCards.poll()

        if (nextCard.isMultipleChoice == true) {
            binding.answerInput.isClickable = true
            binding.answerInput.isFocusable = false
            binding.answerInput.hint = "Tap here to answer"
            binding.answerInput.setOnClickListener {
                openMultipleChoiceDialog()
            }
        } else {
            binding.answerInput.isClickable = true
            binding.answerInput.isFocusable = true
            binding.answerInput.hint = "What is the answer?"
            binding.answerInput.isFocusableInTouchMode = true
            binding.answerInput.setOnClickListener(null)
        }

        if (firstFlip) {
            firstFlip = false
        } else {
            var cardContainer: RelativeLayout = findViewById(R.id.cardContainer)
            var animator = Animations(this)
            animator.flipCard(cardContainer)
        }

        //set the text of the next question
        binding.questionText.text = nextCard.term
    }

    private fun openMultipleChoiceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_multiple_choice, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        val options = ArrayList<String?>()
        options.add(nextCard.fakeAnswer1)
        options.add(nextCard.fakeAnswer2)
        options.add(nextCard.fakeAnswer3)
        options.add(nextCard.answer)
        options.shuffle()

        val optionACard = dialogView.findViewById<MaterialCardView>(R.id.optionACard)
        val optionATextView = dialogView.findViewById<TextView>(R.id.optionAText)
        optionATextView.text = options[0]
        optionACard.setOnClickListener {
            binding.answerInput.setText(optionATextView.text)
            dialog.dismiss()
        }

        val optionBCard = dialogView.findViewById<MaterialCardView>(R.id.optionBCard)
        val optionBTextView = dialogView.findViewById<TextView>(R.id.optionBText)
        optionBTextView.text = options[1]
        optionBCard.setOnClickListener {
            binding.answerInput.setText(optionBTextView.text)

            dialog.dismiss()
        }

        val optionCCard = dialogView.findViewById<MaterialCardView>(R.id.optionCCard)
        val optionCTextView = dialogView.findViewById<TextView>(R.id.optionCText)
        optionCTextView.text = options[2]
        optionCCard.setOnClickListener {
            binding.answerInput.setText(optionCTextView.text)

            dialog.dismiss()
        }

        val optionDCard = dialogView.findViewById<MaterialCardView>(R.id.optionDCard)
        val optionDTextView = dialogView.findViewById<TextView>(R.id.optionDText)
        optionDTextView.text = options[3]
        optionDCard.setOnClickListener {
            binding.answerInput.setText(optionDTextView.text)
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Close the activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun checkIfCorrect(){

        //get the input of user as a string
        var userInput = binding.answerInput.text.toString()

        //check if the users response matches the cards answer (not case sensitive)
        if (userInput.equals(nextCard.answer, ignoreCase = true)) {

            //if the user was correct increase their score and make toast
            currentScore++
            binding.scoreText.text = "Score: " + currentScore

            Toast.makeText(this, "CORRECT!", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "INCORRECT!", Toast.LENGTH_SHORT).show()
        }

        //decrease the total number of cards remaining
        totalCardsRemaining--


    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

}