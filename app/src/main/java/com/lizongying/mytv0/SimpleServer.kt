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


class SimpleServer(private val context: Context) : NanoHTTPD(PORT) {
    private val handler = Handler(Looper.getMainLooper())

    init {
        try {
            start()
            val host = PortUtil.lan()
            (context as MainActivity).setServer("$host:$PORT")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun serve(session: IHTTPSession): Response {
        return when (session.uri) {
            "/api/channels" -> handleChannelsFromFile(session)
            "/api/uri" -> handleChannelsFromUri(session)
            "/api/channel" -> handleDefaultChannel(session)
            "/api/proxy" -> handleProxy(session)
            "/api/settings" -> handleSettings()
            else -> handleStaticContent(session)
        }
    }

    private fun handleChannelsFromFile(session: IHTTPSession): Response {
        R.string.start_config_channel.showToast()
        val response = ""
        try {
            val map = HashMap<String, String>()
            session.parseBody(map)
            map["postData"]?.let {
                handler.post {
                    if (TVList.str2List(it)) {
                        File(context.filesDir, TVList.FILE_NAME).writeText(it)
                        SP.config = "file://"
                        R.string.channel_import_success.showToast()
                    } else {
                        R.string.channel_import_error.showToast()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                e.message
            )
        }
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

    private fun handleChannelsFromUri(session: IHTTPSession): Response {
        R.string.start_config_channel.showToast()
        val response = ""
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
        return newFixedLengthResponse(Response.Status.OK, "text/plain", response)
    }

    data class ReqChannel(
        val channel: Int,
    )

    private fun handleDefaultChannel(session: IHTTPSession): Response {
        R.string.start_set_default_channel.showToast()
        val response = ""
        try {
            val map = HashMap<String, String>()
            session.parseBody(map)
            map["postData"]?.let {
                handler.post {
                    val reqChannel = Gson().fromJson(it, ReqChannel::class.java)
                    if (reqChannel.channel > 1) {
                        SP.channel = reqChannel.channel
                        R.string.default_channel_set_success.showToast()
                    } else {
                        R.string.default_channel_set_failure.showToast()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                e.message
            )
        }
        return newFixedLengthResponse(Response.Status.OK, "text/plain", response)
    }

    data class RespSettings(
        val channelUri: String,
        val channelDefault: Int,
        val proxy: String,
    )

    private fun handleSettings(): Response {
        val response: String
        try {
            val respSettings = RespSettings(
                channelUri = SP.config ?: "",
                channelDefault = SP.channel,
                proxy = SP.proxy ?: "",
            )
            response = Gson().toJson(respSettings) ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                e.message
            )
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", response)
    }

    data class ReqProxy(
        val proxy: String,
    )

    private fun handleProxy(session: IHTTPSession): Response {
        try {
            val map = HashMap<String, String>()
            session.parseBody(map)
            map["postData"]?.let {
                handler.post {
                    val reqProxy = Gson().fromJson(it, ReqProxy::class.java)
                    if (reqProxy.proxy.isNotEmpty()) {
                        SP.proxy = reqProxy.proxy
                        R.string.default_proxy_set_success.showToast()
                    } else {
                        R.string.default_proxy_set_failure.showToast()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                e.message
            )
        }
        val response = ""
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
        const val PORT = 34567
    }
}