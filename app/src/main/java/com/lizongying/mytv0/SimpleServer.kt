package com.lizongying.mytv0


import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.lizongying.mytv0.models.TVList
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.IOException
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
            "/api/settings" -> handleSettings()
            "/api/channels" -> handleChannelsFromFile(session)
            "/api/uri" -> handleChannelsFromUri(session)
            "/api/proxy" -> handleProxy(session)
            "/api/epg" -> handleEPG(session)
            "/api/channel" -> handleDefaultChannel(session)
            else -> handleStaticContent(session)
        }
    }

    data class RespSettings(
        val channelUri: String,
        val channelDefault: Int,
        val proxy: String,
        val epg: String,
    )

    private fun handleSettings(): Response {
        val response: String
        try {
            val respSettings = RespSettings(
                channelUri = SP.config ?: "",
                channelDefault = SP.channel,
                proxy = SP.proxy ?: "",
                epg = SP.epg ?: "",
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

    data class Req(
        var uri: String? = "",
        val proxy: String?,
        val epg: String?,
        val channel: Int?,
    )

    private fun handleChannelsFromFile(session: IHTTPSession): Response {
        R.string.start_config_channel.showToast()
        val response = ""
        try {
            readBody(session)?.let {
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

    private fun handleChannelsFromUri(session: IHTTPSession): Response {
        R.string.start_config_channel.showToast()
        val response = ""
        try {
            readBody(session)?.let {
                val req = Gson().fromJson(it, Req::class.java)
                if (req.uri != null) {
                    val url = req.uri
                    val uri = Uri.parse(url)
                    Log.i(TAG, "uri $uri")
                    handler.post {
                        TVList.parseUri(uri)
                    }
                }
            }
        } catch (e: IOException) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                e.message
            )
        }
        return newFixedLengthResponse(Response.Status.OK, "text/plain", response)
    }

    private fun handleProxy(session: IHTTPSession): Response {
        try {
            readBody(session)?.let {
                handler.post {
                    val req = Gson().fromJson(it, Req::class.java)
                    if (req.proxy != null) {
                        SP.proxy = req.proxy
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

    private fun handleEPG(session: IHTTPSession): Response {
        try {
            readBody(session)?.let {
                handler.post {
                    val req = Gson().fromJson(it, Req::class.java)
                    if (req.epg != null) {
                        SP.epg = req.epg
                        R.string.default_epg_set_success.showToast()
                    } else {
                        R.string.default_epg_set_failure.showToast()
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

    private fun handleDefaultChannel(session: IHTTPSession): Response {
        R.string.start_set_default_channel.showToast()
        val response = ""
        try {
            readBody(session)?.let {
                handler.post {
                    val req = Gson().fromJson(it, Req::class.java)
                    if (req.channel != null && req.channel > -1) {
                        SP.channel = req.channel
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

    private fun readBody(session: IHTTPSession): String? {
        val map = HashMap<String, String>()
        session.parseBody(map)
        return map["postData"]
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