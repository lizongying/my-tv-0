package com.lizongying.mytv0


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.lizongying.mytv0.models.TVList
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
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
            else -> handleStaticContent(session)
        }
    }

    private fun handleHelloRequest(session: IHTTPSession): Response {
        val response = "Hello from NanoHTTPD API!"
        return newFixedLengthResponse(Response.Status.OK, "text/plain", response)
    }

    private fun handleChannelsRequest(session: IHTTPSession): Response {
        try {
            val inputStream = session.inputStream
            val buf = ByteArray(1024)
            var read: Int
            val sb = StringBuilder()
            while (inputStream.available() > 0) {
                read = inputStream.read(buf, 0, Math.min(buf.size, inputStream.available()))
                sb.append(String(buf, 0, read))
            }
            val requestBody = sb.toString()
            Log.i(TAG, requestBody)
            handler.post {
                TVList.str2List(requestBody)
            }
        } catch (e: IOException) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "SERVER INTERNAL ERROR: IOException: " + e.message
            )
        }
        val response = "Success!"
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