package com.example.shuffle_showdown.ui.decks

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shuffle_showdown.databinding.DeckListItemBinding

class DecksAdapter(
    val onItemClicked: (Int, DecksModel) -> Unit,
    private val onDeleteClicked:  (Int, DecksModel) -> Unit,
    private val smallVersion: Boolean,
    private val context: Context?,
    private val forTheLobby : Boolean // needed just for the use of this adapter in the lobby

) : RecyclerView.Adapter<DecksAdapter.MyViewHolder>() {

    // List we will fill up, that the RecyclerView will reference
    private var list: MutableList<DecksModel> = arrayListOf()

    private var selectedItemPosition: Int = RecyclerView.NO_POSITION
    private var defaultDrawable : Drawable? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = DeckListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun getItems() : MutableList<DecksModel> {
        return list.toMutableList()
    }

    fun updateList(list: MutableList<DecksModel>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(private val binding: DeckListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DecksModel){
            // Sets the respective text views to the item's properties
            binding.deckName.text = item.name
            binding.cardAmount.text = "Card Deck"

            if (defaultDrawable == null) {
                defaultDrawable =  binding.itemLayout.background
            }

            // if adapter used in the lobby activity, show which deck was selected
            if (forTheLobby) {
                // set the background colour of selected view to show which is selected
                if (adapterPosition == selectedItemPosition) {
                    val pressedColor = context?.obtainStyledAttributes(intArrayOf(android.R.attr.colorControlHighlight))
                    val color = pressedColor?.getColor(0, 0)

                    binding.itemLayout.setBackgroundColor(color ?: 0)
                } else {
                    // set the default background color
                    // source: https://stackoverflow.com/questions/3671357/reseting-the-background-color-of-a-view
                    binding.itemLayout.background = defaultDrawable
                }

            }


            // source: help from CHATGPT on how to set on click listener to trigger a colour change
            binding.itemLayout.setOnClickListener {
                if (forTheLobby) {
                    // notify to reset the background color of the previously selected item
                    notifyItemChanged(selectedItemPosition)

                    // notify to set the selected view to a
                    selectedItemPosition = adapterPosition
                    notifyItemChanged(selectedItemPosition)
                }

                onItemClicked.invoke(adapterPosition, item)
            }

            if (smallVersion) {
                binding.deleteBtn.visibility = View.GONE
            } else {
                binding.deleteBtn.setOnClickListener {
                    onDeleteClicked.invoke(
                        adapterPosition,
                        item
                    )
                }
            }
        }
    }
}