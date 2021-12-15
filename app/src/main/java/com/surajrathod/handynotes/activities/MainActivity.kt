package com.surajrathod.handynotes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.surajrathod.handynotes.R
import com.surajrathod.handynotes.databinding.ActivityMainBinding
import com.surajrathod.handynotes.db.NoteDatabase
import com.surajrathod.handynotes.repository.NoteRepository
import com.surajrathod.handynotes.viewModel.NoteActivityViewModel
import com.surajrathod.handynotes.viewModel.NoteActivityViewModelFactory

class MainActivity : AppCompatActivity() {


    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)


        try {
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteActivityViewModelFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(this,noteActivityViewModelFactory)[NoteActivityViewModel::class.java]
        }catch (e : Exception)
        {
            Log.d("TAG","Error")
        }
    }
}

