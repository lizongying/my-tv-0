package com.lizongying.mytv0.requests


import android.net.Uri
import android.os.Build
import android.util.Log
import com.lizongying.mytv0.SP
import com.lizongying.mytv0.Utils.formatUrl
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


object HttpClient {
    const val TAG = "HttpClient"
    private const val HOST = "https://www.gitlink.org.cn/lizongying/my-tv-0/raw/"
    const val DOWNLOAD_HOST = "https://www.gitlink.org.cn/lizongying/my-tv-0/releases/download/"

    private val clientCache = mutableMapOf<String?, OkHttpClient>()

    val releaseService: ReleaseService by lazy {
        Retrofit.Builder()
            .baseUrl(HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ReleaseService::class.java)
    }

    val okHttpClient: OkHttpClient by lazy {
        getClientWithProxy()
    }

    val builder: OkHttpClient.Builder by lazy {
        createBuilder()
    }

    fun getClientWithProxy(): OkHttpClient {
        clientCache[SP.proxy]?.let {
            return it
        }

        if (!SP.proxy.isNullOrEmpty()) {
            try {
                val proxyUri = Uri.parse(formatUrl(SP.proxy!!))
                val proxyType = when (proxyUri.scheme) {
                    "http", "https" -> Proxy.Type.HTTP
                    "socks", "socks5" -> Proxy.Type.SOCKS
                    else -> null
                }
                proxyType?.let {
                    builder.proxy(Proxy(it, InetSocketAddress(proxyUri.host, proxyUri.port)))
                }
                Log.i(TAG, "apply proxy $proxyUri")
            } catch (e: Exception) {
                Log.e(TAG, "getClientWithProxy", e)
            }
        }

        val client = builder.build()
        clientCache[SP.proxy] = client
        return client
    }

    private fun createBuilder(): OkHttpClient.Builder {
        val trustManager =
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return emptyArray()
                }
            }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), java.security.SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
            .dns(DnsCache())
            .apply { enableTls12OnPreLollipop() }
    }

    private fun OkHttpClient.Builder.enableTls12OnPreLollipop() {
        if (Build.VERSION.SDK_INT < 22) {
            try {
                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, null, java.security.SecureRandom())

                val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
                )
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                val trustManager = trustManagers[0] as X509TrustManager

                sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory), trustManager)
                connectionSpecs(
                    listOf(
                        ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build(),
                        ConnectionSpec.COMPATIBLE_TLS,
                        ConnectionSpec.CLEARTEXT
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "enableTls12OnPreLollipop", e)
            }
        }
    }
}