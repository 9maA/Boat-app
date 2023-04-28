package com.ny.kystVarsel.dataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ny.kystVarsel.dataClasses.Sted

@Database(entities = [Sted::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stedDao(): StedDAO
}