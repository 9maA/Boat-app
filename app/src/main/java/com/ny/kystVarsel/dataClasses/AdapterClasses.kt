package com.ny.kystVarsel.dataClasses
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Sted (
        @PrimaryKey var tittel: String,
        @ColumnInfo(name = "lat") val lat: String,
        @ColumnInfo(name = "long") val lng: String): Parcelable


@Parcelize
data class StedInfo(val tittel: String?,val bolgeHoyde: String?,val vindRetning: String?,
                    val vindStyrke: String?, var nearmeFarer: MutableList<Info?>) : Parcelable

data class Fare(val tekst: String?, val type: String?)
data class Nod(val tittel: String?, val nr: String?, val tid: String?)