package com.example.shuffle_showdown.ui.play.multiplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.shuffle_showdown.databinding.ActivityGuestLobbyBinding
import com.example.shuffle_showdown.messages.MessagesViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GuestLobbyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuestLobbyBinding
    private var firstTime = true

    // Flag that keeps track of needing to disconnect for onDestroy(). Not needed if moving on
    // to multiplayer game activity
    private var needToDisconnect = false

    @Inject
    lateinit var messagesViewModel: MessagesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuestLobbyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        messagesViewModel.connected.observe(this) {
            if (!it && !firstTime) { // !firstTime needed because this connect is false first time (before properly changing)
                // if no longer connected, finish the activity
                needToDisconnect = true
                finish()
            }

            firstTime = false
        }

        messagesViewModel.messagesLiveData.observe(this) {
            if (it != null) {
                val splitMsg = it.split("-")
                val token = splitMsg[0]

                if (token == "start") {
                    val deckName = splitMsg[1]
                    val time = splitMsg[2]

                    val intent = Intent(this, GuestMultiplayerGameActivity::class.java)
                    intent.putExtra("deckName", deckName)
                    intent.putExtra("time", time)
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.guestDisconnectButton.setOnClickListener {
            needToDisconnect = true
            finish()
        }
    }

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
}