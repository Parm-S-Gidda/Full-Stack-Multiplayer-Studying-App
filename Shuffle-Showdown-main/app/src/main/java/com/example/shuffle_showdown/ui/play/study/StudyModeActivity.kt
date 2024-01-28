package com.example.shuffle_showdown.ui.play.study

import android.os.Bundle
import android.view.MenuItem
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.shuffle_showdown.Animations
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.cueCard.CueCard
import com.example.shuffle_showdown.cueCard.CueCardViewModel
import com.example.shuffle_showdown.databinding.ActivityStudyModeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudyModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyModeBinding

    private val viewModel: CueCardViewModel by viewModels()

    // Sets up the game's cards to be displayed
    private lateinit var activeCards: List<CueCard>
    private var activeCardsIndex: Int = 0
    private var isQuestionSide = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        var animator = Animations(this)
        var cardContainer: ConstraintLayout = findViewById(R.id.cardContainerStudy)

        binding.btnFlip.setOnClickListener {
            animator.fade(cardContainer)
            isQuestionSide = !isQuestionSide
            loadCard()
        }

        binding.btnPrev.setOnClickListener {
            animator.flipCard(cardContainer)
            activeCardsIndex--
            if(activeCardsIndex < 0)
                // Loop to end
                activeCardsIndex = activeCards.size - 1
            loadCard()
        }

        binding.btnNext.setOnClickListener {
            animator.flipCard(cardContainer)
            activeCardsIndex++
            // Loop to start
            if(activeCardsIndex >= activeCards.size)
                activeCardsIndex = 0
            loadCard()
        }
    }

    override fun onResume() {
        super.onResume()
        getDeck()
    }

    private fun getDeck() {
        val deckId = intent.getStringExtra("deckId").toString()

        viewModel.getCueCards(deckId)
        viewModel.cueCards.observe(this) {
            activeCards = it

            loadCard()
        }
    }

    private fun loadCard() {
        if (isQuestionSide) {
            binding.msgLabel.text = getString(R.string.question)
            binding.msg.text = activeCards[activeCardsIndex].term
        } else {
            binding.msgLabel.text = getString(R.string.answer)
            binding.msg.text = activeCards[activeCardsIndex].answer
        }
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
}