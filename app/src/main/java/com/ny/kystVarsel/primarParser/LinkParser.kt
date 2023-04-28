package com.ny.kystVarsel.primarParser

import android.util.Xml
import com.ny.kystVarsel.dataClasses.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

private val ns: String? = null

class LinkParser {
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Info? {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): Info?  {
        var info: Info? = null

        parser.require(XmlPullParser.START_TAG, ns, "alert")
        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "info") {
                info = readEntry(parser)
                break //veldig cheap fiks, vi vil bare ha det første info objektet som er på norsk, fortsetter vi
                //å loope får vi det engelske objektet
            } else {
                skip(parser)
            }
        }
        return info
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEntry(parser: XmlPullParser): Info {
        parser.require(XmlPullParser.START_TAG, ns, "info")

        val parameterList = mutableListOf<Parameter>()
        var severity: String? = null
        var expires: String? = null
        var area: Area? = null
        var certainty: String? = null
        var description: String? = null
        var language: String?= null
        var onset: String? = null
        var eventCode: EventCode?= null
        var effective: String?= null
        var responseType: String?= null
        var senderName: String?= null
        var urgency: String?= null
        var web: String?= null
        var instruction: String?= null
        var category: String?= null
        var event: String? = null
        var headline: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "parameter" -> parameterList.add(readParameter(parser))
                "severity" -> severity = readIn(parser,"severity")
                "expires" -> expires = readIn(parser,"expires")
                "area" -> area = readArea(parser)
                "certainty" -> certainty = readIn(parser,"certainty")
                "description" -> description = readIn(parser,"description")
                "language" -> language = readIn(parser,"language")
                "onset" -> onset = readIn(parser,"onset")
                "eventCode" -> eventCode = readEvent(parser)
                "effective" -> effective = readIn(parser,"effective")
                "responseType" -> responseType= readIn(parser,"responseType")
                "senderName" -> senderName= readIn(parser,"senderName")
                "urgency" -> urgency = readIn(parser,"urgency")
                "web" -> web = readIn(parser,"web")
                "instruction" -> instruction = readIn(parser,"instruction")
                "category" -> category = readIn(parser,"category")
                "event" -> event = readIn(parser,"event")
                "headline" -> headline = readIn(parser,"headline")
                else -> skip(parser)
            }
        }
        return Info(severity,expires,certainty,description
            ,language, onset, eventCode,effective,
            responseType,senderName,
            urgency,web,instruction,
            parameterList,area,category,event,headline)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readIn(parser: XmlPullParser, type:String): String {
        parser.require(XmlPullParser.START_TAG, ns, type)
        val t = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, type)
        return t
    }

    private fun readArea(parser: XmlPullParser): Area {
        parser.require(XmlPullParser.START_TAG, ns, "area")
        var areaDesc: String? = null
        var polygon: String? = null
        var altidude: String? = null
        var ceiling: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "areaDesc" -> areaDesc = readIn(parser, "areaDesc")
                "polygon" -> polygon = readIn(parser,"polygon")
                "altitude" -> altidude = readIn(parser,"altitude")
                "ceiling" -> ceiling = readIn(parser,"ceiling")
                else -> skip(parser)
            }
        }
        return Area(areaDesc,polygon,altidude,ceiling)
    }

    private fun readParameter(parser: XmlPullParser): Parameter {
        parser.require(XmlPullParser.START_TAG, ns, "parameter")
        var valueName: String? = null
        var value: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "valueName" -> valueName = readIn(parser,"valueName")
                "value" -> value = readIn(parser,"value")
                else -> skip(parser)
            }
        }
        return Parameter(valueName, value)
    }

    private fun readEvent(parser: XmlPullParser): EventCode {
        parser.require(XmlPullParser.START_TAG, ns, "eventCode")
        var valueName: String? = null
        var value: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "valueName" -> valueName = readIn(parser,"valueName")
                "value" -> value = readIn(parser,"value")
                else -> skip(parser)
            }
        }
        return EventCode(valueName, value)
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