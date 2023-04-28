package com.ny.kystVarsel.dataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ny.kystVarsel.dataClasses.Sted

@Dao
interface StedDAO {
    @Query("SELECT * FROM sted")
    fun getAll(): List<Sted>

    @Insert
    fun insert(vararg sted: Sted)

    @Delete
    fun delete(sted: Sted)

    @Query("SELECT * FROM sted WHERE tittel LIKE :t")
    fun finnLiktSted(t: String): List<Sted>
}