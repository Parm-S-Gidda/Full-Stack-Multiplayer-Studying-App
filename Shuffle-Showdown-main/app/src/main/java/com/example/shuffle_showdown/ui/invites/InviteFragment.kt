package com.example.shuffle_showdown.ui.invites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.databinding.FragmentInviteBinding
import com.example.shuffle_showdown.messages.MessagesViewModel
import com.example.shuffle_showdown.ui.history.InviteViewModel
import com.example.shuffle_showdown.ui.play.multiplayer.GuestLobbyActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InviteFragment : Fragment() {

    private var _binding: FragmentInviteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var messagesViewModel: MessagesViewModel
    private lateinit var adapter : InviteAdapter
    private lateinit var listView : ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inviteViewModel =
            ViewModelProvider(this).get(InviteViewModel::class.java)

        _binding = FragmentInviteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InviteAdapter(
            requireContext(),
            messagesViewModel.invitesLiveData.value ?: ArrayList(),
            messagesViewModel
        )

        listView = view.findViewById(R.id.invite_listview)
        listView.adapter = adapter


        messagesViewModel.invitesLiveData.observe(viewLifecycleOwner) { it ->
            adapter.update(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}