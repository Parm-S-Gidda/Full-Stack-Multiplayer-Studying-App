package com.example.shuffle_showdown.ui.cards

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.shuffle_showdown.cueCard.CueCard
import com.example.shuffle_showdown.cueCard.CueCardListingAdapter
import com.example.shuffle_showdown.cueCard.CueCardViewModel
import com.example.shuffle_showdown.databinding.ActivityCardListBinding
import com.example.shuffle_showdown.ui.play.PlayViewModel
import com.example.shuffle_showdown.ui.play.multiplayer.HostLobbyActivity
import com.example.shuffle_showdown.ui.play.singleplayer.SingleplayerGameActivity
import com.example.shuffle_showdown.ui.play.study.StudyModeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardListBinding

    // Sets up the singleton ViewModel, and RecyclerView's adapter
    private val viewModel: CueCardViewModel by viewModels()

    private lateinit var adapter: CueCardListingAdapter
    private lateinit var cueCards: List<CueCard>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val deckId = intent.getStringExtra("deckId").toString()
        val deckName = intent.getStringExtra("deckName").toString()
        // Initialize the adapter here
        adapter = CueCardListingAdapter(
            onItemClicked = { pos, item ->
                val intent = Intent(this, CueCardDetailActivity::class.java)
                intent.putExtra("type", "view")
                intent.putExtra("cue card", item)
                intent.putExtra("deckId", deckId)

                startActivity(intent)
            }
        )
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        val cardsViewModel = ViewModelProvider(this).get(PlayViewModel::class.java)

        binding.fab.setOnClickListener {
            val intent = Intent(this, CueCardDetailActivity::class.java)
            intent.putExtra("deckId", deckId)
            startActivity(intent)
        }

        val textView: TextView = binding.loadingData
        cardsViewModel.text.observe(this) {
            textView.text = it
        }

        // Sets up the recycler view
        binding.recyclerView.adapter = adapter

        // Asks the ViewModel for the Database's list, and fills the RecyclerView with it
        viewModel.getCueCards(deckId)
        viewModel.cueCards.observe(this) {
            adapter.updateList(it.toMutableList())
            cueCards = it
        }

        // Set the singleplayer launch
        binding.btnSingleplayer.setOnClickListener {
            if (cueCards.isEmpty()) {
                Toast.makeText(this, "The deck is empty.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, SingleplayerGameActivity::class.java)
                intent.putExtra("deckId", deckId)
                intent.putExtra("deckName", deckName)
                startActivity(intent)
            }
        }

        // Set the study mode launch
        binding.btnStudy.setOnClickListener {
            if (cueCards.isEmpty()) {
                Toast.makeText(this, "The deck is empty.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, StudyModeActivity::class.java)
                intent.putExtra("deckId", deckId)
                intent.putExtra("deckName", deckName)
                startActivity(intent)
            }
        }

        // set the multiplayer launch
        binding.btnMultiplayer.setOnClickListener {
            if (cueCards.isEmpty()) {
                Toast.makeText(this, "The deck is empty.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, HostLobbyActivity::class.java)
                intent.putExtra("deckId", deckId)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Updates the list once returning to this Activity
        val deckId = intent.getStringExtra("deckId").toString()

        viewModel.getCueCards(deckId)
        viewModel.cueCards.observe(this) {
            adapter.updateList(it.toMutableList())
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
