package com.ny.kystVarsel.dataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//den første responsen før man går på linken med detaljert
data class Item(val author: String?, val link: String?, val description: String?, val guid: String?, val title: String?,
                val category: String?, val pubDate: String?)

data class Channel(val item: List<Item>?)
//til detaljert fare
@Parcelize
data class EventCode(val valueName: String?, val value: String?) : Parcelable

@Parcelize
data class Info(val severity: String?, val expires: String?, val certainty: String?,
                val description: String?, val language: String?, val onset: String?, val eventCode: EventCode?, val effective: String?,
                val responseType: String?, val senderName: String?, val urgency: String?, val web: String?,
                val instruction: String?, val parameter: List<Parameter>?, val area: Area?,
                val category: String?, val event: String?, val headline: String?) : Parcelable

@Parcelize
data class Area(val areaDesc: String?, val polygon: String?, val altitude: String?, val ceiling: String?) : Parcelable
@Parcelize
data class Parameter(val valueName: String?, val value: String?) : Parcelable