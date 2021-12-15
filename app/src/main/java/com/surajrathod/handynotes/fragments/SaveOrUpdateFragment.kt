package com.surajrathod.handynotes.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import com.surajrathod.handynotes.R
import com.surajrathod.handynotes.activities.MainActivity
import com.surajrathod.handynotes.databinding.BottomSheetLayoutBinding
import com.surajrathod.handynotes.databinding.FragmentNoteBinding
import com.surajrathod.handynotes.databinding.FragmentSaveOrUpdateBinding
import com.surajrathod.handynotes.model.Note
import com.surajrathod.handynotes.utils.hidekeyboard
import com.surajrathod.handynotes.viewModel.NoteActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class SaveOrUpdateFragment : Fragment(R.layout.fragment_save_or_update) {

    private lateinit var navController: NavController
    private lateinit var result: String
    private lateinit var contentBinding: FragmentSaveOrUpdateBinding
    private var note: Note? = null
    private var color = -1
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main) //for coroutines
    private val args: SaveOrUpdateFragmentArgs by navArgs() //for transfering updating note, we defined inn graph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //creating anmition
        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L
        }
        sharedElementEnterTransition = animation //applying animation while entering
        sharedElementReturnTransition = animation //applying animation while exiting

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentSaveOrUpdateBinding.bind(view)


        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity




        ViewCompat.setTransitionName(
            contentBinding.noteContentFragmentParent,"recyclerView_1${args.note?.id}"
        )

        contentBinding.saveNote.setOnClickListener {
            saveNote()

        }

        try {

            contentBinding.etNoteContent.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    contentBinding.bottomBar.visibility = View.VISIBLE
                    contentBinding.etNoteContent.setStylesBar(contentBinding.styleBar)

                } else {
                    contentBinding.bottomBar.visibility = View.GONE
                }

            }
        } catch (e: Throwable) {
            Log.d("TAG", e.stackTrace.toString())
        }

        contentBinding.fabColorPicker.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)


            val bottomSheetView: View =
                layoutInflater.inflate(R.layout.bottom_sheet_layout, null)


            //for showing the bottom sheet view
            with(bottomSheetDialog)
            {
                setContentView(bottomSheetView)
                show()
            }
            val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)
            bottomSheetBinding.apply {
                colorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener { value ->
                        color = value

                        //content binding means in this block we can acces all views in one fuction
                        contentBinding.apply {
                            noteContentFragmentParent.setBackgroundColor(color)
                            toolBarFragmentNoteContent.setBackgroundColor(color)
                            bottomBar.setBackgroundColor(color)
                            activity.window.statusBarColor = color
                        }
                        bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)
                    }
                }

                bottomSheetParent.setCardBackgroundColor(color)
            }
            bottomSheetView.post {
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }


        contentBinding.backBtn.setOnClickListener {
            requireView().hidekeyboard()
            navController.popBackStack()
        }

        //opens with existing note item
        setUpNote()


    }

    private fun setUpNote() {
        val note = args.note
        val title = contentBinding.etTitle
        val content = contentBinding.etNoteContent
        val lastEdited = contentBinding.lastEdited

        if (note == null) {
            contentBinding.lastEdited.text =
                getString(R.string.edited_on, SimpleDateFormat.getDateInstance().format(Date()))
        }

        if (note != null) {
            title.setText(note.title)
            content.renderMD(note.content)
            lastEdited.text = getString(R.string.edited_on, note.date)
            color = note.color
            contentBinding.apply {
                job.launch {
                    delay(10)
                    noteContentFragmentParent.setBackgroundColor(color)
                }

                toolBarFragmentNoteContent.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)

            }

            activity?.window?.statusBarColor = note.color
        }
    }

    private fun saveNote() {
        if (contentBinding.etNoteContent.text.toString()
                .isEmpty() || contentBinding.etTitle.toString().isEmpty()
        ) {

            Toast.makeText(activity, "Something is empty", Toast.LENGTH_SHORT).show()
        } else {

            //save note

            note = args.note

            when (note) {
                null -> {
                    noteActivityViewModel.saveNote(
                        Note(
                            0,
                            contentBinding.etTitle.text.toString(),
                            contentBinding.etNoteContent.getMD(),
                            currentDate,
                            color
                        )
                    )

                    result = "Note Saved"
                    setFragmentResult("key", bundleOf("bundleKey" to result))

                    navController.navigate(SaveOrUpdateFragmentDirections.actionSaveOrUpdateFragmentToNoteFragment())
                }

                else -> {
                    //update our note

                    updateNote()
                    navController.popBackStack()
                }


            }
        }
    }

    private fun updateNote() {
        if (note != null) {
            noteActivityViewModel.updateNote(
                Note(
                    note!!.id,
                    contentBinding.etTitle.text.toString(),
                    contentBinding.etNoteContent.getMD(),
                    currentDate,
                    color
                )
            )
        }
    }


}

