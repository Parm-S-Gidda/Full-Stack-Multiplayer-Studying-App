package com.example.shuffle_showdown.cueCard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shuffle_showdown.databinding.CardListItemBinding

// Assistance from https://www.youtube.com/watch?v=7ZNk87k441U
// Creates the adapter for the recycler view, showing the
// respective groups cue cards and sets up the onClickListener per item
class CueCardListingAdapter(
    val onItemClicked: (Int, CueCard) -> Unit
) : RecyclerView.Adapter<CueCardListingAdapter.MyViewHolder>() {

    // List we will fill up, that the RecyclerView will reference
    private var list: MutableList<CueCard> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = CardListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<CueCard>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(val binding: CardListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CueCard){
            // Sets the respective text views to the item's properties
            binding.cardTermValue.text = item.term
            binding.msg.text = item.answer
            binding.itemLayout.setOnClickListener { onItemClicked.invoke(adapterPosition, item) }
        }
    }
}