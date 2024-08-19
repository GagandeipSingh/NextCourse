package com.example.nextcourse.adapters

import android.annotation.SuppressLint
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
import com.example.nextcourse.R
import com.example.nextcourse.activities.AnnouncementsView
import com.example.nextcourse.databinding.Viewholder1Binding
import com.example.nextcourse.domains.ClassDomain2
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ClassAdapter(private var dataList: ArrayList<ClassDomain2>, var context: Context) :
    RecyclerView.Adapter<ClassAdapter.ViewHolder>() {
    class ViewHolder(var binding: Viewholder1Binding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = Viewholder1Binding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.ImageBehind.setImageResource(dataList[position % 6].picBg)
        holder.binding.Classtitle.text = (dataList[position].classTitle)
        holder.binding.creator.text = (dataList[position].creator)
        holder.binding.Section.text = (dataList[position].section)
        holder.binding.Subject.text = (dataList[position].subject)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AnnouncementsView::class.java)
            intent.putExtra("classRef", dataList[position].key)
            intent.putExtra("bg", dataList[position].picBg)
            intent.putExtra("class", dataList[position].classTitle)
            intent.putExtra("sec", dataList[position].section)
            intent.putExtra("sub", dataList[position].subject)
            context.startActivity(intent)
        }
        holder.binding.moreOptions.setOnClickListener {
            var creator = false
            val pos = holder.adapterPosition
            if (holder.binding.creator.text.toString() == "(Creator)") creator = true
            optionsMenu(it, creator, pos)
        }
    }

    private fun optionsMenu(view: View, creator: Boolean, pos: Int) {
        val optionsMenu = PopupMenu(context, view, Gravity.END)
        optionsMenu.inflate(R.menu.more_options)
        val uimpItem = optionsMenu.menu.findItem(R.id.action_unimp)
        uimpItem.setVisible(false)
        val item = optionsMenu.menu.findItem(R.id.action_important)
        item?.setVisible(false)
        if (!creator) {
            val hideItem1 = optionsMenu.menu.findItem(R.id.action_edit)
            hideItem1?.setVisible(false)
            val hideItem2 = optionsMenu.menu.findItem(R.id.action_delete)
            hideItem2?.setVisible(false)
            val hideItem3 = optionsMenu.menu.findItem(R.id.action_share)
            hideItem3?.setVisible(false)
        } else {
            val hideItem1 = optionsMenu.menu.findItem(R.id.action_unenrol)
            hideItem1?.setVisible(false)
            val hideItem2 = optionsMenu.menu.findItem(R.id.action_announcement)
            hideItem2?.setVisible(false)
        }
        optionsMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    val dialog = Dialog(context)
                    dialog.setContentView(R.layout.class_bg)
                    val etTitle = dialog.findViewById<EditText>(R.id.etTitle)
                    val tilTitle = dialog.findViewById<TextInputLayout>(R.id.tilTitle)
                    val etSection = dialog.findViewById<EditText>(R.id.etSection)
                    val tilSection = dialog.findViewById<TextInputLayout>(R.id.tilSection)
                    val etSubject = dialog.findViewById<EditText>(R.id.etSubject)
                    val tilSubject = dialog.findViewById<TextInputLayout>(R.id.tilSubject)
                    etTitle.setText(dataList[pos].classTitle)
                    etSection.setText(dataList[pos].section)
                    etSubject.setText(dataList[pos].subject)
                    etTitle.doOnTextChanged { _, _, _, _ -> tilTitle.isErrorEnabled = false }
                    etSection.doOnTextChanged { _, _, _, _ -> tilSection.isErrorEnabled = false }
                    etSubject.doOnTextChanged { _, _, _, _ -> tilSubject.isErrorEnabled = false }
                    dialog.findViewById<Button>(R.id.saveButton).setOnClickListener {
                        if(etTitle.text.trim().isEmpty()){
                            tilTitle.error = "Enter Classtitle.."
                        }
                        else if(etSection.text.trim().isEmpty()){
                            tilSection.error = "Enter Section.."
                        }
                        else if(etSubject.text.trim().isEmpty()){
                            tilSubject.error = "Enter Subject.."
                        }
                        else{
                            val announceReference = FirebaseDatabase.getInstance().getReference("Classes").child(dataList[pos].key)
                            announceReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val announcementUpdates = HashMap<String, Any>()
                                    announcementUpdates["classTitle"] = etTitle.text.trim().toString()
                                    announcementUpdates["section"] = etSection.text.trim().toString()
                                    announcementUpdates["subject"] = etSubject.text.trim().toString()
                                    announceReference.updateChildren(announcementUpdates)
                                    dialog.dismiss()
                                    Toast.makeText(context,"Class Updated..",Toast.LENGTH_SHORT).show()
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                        }
                    }
                    dialog.show()

                    return@setOnMenuItemClickListener true
                }
                R.id.action_share -> {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_TEXT, dataList[pos].key)
                    intent.type = "text/plain"
                    context.startActivity(Intent.createChooser(intent, "Share: "))
                    return@setOnMenuItemClickListener true
                }
                R.id.action_announcement -> {
                    val intent = Intent(context, AnnouncementsView::class.java)
                    intent.putExtra("classRef", dataList[pos].key)
                    intent.putExtra("bg", dataList[pos].picBg)
                    intent.putExtra("class", dataList[pos].classTitle)
                    intent.putExtra("sec", dataList[pos].section)
                    intent.putExtra("sub", dataList[pos].subject)
                    context.startActivity(intent)
                    return@setOnMenuItemClickListener true
                }
                R.id.action_unenrol -> {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Confirmation!")
                    alertDialog.setMessage("Do you want to Unenrol..")
                    alertDialog.setCancelable(false)
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        val auth = FirebaseAuth.getInstance()
                        val userId = auth.currentUser?.uid.toString()
                        val database = FirebaseDatabase.getInstance()
                        val studentsRef = database.getReference("Classes/${dataList[pos].key}/Students")
                        studentsRef.child(userId).removeValue()
                    }
                    alertDialog.setNegativeButton("No") { _, _ ->
                    }
                    alertDialog.show()
                    return@setOnMenuItemClickListener true
                }

                R.id.action_delete -> {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("Confirmation!")
                    alertDialog.setMessage("Do you want to Delete..")
                    alertDialog.setCancelable(false)
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        val databaseReference = FirebaseDatabase.getInstance().getReference()
                        databaseReference.child("Classes/${dataList[pos].key}/Announcements")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(announcementsSnapshot: DataSnapshot) {
                                    for (announcementSnapshot in announcementsSnapshot.children) {
                                        val announcementId = announcementSnapshot.key
                                        val filesReference = databaseReference.child("Classes/${dataList[pos].key}/Announcements/$announcementId/Files")
                                        filesReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                for (userSnapshot in dataSnapshot.children) {
                                                    for (fileSnapshot in userSnapshot.children) {
                                                        val uri = fileSnapshot.child("uri").getValue(String::class.java).toString()
                                                        val storage = FirebaseStorage.getInstance()
                                                        val storageRef = storage.getReferenceFromUrl(uri)
                                                        storageRef.delete()
                                                    }
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                    }
                                    val classReference = FirebaseDatabase.getInstance().getReference("Classes").child(dataList[pos].key)
                                    classReference.removeValue()
                                    Toast.makeText(context, "Class Deleted..", Toast.LENGTH_SHORT).show()
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
        }
        alertDialog.setNegativeButton("No") { _, _ ->
        }
        alertDialog.show()
        return@setOnMenuItemClickListener true
    }
    else ->
    {
        return@setOnMenuItemClickListener false
    }
}
}
optionsMenu.show()
}

override fun getItemCount(): Int {
    return dataList.size
}

@SuppressLint("NotifyDataSetChanged")
fun updateData(newData: MutableList<ClassDomain2>) {
    this.dataList.clear()
    this.dataList.addAll(newData)
    notifyDataSetChanged()
}

}