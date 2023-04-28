package com.ny.kystVarsel.primarParser

import android.util.Xml
import com.ny.kystVarsel.dataClasses.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

private var ns: String? = null
class TempParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Product? {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): Product? {

        parser.require(XmlPullParser.START_TAG, ns,"weatherdata")
        var prod: Product? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "product") {
                prod = readEntry(parser)
            } else {
                skip(parser)
            }
        }
        return prod

    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEntry(parser: XmlPullParser): Product {
        parser.require(XmlPullParser.START_TAG, ns, "product")

        val timeList = mutableListOf<Time>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "time" ->  timeList.add(readTime(parser))
                else -> skip(parser)
            }
        }
        return(Product(timeList))
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTime(parser: XmlPullParser): Time {
        parser.require(XmlPullParser.START_TAG, ns, "time")
        val from: String?= parser.getAttributeValue(null, "from")
        val to: String?= parser.getAttributeValue(null, "to")
        var lokasjon: Location? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "location" -> lokasjon = readLocation(parser)
                else -> skip(parser)
            }
        }
        return Time(from, lokasjon, to)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLocation(parser: XmlPullParser): Location {
        var cloudiness: Cloudiness?= null
        var temperature: Temperature?= null
        var humidity: Humidity?= null
        var windDirection: WindDirection?= null
        var windSpeed: WindSpeed?= null
        var fog: Fog? = null
        var symbol: Symbol? = null

        val longitude: String?= parser.getAttributeValue(null, "longitude")
        val latitude: String?= parser.getAttributeValue(null, "latitude")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "temperature" -> temperature  = readTemp(parser)
                "cloudiness" -> cloudiness = readCloud(parser)
                "humidity" -> humidity = readHum(parser)
                "windDirection" -> windDirection = readWindDir(parser)
                "windSpeed" -> windSpeed = readWindSpeed(parser)
                "fog" -> fog = readFog(parser)
                //Test Malin
                "symbol" -> symbol = readSymbol(parser)

                else -> skip(parser)
            }
        }
        return Location(latitude,cloudiness,temperature,humidity,windDirection,windSpeed,longitude,fog, symbol)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTemp(parser: XmlPullParser): Temperature {
        parser.require(XmlPullParser.START_TAG, ns, "temperature")

        val unit: String? = parser.getAttributeValue(null, "unit")
        val value: String? = parser.getAttributeValue(null, "value")

        parser.next()
        return Temperature(unit,value)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCloud(parser: XmlPullParser): Cloudiness {
        parser.require(XmlPullParser.START_TAG, ns, "cloudiness")

        val value: String? = parser.getAttributeValue(null, "percent")
        parser.next()
        return Cloudiness(value)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readFog(parser: XmlPullParser): Fog {
        parser.require(XmlPullParser.START_TAG, ns, "fog")

        val percent: String? = parser.getAttributeValue(null, "percent")

        parser.next()
        return Fog(percent)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readHum(parser: XmlPullParser): Humidity {
        parser.require(XmlPullParser.START_TAG, ns, "humidity")

        val unit: String? = parser.getAttributeValue(null, "unit")
        val value: String? = parser.getAttributeValue(null, "value")

        parser.next()
        return Humidity(unit,value)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readWindDir(parser: XmlPullParser): WindDirection {
        parser.require(XmlPullParser.START_TAG, ns, "windDirection")

        val deg: String? = parser.getAttributeValue(null, "deg")

        parser.next()
        return WindDirection(deg)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readWindSpeed(parser: XmlPullParser): WindSpeed {
        parser.require(XmlPullParser.START_TAG, ns, "windSpeed")

        val mps: String? = parser.getAttributeValue(null, "mps")
        val beufort: String? = parser.getAttributeValue(null, "beaufort")
        val name: String? = parser.getAttributeValue(null, "name")

        parser.next()
        return WindSpeed(mps,name,beufort)
    }

    //Test Malin
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readSymbol(parser: XmlPullParser): Symbol {
        parser.require(XmlPullParser.START_TAG, ns, "symbol")

        val id: String? = parser.getAttributeValue(null, "id")
        val number: String? = parser.getAttributeValue(null, "number")
        val code: String? = parser.getAttributeValue(null, "code")

        parser.next()
        return Symbol(id, number, code)
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