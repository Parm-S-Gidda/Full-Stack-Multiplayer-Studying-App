package com.example.shuffle_showdown.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shuffle_showdown.databinding.HistoryListBinding
import com.example.shuffle_showdown.history.HistoryModel


class HistoryAdapter(
   // val onItemClicked: (Int, HistoryModel) -> Unit,

) : RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {

    // List we will fill up, that the RecyclerView will reference
    private var list: MutableList<HistoryModel> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            HistoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<HistoryModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(private val binding: HistoryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryModel) {

            // Sets the respective text views to the item's properties
            binding.deckName.text = item.deckName

            if(item.otherPlayerUserName == "Single-Player"){
                binding.singleMultiplayerTitle.text = "Single-Player"
                binding.otherPlayerName.text = ""
            }
            else{
                binding.singleMultiplayerTitle.text = "Multi-Player"
                binding.otherPlayerName.text = item.otherPlayerUserName
            }

            //check who won and display that
            if(item.iwon == 0){
                binding.winner.text = "Winner: You"
            }
            else if(item.iwon == 1){
                binding.winner.text = "Winner: " + item.otherPlayerUserName
            }
            else {
                binding.winner.text = "Winner: Tie"
            }
            binding.score.text = item.score
        }
    }
}