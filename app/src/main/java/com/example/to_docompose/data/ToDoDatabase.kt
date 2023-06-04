package com.example.to_docompose.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.to_docompose.data.models.TodoTask

@Database(entities = [TodoTask::class], version = 1, exportSchema = false)
abstract class ToDoDatabase : RoomDatabase() {

    abstract fun toDoDao(): TodoDao

}