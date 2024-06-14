package com.example.nextcourse

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nextcourse.databinding.Viewholder1Binding

class Adapter1(private var dataList: ArrayList<Domain1>, var context : Context) : RecyclerView.Adapter<Adapter1.ViewHolder>() {
    class ViewHolder(var binding: Viewholder1Binding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = Viewholder1Binding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.ImageAhead.setImageResource(dataList[position%6].picPath)
        holder.binding.ImageBehind.setImageResource(dataList[position%6].picBg)
        holder.binding.Classtitle.text = (dataList[position].classTitle)
        holder.binding.Section.text = (dataList[position].section)
        holder.binding.Subject.text = (dataList[position].subject)
        holder.itemView.setOnClickListener{
            val intent = Intent(context,FilesView::class.java)
            intent.putExtra("classRef",dataList[position].key)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: MutableList<Domain1>) {
        this.dataList.clear()
        this.dataList.addAll(newData)
        notifyDataSetChanged()
    }

}