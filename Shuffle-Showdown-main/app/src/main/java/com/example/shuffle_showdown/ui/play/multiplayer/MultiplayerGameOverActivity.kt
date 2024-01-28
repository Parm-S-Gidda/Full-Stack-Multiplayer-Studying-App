package com.example.shuffle_showdown.ui.play.multiplayer

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.shuffle_showdown.MainActivity
import com.example.shuffle_showdown.account.AccountViewModel
import com.example.shuffle_showdown.databinding.ActivityMultiplayerGameOverBinding
import com.example.shuffle_showdown.history.HistoryModel
import com.example.shuffle_showdown.messages.MessagesViewModel
import com.example.shuffle_showdown.ui.history.HistoryViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// This activity looks slightly different for guest or host. Not enough to warrant a separate activity
@AndroidEntryPoint
class MultiplayerGameOverActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMultiplayerGameOverBinding

    // Flag that keeps track of needing to disconnect for onDestroy(). Not needed if moving on
    // to lobby activity
    private var needToDisconnect = false

    @Inject
    lateinit var messagesViewModel: MessagesViewModel

    private var replayIntent : Intent? = null
    private lateinit var guestOrHost : String


    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var account: FirebaseUser

    private val historyViewModel: HistoryViewModel by viewModels()

    companion object {
        const val YOU_WON = 0
        const val YOU_LOST = 1
        const val DRAW = 2
    }

    private val observer: Observer<String?> = Observer {
        if (it != null) {
            val msg = it.split("-")

            when (msg[0]) {
                "replay" -> {
                    // if replay intent not been set (host player has not said replay yet), set the replay intent
                    if (replayIntent == null) {
                        setReplayIntent(guestOrHost)

                        // both players have said replay, notify player 2 and go to lobby
                    } else {
                        messagesViewModel.sendMessage("sendToLobby-\n")
                        launchReplayIntent()
                    }
                }

                // guest receives msg from host to go back to the lobby
                "sendToLobby" -> {
                    launchReplayIntent()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerGameOverBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        val hostScore = intent.getIntExtra("hostScore", -1)
        val player2Score = intent.getIntExtra("player2Score", -1)
        guestOrHost = intent.getStringExtra("guestOrHost") ?: "null"


        displayScore(hostScore, player2Score, guestOrHost)
        val whoWon = determineWhoWon(hostScore, player2Score, guestOrHost)
        addToHistory(hostScore, player2Score, whoWon)

        binding.replayButton.setOnClickListener {
            if (guestOrHost == "host" ) {
                // if replay intent not been set (other player has not said replay yet), set the replay intent
                if (replayIntent == null) {
                    setReplayIntent(guestOrHost)

                    // both players have said replay, notify player 2 and go to lobby
                } else {
                    messagesViewModel.sendMessage("sendToLobby-\n")
                    launchReplayIntent()
                }
            } else if (guestOrHost == "guest") {
                setReplayIntent(guestOrHost)
                messagesViewModel.sendMessage("replay-\n")
            }

            binding.replayButton.isEnabled = false
            binding.waitingText.visibility = View.VISIBLE
        }

        binding.gameoverDisconnectButton.setOnClickListener {
            onDisconnect()
        }

        messagesViewModel.connected.observe(this) {
            if (!it) {
                val intent = if (guestOrHost == "host") {
                    Intent(this, HostLobbyActivity::class.java)
                } else {
                    Intent(this, MainActivity::class.java)
                }

                // needed so that when this activity relaunches, observer doesn't immediately trigger
                // with last answer that was sent
                messagesViewModel.messagesLiveData.value = null
                messagesViewModel.messagesLiveData.removeObserver(observer)

                startActivity(intent)
                finish()
            }
        }

        messagesViewModel.messagesLiveData.observe(this, observer)

        // create callback for back button to go back to the proper place and disconnect
        // source: https://www.droidcon.com/2023/02/22/handling-back-press-in-android-13-the-correct-way/
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onDisconnect()
            }
        })

    }

    // *NOTE: onDestroy() is not guaranteed to be called if the app is force closed, therefore
    // sometimes disconnection is not immediate and has to rely on timeout (DEFAULT_TIMEOUT IN MessagesViewModel).
    override fun onDestroy() {
        super.onDestroy()
        messagesViewModel.messagesLiveData.removeObservers(this)
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

    // Prepare the replay intent, which also indicates that one of 2 players have hit replay
    private fun setReplayIntent(guestOrHost: String) {
        replayIntent = when (guestOrHost) {
            // host must wait for guest message before replaying
            "host" -> {
                Intent(this, HostLobbyActivity::class.java)
            }
            // indicate to host that you are ready to replay
            "guest" -> {
                Intent(this, GuestLobbyActivity::class.java)
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }
    }

    private fun launchReplayIntent() {
        // needed so that when this activity relaunches, observer doesn't immediately trigger
        // with last answer that was sent
        messagesViewModel.messagesLiveData.value = null
        messagesViewModel.messagesLiveData.removeObserver(observer)

        startActivity(replayIntent)
        finish()
    }

    // Displays the scores in the correct order
    private fun displayScore(hostScore : Int, player2Score: Int, guestOrHost: String) {
        if (guestOrHost == "host") {
            binding.gameoverPlayer1ScoreText.text = "You: $hostScore"
            binding.gameoverPlayer2ScoreText.text = "Player 2: $player2Score"
        } else {
            binding.gameoverPlayer1ScoreText.text = "You: $player2Score"
            binding.gameoverPlayer2ScoreText.text = "Host: $hostScore"
        }
    }

    // Displays the correct winner message
    private fun determineWhoWon(hostScore : Int, player2Score : Int, guestOrHost : String) : Int {
        // draw
        if (hostScore == player2Score) {
            binding.winnerText.text = "It is a Draw!"
            return DRAW

            // host won
        } else if (hostScore > player2Score) {
            return if (guestOrHost == "host") {
                binding.winnerText.text = "You won!"
                YOU_WON
            } else {
                binding.winnerText.text = "The host won!"
                YOU_LOST
            }

            // player 2 won
        } else {
            return if (guestOrHost == "host") {
                binding.winnerText.text = "Player 2 has won!"
                YOU_LOST
            } else {
                binding.winnerText.text = "You won!"
                YOU_WON
            }
        }
    }


    // adds to the history database
    private fun addToHistory(hostScore: Int, player2Score: Int, whoWon : Int) {
        val historyScoreString = when (guestOrHost) {
            "host" -> {
                "$hostScore - $player2Score"
            }
            "guest" -> {
                "$player2Score - $hostScore"
            }
            else -> {
                "null"
            }
        }

        val deckName = intent.getStringExtra("deckName") ?: "null"

        // Insert into history db
        account = accountViewModel.getCurrentUser()!!
        val history = HistoryModel(
            id= "",
            accountId= account.uid,
            deckName = deckName,
            otherPlayerUserName = "Player 2",
            iwon = whoWon, // ***** CHANGE TO whoWon var *****
            score = "Score: $historyScoreString"
        )
        historyViewModel.addHistorySegment(history)
    }

    private fun onDisconnect() {
        needToDisconnect = true
        finish()
    }
}