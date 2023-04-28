package com.ny.kystVarsel.primarParser

import android.util.Xml
import com.ny.kystVarsel.dataClasses.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

private var ns: String? = null

class OceanParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): MutableList<OceanForecast> {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): MutableList<OceanForecast> {

        val oceList = mutableListOf<OceanForecast>()
        parser.require(XmlPullParser.START_TAG, ns,"mox:Forecasts")

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "mox:forecast") {
                oceList.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }
        return oceList

    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEntry(parser: XmlPullParser): OceanForecast {
        parser.require(XmlPullParser.START_TAG, ns, "mox:forecast")

        var oceanForecast: OceanForecast? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "metno:OceanForecast" -> oceanForecast = readOcean(parser)
                else -> skip(parser)
            }
        }
        return oceanForecast!!
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readOcean(parser: XmlPullParser): OceanForecast {
        var significantTotalWaveHeight: SignificantTotalWaveHeight? = null
        var meanTotalWaveDirection: MeanTotalWaveDirection? = null
        var seaTemperature: SeaTemperature? = null
        var validTime: ValidTime? = null


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "mox:significantTotalWaveHeight" -> significantTotalWaveHeight  = readHeight(parser)
                "mox:meanTotalWaveDirection" -> meanTotalWaveDirection = readDirection(parser)
                "mox:seaTemperature" -> seaTemperature = readTemp(parser)
                "mox:validTime" -> validTime = readValid(parser)
                else -> skip(parser)
            }
        }
        return OceanForecast(significantTotalWaveHeight,meanTotalWaveDirection,seaTemperature,validTime)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readHeight(parser: XmlPullParser): SignificantTotalWaveHeight {

        parser.require(XmlPullParser.START_TAG, ns, "mox:significantTotalWaveHeight")
        val uom: String? = parser.getAttributeValue(null, "uom")
        val content: String = readText(parser)

        parser.require(XmlPullParser.END_TAG, ns, "mox:significantTotalWaveHeight")
        return SignificantTotalWaveHeight(uom,content)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDirection(parser: XmlPullParser): MeanTotalWaveDirection {

        parser.require(XmlPullParser.START_TAG, ns, "mox:meanTotalWaveDirection")
        val uom: String? = parser.getAttributeValue(null, "uom")
        val content: String = readText(parser)

        parser.require(XmlPullParser.END_TAG, ns, "mox:meanTotalWaveDirection")

        return MeanTotalWaveDirection(uom,content)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTemp(parser: XmlPullParser): SeaTemperature {

        parser.require(XmlPullParser.START_TAG, ns, "mox:seaTemperature")
        val uom: String? = parser.getAttributeValue(null, "uom")
        val content: String = readText(parser)

        parser.require(XmlPullParser.END_TAG, ns, "mox:seaTemperature")

        return SeaTemperature(uom,content)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readValid(parser: XmlPullParser): ValidTime {

        parser.require(XmlPullParser.START_TAG, ns, "mox:validTime")

        var timePeriod: TimePeriod? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "gml:TimePeriod" -> timePeriod = readTime(parser)
                else -> skip(parser)
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "mox:validTime")
        return ValidTime(timePeriod)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTime(parser: XmlPullParser): TimePeriod {
        parser.require(XmlPullParser.START_TAG, ns, "gml:TimePeriod")

        val id: String? = parser.getAttributeValue(null, "gml:id")
        var begin: String? = null
        var end: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "gml:begin" -> begin = readText(parser)
                "gml:end" -> end = readText(parser)
                else -> skip(parser)
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "gml:TimePeriod")

        return TimePeriod(begin,end,id)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}