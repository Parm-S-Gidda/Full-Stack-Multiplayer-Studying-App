package com.example.shuffle_showdown.ui.play.singleplayer

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.shuffle_showdown.account.AccountViewModel
import com.example.shuffle_showdown.account.Result
import com.example.shuffle_showdown.cueCard.CueCardViewModel
import com.example.shuffle_showdown.databinding.ActivitySelectDeckBinding
import com.example.shuffle_showdown.ui.decks.DecksAdapter
import com.example.shuffle_showdown.ui.decks.DecksViewModel
import com.example.shuffle_showdown.ui.play.study.StudyModeActivity
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectDeckActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectDeckBinding

    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var account: FirebaseUser

    private var mode: String = "single"

    private val cueCardViewModel: CueCardViewModel by viewModels()

    // Sets up the singleton ViewModel, and RecyclerView's adapter
    private val viewModel: DecksViewModel by viewModels()
    private val adapter by lazy {
        mode = intent.getStringExtra("mode").toString()
        DecksAdapter(
            onItemClicked = { pos, item ->
                checkDeckSize(item.id, item.name)

//                val intent =
//                    if (mode == "single")
//                        // Single player mode
//                        Intent(this, SingleplayerGameActivity::class.java)
//                    else
//                        // Study mode
//                        Intent(this, StudyModeActivity::class.java)
//                intent.putExtra("deckId", item.id)
//                intent.putExtra("deckName", item.name)
//                startActivity(intent)
            },
            onDeleteClicked = { pos, item ->

            },
            true,
            this,
            false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectDeckBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Get the account, so we can access its info
        account = accountViewModel.getCurrentUser()!!

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        // TODO: error handling
        // Recycler view
        binding.recyclerView.adapter = adapter

        // Asks the ViewModel for the Database's list, and fills the RecyclerView with it
        viewModel.getDecks(account.uid)
        viewModel.decks.observe(this) {
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

    private fun checkDeckSize(id: String, name: String) {
        cueCardViewModel.getCueCardsAlt(id)
        cueCardViewModel.cueCardsAlt.observe(this) {
            when (it) {
                is Result.Loading -> {
                    // Loading
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    // Login success, go into app
                    binding.progressBar.visibility = View.GONE

                    if (it.data.isNotEmpty()) {
                        val intent =
                            if (mode == "single")
                                // Single player mode
                                Intent(this, SingleplayerGameActivity::class.java)
                            else
                                // Study mode
                            Intent(this, StudyModeActivity::class.java)
                        intent.putExtra("deckId", id)
                        intent.putExtra("deckName", name)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "The deck is empty.", Toast.LENGTH_SHORT).show()
                    }

                    cueCardViewModel.cueCardsAlt.removeObservers(this)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    cueCardViewModel.cueCardsAlt.removeObservers(this)
                }
            }
        }
    }

}