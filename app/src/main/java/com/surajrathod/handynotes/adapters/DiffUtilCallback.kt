package com.surajrathod.handynotes.adapters

import androidx.recyclerview.widget.DiffUtil
import com.surajrathod.handynotes.model.Note

class DiffUtilCallback : DiffUtil.ItemCallback<Note>(){

    //content id are same
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    //content is changed
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id== newItem.id
    }
}