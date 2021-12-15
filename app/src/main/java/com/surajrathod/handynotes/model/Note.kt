package com.surajrathod.handynotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Note(


    @PrimaryKey(autoGenerate = true) //automatic generate primary key
    var id : Int = 0,
    val title : String,
    val content : String,
    val date : String,
    val color : Int = -1,
) : Serializable //using this we can transfer entire this class object to , one fragment to another ,sounds good ;)
