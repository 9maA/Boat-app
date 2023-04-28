package com.ny.kystVarsel.primarParser

import android.util.Xml
import com.ny.kystVarsel.dataClasses.Channel
import com.ny.kystVarsel.dataClasses.Item
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

private val ns: String? = null
class GeneralParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Channel? {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): Channel? {
        var ch: Channel? = null

        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "channel") {
                ch = readEntry(parser)
            } else {
                skip(parser)
            }
        }
        return ch
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEntry(parser: XmlPullParser): Channel {
        parser.require(XmlPullParser.START_TAG, ns, "channel")

        val itemList = mutableListOf<Item>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "item" -> itemList.add(readItem(parser))
                else -> skip(parser)
            }
        }
        return Channel(itemList)
    }

    private fun readItem(parser: XmlPullParser): Item {
        parser.require(XmlPullParser.START_TAG, ns, "item")
        var author: String? = null
        var link: String? = null
        var description: String? = null
        var guid: String? = null
        var title: String? = null
        var category: String? = null
        var pubDate: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTitle(parser)
                "description" -> description = readDesc(parser)
                "link" -> link = readLink(parser)
                "author" -> author = readAuth(parser)
                "category" -> category = readCat(parser)
                "guid" -> guid = readGuid(parser)
                "pubDate" -> pubDate = readPub(parser)

                else -> skip(parser)
            }
        }
        return Item(author, link, description, guid, title, category, pubDate)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return title
    }

    private fun readDesc(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "description")
        val description = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "description")
        return description
    }

    private fun readLink(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "link")
        val link = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "link")
        return link
    }

    private fun readAuth(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "author")
        val author = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "author")
        return author
    }

    private fun readCat(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "category")
        val cat = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "category")
        return cat
    }

    private fun readGuid(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "guid")
        val guid = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "guid")
        return guid
    }

    private fun readPub(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate")
        val date = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "pubDate")
        return date
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