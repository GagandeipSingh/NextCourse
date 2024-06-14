package com.example.nextcourse

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nextcourse.databinding.Viewholder2Binding

class Adapter2(private var dataList: ArrayList<Domain2>, var context : Context) : RecyclerView.Adapter<Adapter2.ViewHolder>() {
    class ViewHolder(var binding: Viewholder2Binding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = Viewholder2Binding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.ImageAhead.setImageResource(R.drawable.filelogo)
        holder.binding.ImageBehind.setImageResource(dataList[position%6].picBg)
        holder.binding.Filename.text = (dataList[position].fileName)
        holder.binding.AssignDate.text = String.format("A: %s", dataList[position].assignDate)
        holder.binding.LastDate.text = String.format("L: %s", dataList[position].lastDate)
        dataList[position].fileName
        val fileUri = dataList[position].uri
        holder.itemView.setOnClickListener{
            val intent = Intent(context,ViewPdf::class.java)
            intent.putExtra("fileUri",fileUri)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}