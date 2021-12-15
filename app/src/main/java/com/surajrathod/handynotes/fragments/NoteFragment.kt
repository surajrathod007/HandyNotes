package com.surajrathod.handynotes.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.surajrathod.handynotes.R
import com.surajrathod.handynotes.activities.MainActivity
import com.surajrathod.handynotes.adapters.RvNotesAdapter
import com.surajrathod.handynotes.databinding.FragmentNoteBinding
import com.surajrathod.handynotes.utils.SwipeToDelete
import com.surajrathod.handynotes.utils.hidekeyboard
import com.surajrathod.handynotes.viewModel.NoteActivityViewModel
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


class NoteFragment : Fragment(R.layout.fragment_note) {

    private lateinit var noteBinding: FragmentNoteBinding
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private lateinit var rvAdapter : RvNotesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //anitmation when fragment is created
        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }

        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteBinding = FragmentNoteBinding.bind(view)
        val activity = activity as MainActivity

        val navController = Navigation.findNavController(view)
        requireView().hidekeyboard() //hide key board, when user come back after deleting or updating note

        //hide action or title bar

        //we do this in background thread

        CoroutineScope(Dispatchers.Main).launch {

            delay(10)
            //activity.window.statusBarColor = Color.WHITE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9e9d9d")

        }

        noteBinding.addNoteFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment())

        }

        noteBinding.innerFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment())

        }

        recyclerViewDisplay()

        //swipe delete functionility
        swipeToDelete(noteBinding.rvNote)


        //implements search here

        noteBinding.search.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteBinding.noData.isVisible = false
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(s.toString().isNotEmpty())
                {
                    val text = s.toString()
                    val query = "%$text%" //it is send to room database , what user has typed.
                    if(query.isNotEmpty()){ //if query is not empty then , show matching result in recycler view
                        noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner)
                        {

                            rvAdapter.submitList(it)

                        }
                    }else{
                        observerDataChanges()
                    }
                }else{

                    observerDataChanges()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        noteBinding.search.setOnEditorActionListener{v,actionId, _ ->

            if(actionId==EditorInfo.IME_ACTION_SEARCH){
                v.clearFocus()
                requireView().hidekeyboard()
            }

            return@setOnEditorActionListener true
        }

        noteBinding.rvNote.setOnScrollChangeListener { _, scrollx, scrolly, _, oldScrollY ->

            when{
                scrolly>oldScrollY->{
                    noteBinding.chatFabText.isVisible = false
                }

                scrollx == scrolly->{
                    noteBinding.chatFabText.isVisible = true
                }

                else->{
                    noteBinding.chatFabText.isVisible = true
                }
            }
        }
    }

    private fun swipeToDelete(rvNote: RecyclerView) {

        val swipeToDeleteCallback=object : SwipeToDelete()
        {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition //get the position of current item

                val note = rvAdapter.currentList[position]

                var actionBtnTapped = false
                noteActivityViewModel.deleteNote(note)
                noteBinding.search.apply {
                    hidekeyboard()
                    clearFocus()
                }

                if(noteBinding.search.text.toString().isEmpty())
                {
                    observerDataChanges()
                }

                val snackBar = Snackbar.make(
                    requireView(),"Note Deleted",Snackbar.LENGTH_LONG
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){

                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {

                        transientBottomBar?.setAction("UNDO"){
                            noteActivityViewModel.saveNote(note)
                            actionBtnTapped = true
                            noteBinding.noData.isVisible = false
                        }
                        super.onShown(transientBottomBar)

                    }
                }).apply {

                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.add_note_fab) //above our note fab , this snackbar will be display
                }

                snackBar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext() ,
                        R.color.yellowOrange
                    )
                )

                snackBar.show()
            }
        }


        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback) //assign swipe funciton to item
        itemTouchHelper.attachToRecyclerView(rvNote) //attach it to recycler view
    }

    private fun observerDataChanges() {

        noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner){list->

            noteBinding.noData.isVisible = list.isEmpty() //if list is empty then No data Layout Will Be Visible
            rvAdapter.submitList(list)
        }

    }

    private fun recyclerViewDisplay() {

        when(resources.configuration.orientation)
        {
            Configuration.ORIENTATION_PORTRAIT-> setUpRecyclerView(2) //if orientaion is portrait then 2 colum
            Configuration.ORIENTATION_LANDSCAPE-> setUpRecyclerView(3) //in landscape 3 column
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {


        noteBinding.rvNote.apply {

            layoutManager = StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            rvAdapter= RvNotesAdapter()
            rvAdapter.stateRestorationPolicy= RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY //when recyclerview changes it automatically update
            adapter= rvAdapter
            postponeEnterTransition(300L,TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true

            }

        }

        observerDataChanges()

    }
}