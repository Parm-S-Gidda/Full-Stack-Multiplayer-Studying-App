package com.example.shuffle_showdown.ui.play.singleplayer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.account.AccountViewModel
import com.example.shuffle_showdown.cueCard.CueCard
import com.example.shuffle_showdown.cueCard.CueCardRepository
import com.example.shuffle_showdown.databinding.ActivitySingleplayerGameBinding
import com.example.shuffle_showdown.history.HistoryModel
import com.example.shuffle_showdown.ui.decks.DecksModel
import com.example.shuffle_showdown.ui.history.HistoryViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.util.Queue
import kotlin.math.roundToInt

@AndroidEntryPoint
class singleplayerGameOverScreenActivity: AppCompatActivity() {

    private lateinit var score: TextView
    private lateinit var percentage: TextView
    private lateinit var continueButton: Button
    private val historyViewModel: HistoryViewModel by viewModels()
    private lateinit var account: FirebaseUser
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_singleplayer_gameover)


        // Get the account, so we can access its info
        account = accountViewModel.getCurrentUser()!!

        //get the score and total amount of cards from the previous game played
        val scoreFinal = intent.getIntExtra("score", 0)
        val totalFinal = intent.getIntExtra("total", 0)
        val deckName1 = intent.getStringExtra("deckName")

        score = findViewById(R.id.score_text)
        percentage = findViewById(R.id.percentage_text)
        continueButton = findViewById(R.id.continue_button)

        //calculate the rounded percentage
        var percetage: Double = (scoreFinal.toDouble()/totalFinal) * 100
        var roundedpercent = percetage.roundToInt()

        //display the scores
        score.text = scoreFinal.toString() + " out of " + totalFinal.toString() + "!"
        percentage.text = roundedpercent.toString() + "%"

        continueButton.setOnClickListener(){

            //add a history segment to the db
            deckName1?.let { it1 ->
                HistoryModel(
                    id= "",
                    accountId= account.uid,
                    deckName = it1,
                    otherPlayerUserName = "Single-Player",
                    iwon = 0,
                    score = "Score: " + scoreFinal.toString() + "-" + totalFinal.toString()
                )
            }?.let { it2 ->
                historyViewModel.addHistorySegment(
                    it2
                )
            }
            finish()
        }
    }
}

