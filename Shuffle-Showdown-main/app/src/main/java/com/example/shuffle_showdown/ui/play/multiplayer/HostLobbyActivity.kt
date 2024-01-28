package com.example.shuffle_showdown.ui.play.multiplayer


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.account.AccountViewModel
import com.example.shuffle_showdown.account.Result
import com.example.shuffle_showdown.cueCard.CueCardViewModel
import com.example.shuffle_showdown.customAccount.CustomAccountRepository
import com.example.shuffle_showdown.databinding.ActivityHostLobbyBinding
import com.example.shuffle_showdown.messages.MessagesViewModel
import com.example.shuffle_showdown.ui.decks.DecksAdapter
import com.example.shuffle_showdown.ui.decks.DecksModel
import com.example.shuffle_showdown.ui.decks.DecksViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HostLobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostLobbyBinding
    private val viewModel: DecksViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()
    val database = FirebaseFirestore.getInstance()
    private val customAccRepository = CustomAccountRepository(database)
    private var firstTime: Boolean = true

    private val cueCardViewModel: CueCardViewModel by viewModels()

    // Flag that keeps track of needing to disconnect for onDestroy(). Not needed if moving on
    // to multiplayer game activity
    private var needToDisconnect = false

    @Inject
    lateinit var messagesViewModel: MessagesViewModel

    // For selecting which deck use in the game
    private var selectedDeck: DecksModel? = null

    private val adapter by lazy {
        DecksAdapter(
            onItemClicked = { _, item ->
                cueCardViewModel.getCueCardsAlt(item.id)
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
                                // If it's not empty, then the item is correctly selected
                                selectedDeck = item

                            } else {
                                selectedDeck = null
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
            },
            onDeleteClicked = { _, _ ->

            },
            true,
            this,
            true
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHostLobbyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        // Recycler view
        binding.recyclerView.adapter = adapter

        // Asks the ViewModel for the Database's list, and fills the RecyclerView with it
        val uid = accountViewModel.getCurrentUser()?.uid
        if (uid != null) {
            viewModel.getDecks(uid)
        }
        viewModel.decks.observe(this) { it ->
            adapter.updateList(it.toMutableList())
            selectDefaultDeck()
        }

        // when a connection is established (another player accepted invite)
        messagesViewModel.connected.observe(this) {
            if (it) {
                // disable invite button
                binding.btnInvite.isClickable = false
                binding.btnInvite.visibility = View.INVISIBLE

                // display connected player
                binding.textPlayer2.text = "PLayer 2\nConnected"

                // reveal disconnect button
                binding.hostDisconnectButton.visibility = View.VISIBLE

                // enable start button
                binding.btnStart.isClickable = true

            } else {
                // re-enable invite button
                binding.btnInvite.isClickable = true
                binding.btnInvite.visibility = View.VISIBLE

                // remove display of connected player
                binding.textPlayer2.text = "Player 2\nNot Connected"

                // hide disconnect button
                binding.hostDisconnectButton.visibility = View.GONE
                firstTime = false

                // block start button
                binding.btnStart.isClickable = false

            }
        }

        /**
         * Buttons
         */

        binding.btnInvite.setOnClickListener { // Invite player
            showInviteDialogue()
        }

        binding.btnStart.setOnClickListener {// Start game
            // Get selected Deck Id
            if (selectedDeck != null) {
                val deckId: String = selectedDeck!!.id

                // grab round and times specified by host
                val rounds = binding.spinnerRounds.selectedItem.toString()
                val time = binding.spinnerTime.selectedItem.toString()
                val deckName = selectedDeck!!.name
                messagesViewModel.sendMessage("start-$deckName-$time\n")

                val intent = Intent(this, HostMultiplayerGameActivity::class.java)
                intent.putExtra("deckId", deckId)
                intent.putExtra("deckName", deckName)
                intent.putExtra("rounds", rounds)
                intent.putExtra("time", time)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please select a non-empty deck.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.hostDisconnectButton.setOnClickListener { // Disconnect players
            messagesViewModel.disconnect()
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

    // If deckId specified, automatically select that deck
    private fun selectDefaultDeck() {
        val deckId: String? = intent.getStringExtra("deckId")
        if (deckId != null) {
            val listItems = adapter.getItems()

            // find deck by id
            val targetItem = listItems.find {
                it.id == deckId
            }
            targetItem?.let { deck ->

                // If deck is found, find its position
                val targetItemIndex = listItems.indexOf(deck)

                // click deck in recyclerview to select it
                // source on how to click: https://stackoverflow.com/questions/37192024/programmatically-make-a-click-or-touch-an-item-in-recyclerview
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    binding.recyclerView.findViewHolderForAdapterPosition(
                        targetItemIndex
                    )?.itemView?.performClick()
                }, 100)
            }
        }
    }

    private fun showInviteDialogue() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogue_invite_player, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val inputText = dialogView.findViewById<TextInputLayout>(R.id.playerName).editText
        builder.setPositiveButton("Submit") { _, _ ->
            val textValue = inputText?.text.toString()
            if (textValue.isNotEmpty() && textValue.isNotBlank()) {
                // Invite player
                Log.d("test", "username: $textValue")


                // SOURCE: Coroutine demo and CHATGPT for async calling and transfering to main thread
                CoroutineScope(Dispatchers.IO).launch {
                    val account = customAccRepository.getAccountByEmail(textValue)

                    //check if the user was found if they were send their ip to the messageViewModel
                    if(account.id == "N/A"){

                        withContext(Dispatchers.Main) {    Toast.makeText(this@HostLobbyActivity, "Could not find a user with that email.", Toast.LENGTH_SHORT).show()}
                    }
                    else{

                        Log.d("test", "other player IP: ${account.address}")
                        withContext(Dispatchers.Main) { account.address?.let { it1 -> messagesViewModel.sendInvite(it1) }
                            Toast.makeText(this@HostLobbyActivity, "Successfully invited player", Toast.LENGTH_SHORT).show()}

                    }
                }

            } else {
                Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

}