package com.lizongying.mytv0.models

import android.util.Xml
import com.lizongying.mytv0.data.EPG
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale


class EPGXmlParser {

    private val ns: String? = null

    private val epg = mutableMapOf<String, MutableList<EPG>>()

    private fun formatFTime(s: String): Int {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.getDefault())
        val date = dateFormat.parse(s)
        if (date != null) {
            return (date.time / 1000).toInt()
        }
        return 0
    }

    fun parse(inputStream: InputStream): MutableMap<String, MutableList<EPG>> {
        inputStream.use { input ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            var channel = ""
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    parser.next()
                    continue
                }
                if (parser.name == "channel") {
                    parser.nextTag()
                    channel = parser.nextText()
                    epg[channel] = mutableListOf()
                } else if (parser.name == "programme") {
                    val start = parser.getAttributeValue(ns, "start")
                    parser.nextTag()
                    val title = parser.nextText()
                    epg[channel]?.add(EPG(title, formatFTime(start)))
                }
                parser.next()
            }
        }

        return epg.toSortedMap { a, b -> b.compareTo(a) }
    }
}