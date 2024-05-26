package com.lizongying.mytv0


import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.lizongying.mytv0.models.TVList
import fi.iki.elonen.NanoHTTPD
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


class SimpleServer(private val context: Context, port: Int) : NanoHTTPD(port) {
    private val handler = Handler(Looper.getMainLooper())

    init {
        try {
            start()
            val host = PortUtil.lan()
            (context as MainActivity).setServer("$host:$port")
            println("Server running on $host:$port")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun serve(session: IHTTPSession): Response {
        return when (session.uri) {
            "/api/hello" -> handleHelloRequest(session)
            "/api/channels" -> handleChannelsRequest(session)
            "/api/uri" -> handleUriRequest(session)
            else -> handleStaticContent(session)
        }
    }

    private fun handleHelloRequest(session: IHTTPSession): Response {
        val response = "Hello from NanoHTTPD API!"
        return newFixedLengthResponse(Response.Status.OK, "text/plain", response)
    }

    private fun handleChannelsRequest(session: IHTTPSession): Response {
        try {
            val map = HashMap<String, String>()
            session.parseBody(map)
            map["postData"]?.let {
                handler.post {
                    if (TVList.str2List(it)) {
                        File(context.filesDir, TVList.FILE_NAME).writeText(it)
                        "频道导入成功".showToast()
                    } else {
                        "频道导入错误".showToast()
                    }
                }
            }
        } catch (e: IOException) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "SERVER INTERNAL ERROR: IOException: " + e.message
            )
        }
        val response = "频道读取中"
        return newFixedLengthResponse(Response.Status.OK, "text/plain", response)
    }

    private fun readBody(session: IHTTPSession): String {
        val buffer = StringBuilder()
        val inputStreamReader = InputStreamReader(session.inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        bufferedReader.use {
            var line = it.readLine()
            while (line != null) {
                buffer.append(line)
                line = it.readLine()
            }
        }
        return buffer.toString()
    }

    data class UriResponse(
        var uri: String = "",
    )

    private fun handleUriRequest(session: IHTTPSession): Response {
        try {
            val map = HashMap<String, String>()
            session.parseBody(map)
            map["postData"]?.let {
                val url = Utils.formatUrl(Gson().fromJson(it, UriResponse::class.java).uri)
                val uri = Uri.parse(url)
                Log.i(TAG, "uri $uri")
                handler.post {
                    TVList.parseUri(uri)
                }
            }
        } catch (e: IOException) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "SERVER INTERNAL ERROR: IOException: " + e.message
            )
        }
        val response = "频道读取中"
        return newFixedLengthResponse(Response.Status.OK, "text/plain", response)
    }

    private fun handleStaticContent(session: IHTTPSession): Response {
        val html = loadHtmlFromResource(R.raw.index)
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }

    private fun loadHtmlFromResource(resourceId: Int): String {
        val inputStream = context.resources.openRawResource(resourceId)
        return inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }

    companion object {
        const val TAG = "SimpleServer"
    }
}