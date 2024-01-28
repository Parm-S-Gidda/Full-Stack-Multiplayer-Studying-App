package com.example.shuffle_showdown.ui.history

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.account.AccountViewModel
import com.example.shuffle_showdown.databinding.FragmentDecksBinding
import com.example.shuffle_showdown.databinding.FragmentHistoryBinding
import com.example.shuffle_showdown.ui.cards.CardListActivity
import com.example.shuffle_showdown.ui.decks.DecksAdapter
import com.example.shuffle_showdown.ui.decks.DecksModel
import com.example.shuffle_showdown.ui.decks.DecksViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var account: FirebaseUser

    private val viewModel: HistoryViewModel by viewModels()
    private val adapter by lazy { HistoryAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Get the account, so we can access its info
        account = accountViewModel.getCurrentUser()!!


        // Recycler view
        binding.recyclerView.adapter = adapter

        // Asks the ViewModel for the Database's list, and fills the RecyclerView with it
        viewModel.getFullHistory(account.uid)
        viewModel.historySegments.observe(viewLifecycleOwner) {
            adapter.updateList(it.toMutableList())
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        // Referencing https://firebase.google.com/docs/auth/android/manage-users
        // Get the account, so we can access its info
        account = accountViewModel.getCurrentUser()!!

        refreshHistory()
    }

    private fun refreshHistory() {
        viewModel.getFullHistory(account.uid)
        viewModel.historySegments.observe(viewLifecycleOwner) {
            adapter.updateList(it.toMutableList())
        }
    }

}