package com.sahin.artbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sahin.artbook.databinding.RecyclerRowBinding

class ArtAdapter(val  list : ArrayList<Art>) : RecyclerView.Adapter<ArtAdapter.artHolder>() {

    class artHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): artHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return  artHolder(binding)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: artHolder, position: Int) {
        holder.binding.recyclerViewText.text = list.get(position).name

        holder.itemView.setOnClickListener {

            val intent = Intent(holder.itemView.context,ArtActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",list.get(position).id)
            holder.itemView.context.startActivity(intent)



        }
    }
}