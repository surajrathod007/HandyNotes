package com.surajrathod.handynotes.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.surajrathod.handynotes.model.Note


//database setup and configration
@Database(
    entities = [Note::class], //what type of data will stored
    version = 1, //version to check & track databse design
    exportSchema = false //save previous database in case of data lose
)

abstract class NoteDatabase: RoomDatabase(){

    abstract fun getNoteDao() : DAO //its useful to acces DAO of note

    companion object
    {
        @Volatile
        private var instance : NoteDatabase? = null
        private val LOCK = Any()  //only one thread can use this database at one time


        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){

            instance ?: createDatabase(context).also {
                instance = it
            }
        }


        private fun createDatabase(context : Context) = Room.databaseBuilder(
            context.applicationContext,
            NoteDatabase::class.java,
            "note_database"
        ).build()

    }
}