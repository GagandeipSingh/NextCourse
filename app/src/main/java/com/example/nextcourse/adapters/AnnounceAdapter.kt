package com.example.nextcourse.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.example.nextcourse.domains.AnnounceDomain2
import com.example.nextcourse.R
import com.example.nextcourse.activities.AnnounceDescView
import com.example.nextcourse.databinding.ViewholderAnnounceBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AnnounceAdapter(var context : Context, private var list:ArrayList<AnnounceDomain2>, private var isSameUser : Boolean) : RecyclerView.Adapter<AnnounceAdapter.ViewHolder>(){
    class ViewHolder(var binding: ViewholderAnnounceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderAnnounceBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.Heading.text = list[position].heading
        holder.binding.adate.text = String.format("Added on : %s",list[position].adate)
        holder.binding.ImageAhead.setImageResource(R.drawable.bookmark)

        holder.itemView.setOnClickListener{
            val intent = Intent(context, AnnounceDescView::class.java)
            intent.putExtra("Heading",list[position].heading)
            intent.putExtra("Description",list[position].description)
            intent.putExtra("Date",list[position].adate)
            intent.putExtra("keyRef",list[position].keyRef)
            intent.putExtra("AnnounceKey",list[position].akey)
            context.startActivity(intent)
        }
        holder.binding.options.setOnClickListener {
            holder.adapterPosition.toString()
            val pos = holder.adapterPosition
            optionsMenu(it,pos,holder)
        }
    }
    private fun optionsMenu(view: View?, pos:Int ,holder: ViewHolder) {
        val optionsMenu = PopupMenu(context,view,Gravity.END)
        optionsMenu.inflate(R.menu.more_options)
        if(holder.binding.imp.visibility == View.INVISIBLE){
            val uimpItem = optionsMenu.menu.findItem(R.id.action_unimp)
            uimpItem.setVisible(false)
        }
        else{
            val impItem = optionsMenu.menu.findItem(R.id.action_important)
            impItem.setVisible(false)
        }
        val item1 = optionsMenu.menu.findItem(R.id.action_unenrol)
        item1?.setVisible(false)
        val item2 = optionsMenu.menu.findItem(R.id.action_announcement)
        item2?.setVisible(false)
        optionsMenu.setOnMenuItemClickListener {
            val keyRef = list[pos].keyRef
            val aKey = list[pos].akey
            when (it.itemId) {
                R.id.action_share -> {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_TEXT, "Announcement:\n${list[pos].heading}\nDescription:\n${list[pos].description}")
                    intent.type = "text/plain"
                    context.startActivity(Intent.createChooser(intent, "Share: "))
                    return@setOnMenuItemClickListener true
                }
                R.id.action_important -> {
                    holder.binding.imp.visibility = View.VISIBLE
                    return@setOnMenuItemClickListener true
                }
                R.id.action_unimp -> {
                    holder.binding.imp.visibility = View.INVISIBLE
                    return@setOnMenuItemClickListener true
                }
                R.id.action_edit -> {
                    val dialog = Dialog(context)
                    dialog.setContentView(R.layout.announce_bg)
                    val etHeading = dialog.findViewById<EditText>(R.id.etHeading)
                    val tilHeading = dialog.findViewById<TextInputLayout>(R.id.tilHeading)
                    val etDescription = dialog.findViewById<EditText>(R.id.etDescription)
                    val tilDescription = dialog.findViewById<TextInputLayout>(R.id.tilDescription)
                    etHeading.setText(list[pos].heading)
                    etDescription.setText(list[pos].description)
                    etHeading.doOnTextChanged { _, _, _, _ -> tilHeading.isErrorEnabled = false }
                    etDescription.doOnTextChanged { _, _, _, _ -> tilDescription.isErrorEnabled = false }
                    dialog.findViewById<Button>(R.id.saveButton).setOnClickListener {
                        if(etHeading.text.trim().isEmpty()){
                            tilHeading.error = "Enter Heading.."
                        }
                        else if(etDescription.text.trim().isEmpty()){
                            tilDescription.error = "Enter Description.."
                        }
                        else{
                            val announceReference = FirebaseDatabase.getInstance().getReference("Classes").child("$keyRef/Announcements/$aKey")
                            announceReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val announcementUpdates = HashMap<String, Any>()
                                    announcementUpdates["heading"] = etHeading.text.trim().toString()
                                    announcementUpdates["description"] = etDescription.text.trim().toString()
                                    announceReference.updateChildren(announcementUpdates)  // Update only specific fields
                                    dialog.dismiss()
                                    Toast.makeText(context,"Announcement Updated..",Toast.LENGTH_SHORT).show()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle error
                                }
                            })
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
                        val databaseReference = FirebaseDatabase.getInstance().getReference("Classes").child("$keyRef/Announcements/$aKey/Files")
                        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (userSnapshot in dataSnapshot.children) {
                                    for (fileSnapshot in userSnapshot.children) {
                                        val uri = fileSnapshot.child("uri").getValue(String::class.java).toString()
                                        val storage = FirebaseStorage.getInstance()
                                        val storageRef = storage.getReferenceFromUrl(uri)
                                        storageRef.delete()
                                    }
                                }
                                val announceReference = FirebaseDatabase.getInstance().getReference("Classes").child("$keyRef/Announcements/$aKey")
                                announceReference.removeValue()
                                Toast.makeText(context,"Announcement Deleted..", Toast.LENGTH_SHORT).show()
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
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
        if(isSameUser){
            optionsMenu.show()
        }
        else{
            val hideItem1 = optionsMenu.menu.findItem(R.id.action_edit)
            hideItem1?.setVisible(false)
            val hideItem2 = optionsMenu.menu.findItem(R.id.action_delete)
            hideItem2?.setVisible(false)
            optionsMenu.show()
        }
    }
}