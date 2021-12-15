package com.surajrathod.handynotes.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.surajrathod.handynotes.model.Note

@Dao
interface DAO {


    //save note
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNote(note: Note)  //we use coroutines so , made a suspend function


    //update note
    @Update
    suspend fun updateNote(note : Note)


    //read note from database
    @Query("SELECT * FROM Note ORDER BY id DESC") //query to read data from database
    fun getAllNote(): LiveData<List<Note>> //this will return live data and list of all notes, we dont use suspend because its , already perform in background thread


    //search note-> performed by user
    @Query("SELECT * FROM Note WHERE title LIKE :query OR content LIKE :query OR date LIKE :query ORDER BY id DESC") //for searching notes
    fun searchNote(query: String): LiveData<List<Note>>


    //delete a note
    @Delete
    suspend fun deleteNote(note : Note)





}