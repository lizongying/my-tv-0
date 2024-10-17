package com.lizongying.mytv0


import MainViewModel
import MainViewModel.Companion.FILE_NAME
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.lizongying.mytv0.data.ReqSettings
import com.lizongying.mytv0.data.RespSettings
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets


class SimpleServer(private val context: Context, private val viewModel: MainViewModel) :
    NanoHTTPD(PORT) {
    private val handler = Handler(Looper.getMainLooper())

    init {
        try {
            start()
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
            else -> handleStaticContent()
        }
    }

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

    private fun handleChannelsFromFile(session: IHTTPSession): Response {
        R.string.start_config_channel.showToast()
        val response = ""
        try {
            readBody(session)?.let {
                handler.post {
                    if (viewModel.str2List(it)) {
                        File(context.filesDir, FILE_NAME).writeText(it)
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
                val req = Gson().fromJson(it, ReqSettings::class.java)
                if (req.uri != null) {
                    val uri = Uri.parse(req.uri)
                    handler.post {
                        viewModel.parseUri(uri)
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
                    val req = Gson().fromJson(it, ReqSettings::class.java)
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
                    val req = Gson().fromJson(it, ReqSettings::class.java)
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
                    val req = Gson().fromJson(it, ReqSettings::class.java)
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

    private fun handleStaticContent(): Response {
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