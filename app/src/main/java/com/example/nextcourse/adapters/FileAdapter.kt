package com.example.nextcourse.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.nextcourse.domains.FileDomain1
import com.example.nextcourse.domains.FileDomain2
import com.example.nextcourse.R
import com.example.nextcourse.databinding.Viewholder2Binding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class FileAdapter(private var dataList: ArrayList<FileDomain1>, var context : Context, private var isSameUser:Boolean) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    class ViewHolder(var binding: Viewholder2Binding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = Viewholder2Binding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.ImageAhead.setImageResource(R.drawable.filelogo)
        holder.binding.Filename.text = (dataList[position].fileName)
        holder.binding.Assigner.text = (dataList[position].assigner)
        holder.binding.uploader.text = String.format("Uploaded By : %s", dataList[position].uploader)
        holder.binding.date.text = String.format("Uploaded On : %s", dataList[position].date)
        dataList[position].fileName
        val fileUri = dataList[position].uri
        if(!isSameUser){
            if(holder.binding.Assigner.text.toString() == "(Assigner)"){
                holder.binding.options.visibility = View.INVISIBLE
            }
        }
        holder.itemView.setOnClickListener{
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(fileUri), "application/pdf")
                context.startActivity(intent)

        }
        holder.binding.options.setOnClickListener {
            val pos = holder.adapterPosition
            holder.adapterPosition.toString()
            optionsMenu(it,pos)
        }
    }

    private fun optionsMenu(view: View?,pos:Int) {
        val optionsMenu = PopupMenu(context,view,Gravity.END)
        optionsMenu.inflate(R.menu.more_options)
        val uimpItem = optionsMenu.menu.findItem(R.id.action_unimp)
        uimpItem.setVisible(false)
        val item1 = optionsMenu.menu.findItem(R.id.action_unenrol)
        item1?.setVisible(false)
        val item2 = optionsMenu.menu.findItem(R.id.action_important)
        item2?.setVisible(false)
        val item3 = optionsMenu.menu.findItem(R.id.action_share)
        item3?.setVisible(false)
        val item4 = optionsMenu.menu.findItem(R.id.action_announcement)
        item4?.setVisible(false)
        optionsMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit ->{
                    val dialog = Dialog(context)
                    dialog.setContentView(R.layout.file_bg)
                    val etFileName = dialog.findViewById<EditText>(R.id.fileName)
                    val tilFile = dialog.findViewById<TextInputLayout>(R.id.tilFileName)
                    etFileName.setText(dataList[pos].fileName)
                    dialog.findViewById<Button>(R.id.saveButton).setOnClickListener {
                        etFileName.doOnTextChanged { _, _, _, _ -> tilFile.isErrorEnabled = false }
                        if(etFileName.text.trim().isEmpty()){
                            tilFile.error = "Enter File Name.."
                        }
                        else{
                            val database = FirebaseDatabase.getInstance()
                            val fileKey = dataList[pos].fileKey
                            val indexOfClasses = fileKey.indexOf("Classes/")
                            val keyValue = fileKey.substring(indexOfClasses + "Classes/".length)
                            val myRef = database.getReference("Classes").child(keyValue)
                            myRef.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val oldFileInfo = dataSnapshot.getValue(FileDomain2::class.java)
                                    oldFileInfo!!.fileName = etFileName.text.trim().toString()
                                    myRef.setValue(oldFileInfo)
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            }
                            )
                            dialog.dismiss()
                            Toast.makeText(context,"File Name Updated..",Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.show()
                    return@setOnMenuItemClickListener true
                }
                R.id.action_delete -> {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Confirmation!")
                    alertDialog.setMessage("Do you want to Delete..")
                    alertDialog.setCancelable(false)
                    alertDialog.setPositiveButton("Yes"){_,_->
                        val uri = dataList[pos].uri
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.getReferenceFromUrl(uri)
                        storageRef.delete()
                            .addOnSuccessListener {
                                val database = FirebaseDatabase.getInstance()
                                val fileKey = dataList[pos].fileKey
                                val indexOfClasses = fileKey.indexOf("Classes/")
                                val keyValue = fileKey.substring(indexOfClasses + "Classes/".length)
                                val myRef = database.getReference("Classes").child(keyValue)
                                myRef.removeValue()
                                Toast.makeText(context,"File Deleted..",Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                // Handle any errors
                                println("Error deleting file: ${exception.message}")
                            }
                    }
                    alertDialog.setNegativeButton("No"){_,_->
                    }
                    alertDialog.show()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
        optionsMenu.show()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}