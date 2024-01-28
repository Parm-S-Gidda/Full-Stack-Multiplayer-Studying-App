package com.example.shuffle_showdown.ui.invites

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.messages.MessagesViewModel
import com.example.shuffle_showdown.ui.play.multiplayer.GuestLobbyActivity
import dagger.hilt.android.AndroidEntryPoint

// Adapter for invite messages
class InviteAdapter internal constructor(
    private val context: Context,
    private var invites: ArrayList<String>,
    private val messagesViewModel: MessagesViewModel
) : BaseAdapter() {
    override fun getCount(): Int {
        return invites.size
    }

    override fun getItem(position: Int): String {
        return invites[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.invite_list_item, null)

        val inviteMessage: TextView = view.findViewById(R.id.invite_message)
        val username = invites[position].split("-")[1]
        inviteMessage.text = "Player $username has invited you to play"

        // delete the invite
        val declineButton: Button = view.findViewById(R.id.decline_button)
        declineButton.setOnClickListener {
            messagesViewModel.deleteInvite(position)
        }

        val acceptButton: Button = view.findViewById(R.id.accept_button)
        acceptButton.setOnClickListener {
            messagesViewModel.deleteInvite(position)
            messagesViewModel.acceptInvite(username) // establish a connection

            // launch guest lobby activity
            val intent = Intent(context, GuestLobbyActivity::class.java)
            context.startActivity(intent)
        }

        return view
    }

    fun update(newInvites: ArrayList<String>) {
        invites = ArrayList(newInvites)
        this.notifyDataSetChanged()
    }
}