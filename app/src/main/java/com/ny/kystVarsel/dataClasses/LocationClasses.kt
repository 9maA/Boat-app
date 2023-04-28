package com.ny.kystVarsel.dataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
//til ocean forecast api

@Parcelize
data class TimePeriod(val begin: String?, val end: String?, val id: String?) : Parcelable

@Parcelize
data class OceanForecast(val significantTotalWaveHeight: SignificantTotalWaveHeight?,
                         val meanTotalWaveDirection: MeanTotalWaveDirection?, val seaTemperature: SeaTemperature?,
                         val validTime: ValidTime?): Parcelable

@Parcelize
data class MeanTotalWaveDirection(val uom: String?, val content: String?): Parcelable
@Parcelize
data class SeaTemperature(val uom: String?, val content: String?): Parcelable

@Parcelize
data class SignificantTotalWaveHeight(val uom: String?, val content: String?): Parcelable

@Parcelize
data class ValidTime(val TimePeriod: TimePeriod?): Parcelable

//til temperatur
@Parcelize
data class Location(val latitude: String?,
                    val cloudiness: Cloudiness?,
                    val temperature: Temperature?,
                    val humidity: Humidity?,
                    val windDirection: WindDirection?,
                    val windSpeed: WindSpeed?, val longitude: String?, val fog: Fog?,
                    val symbol: Symbol?): Parcelable

@Parcelize
data class Product(val time: List<Time>?): Parcelable
@Parcelize
data class Time(val from: String?,
                 val location: Location?, val to: String?): Parcelable
@Parcelize
data class Temperature(val unit: String?, val value: String?): Parcelable

@Parcelize
data class WindDirection(val deg: String?): Parcelable

@Parcelize
data class WindSpeed(val mps: String?, val name: String?,val beaufort: String?): Parcelable

@Parcelize
data class Fog(val percent: String?): Parcelable

@Parcelize
data class Humidity(val unit: String?, val value: String?): Parcelable

@Parcelize
data class Cloudiness( val percent: String?): Parcelable

@Parcelize
data class Symbol(val id: String?, val number: String?, val code: String?): Parcelable