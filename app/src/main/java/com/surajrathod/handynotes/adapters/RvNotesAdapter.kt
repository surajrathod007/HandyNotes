package com.surajrathod.handynotes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.surajrathod.handynotes.R
import com.surajrathod.handynotes.databinding.NoteItemLayoutBinding
import com.surajrathod.handynotes.fragments.NoteFragmentDirections
import com.surajrathod.handynotes.model.Note
import com.surajrathod.handynotes.utils.hidekeyboard
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak
import java.util.*


/*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.surajrathod.handynotes.R

import com.surajrathod.handynotes.databinding.NoteItemLayoutBinding
import com.surajrathod.handynotes.model.Note
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak
import java.text.FieldPosition */


//this is ListAdapter not recycler view adapter
class RvNotesAdapter : ListAdapter<Note, RvNotesAdapter.NotesViewHolder>(DiffUtilCallback()) {
    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val contentBinding =
            NoteItemLayoutBinding.bind(itemView) //in recyclerview databiindnig

        val title: MaterialTextView = contentBinding.noteItemTitle
        val content: TextView = contentBinding.noteContentItem
        val date: MaterialTextView = contentBinding.noteDate
        val parent: MaterialCardView = contentBinding.noteItemLayoutParent
        val markWon = Markwon.builder(itemView.context).usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)

                    builder.on(
                        SoftLineBreak::class.java
                    ) { visitor, _ -> visitor.forceNewLine() }
                }
            }).build()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.note_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        getItem(position).let { note ->

            holder.apply {

                parent.transitionName = "recyclerView_2${note.id}"
                title.text = note.title
                markWon.setMarkdown(content,note.content)
                date.text= note.date
                parent.setCardBackgroundColor(note.color)


                //when note is clicked then pass it to edit or update note fragment
                itemView.setOnClickListener {

                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment()
                        .setNote(note) //because we already seted argument in nav graph so , we can easy pass object

                    val extras = FragmentNavigatorExtras(parent to "recyclerView_3${note.id}")
                    it.hidekeyboard()
                    Navigation.findNavController(it).navigate(action,extras)

                }

                //same as above
                content.setOnClickListener {

                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment()
                        .setNote(note) //because we already seted argument in nav graph so , we can easy pass object

                    val extras = FragmentNavigatorExtras(parent to "recyclerview_${note.id}")
                    it.hidekeyboard()
                    Navigation.findNavController(it).navigate(action,extras)
                }
            }
        }
    }


}