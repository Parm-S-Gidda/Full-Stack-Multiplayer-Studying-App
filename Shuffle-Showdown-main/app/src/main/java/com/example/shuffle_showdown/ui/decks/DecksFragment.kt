package com.example.shuffle_showdown.ui.decks

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.account.AccountViewModel
import com.example.shuffle_showdown.databinding.FragmentDecksBinding
import com.example.shuffle_showdown.ui.cards.CardListActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecksFragment : Fragment() {

    private var _binding: FragmentDecksBinding? = null
    private val binding get() = _binding!!

    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var account: FirebaseUser

    private val viewModel: DecksViewModel by viewModels()
    private val adapter by lazy {
        DecksAdapter(
            onItemClicked = { pos, item ->
                val intent = Intent(requireActivity(), CardListActivity::class.java)
                intent.putExtra("deckId", item.id)
                intent.putExtra("deckName", item.name)
                startActivity(intent)
            },
            onDeleteClicked = { pos, item ->
                confirmDelete(item)
            },
            false,
            context,
            false
        )
    }
    private var dataList = mutableListOf<DecksModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDecksBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Get the account, so we can access its info
        account = accountViewModel.getCurrentUser()!!

        binding.btnCreate.setOnClickListener {
            showCreateDialogue()
        }

        // Recycler view
        binding.recyclerView.adapter = adapter

        // Asks the ViewModel for the Database's list, and fills the RecyclerView with it
        viewModel.getDecks(account.uid)
        viewModel.decks.observe(viewLifecycleOwner) {
            adapter.updateList(it.toMutableList())
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        // Referencing https://firebase.google.com/docs/auth/android/manage-users
        // Get the account, so we can access its info
        account = accountViewModel.getCurrentUser()!!

        refreshDecks()
    }

    // Source: DIALOG DEMO https://canvas.sfu.ca/courses/80625/pages/customizing-dialogs-with-dialogfragment
    // Additional source: CHATGPT to modify code to work in this fragment and TextInputLayout UI
    private fun showCreateDialogue() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialogue_create_deck, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        val inputText = dialogView.findViewById<TextInputLayout>(R.id.deckName).editText
        builder.setPositiveButton("Submit") { dialog, which ->
            val textValue = inputText?.text.toString()
            if (textValue.isNotEmpty() && textValue.isNotBlank() ) {
                viewModel.addDeck(
                    DecksModel(
                        id= "",
                        accountId= account.uid,
                        name = textValue,
                        favorite = false,
                        cardCount = 0
                    )
                )
                refreshDecks()
                Toast.makeText(requireContext(), "Successfully created deck", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter name", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun refreshDecks() {
        viewModel.getDecks(account.uid)
        viewModel.decks.observe(viewLifecycleOwner) {
            adapter.updateList(it.toMutableList())
        }
    }

    private fun confirmDelete(deck: DecksModel) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Confirm Delete")
            setMessage("Are you sure you want to delete this deck?")
            setPositiveButton("Yes") { dialog, which ->
                viewModel.deleteDeck(deck.id)
                refreshDecks()
                Toast.makeText(requireContext(), "Deck deleted", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("No", null)
            show()
        }
    }


}