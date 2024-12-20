package com.lizongying.mytv0.requests


import android.net.Uri
import android.os.Build
import android.util.Log
import com.lizongying.mytv0.SP
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


object HttpClient {
    const val TAG = "HttpClient"
    private const val HOST = "https://www.gitlink.org.cn/lizongying/my-tv-0/raw/"
    const val DOWNLOAD_HOST = "https://www.gitlink.org.cn/lizongying/my-tv-0/releases/download/"

    val okHttpClient: OkHttpClient by lazy {
        getUnsafeOkHttpClient()
    }

    val releaseService: ReleaseService by lazy {
        Retrofit.Builder()
            .baseUrl(HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ReleaseService::class.java)
    }

    val configService: ConfigService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .build().create(ConfigService::class.java)
    }

    private fun enableTls12OnPreLollipop(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        if (Build.VERSION.SDK_INT < 22) {
            try {
                val sc = SSLContext.getInstance("TLSv1.2")

                sc.init(null, null, java.security.SecureRandom())

                // a more robust version is to pass a custom X509TrustManager
                // as the second parameter and make checkServerTrusted to accept your server.
                // Credits: https://github.com/square/okhttp/issues/2372#issuecomment-1774955225
                val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
                )
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                    ("Unexpected default trust managers: ${trustManagers.contentToString()}")
                }
                val trustManager = trustManagers[0] as X509TrustManager

                builder
                    .sslSocketFactory(Tls12SocketFactory(sc.socketFactory), trustManager)
                    .connectionSpecs(
                        listOf(
                            ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                                .tlsVersions(TlsVersion.TLS_1_2)
                                .build(),
                            ConnectionSpec.COMPATIBLE_TLS,
                            ConnectionSpec.CLEARTEXT
                        )
                    )
            } catch (e: Exception) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", e)
            }
        }

        return builder
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            val trustAllCerts: Array<TrustManager> = arrayOf(
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
            )

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            val builder = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
                .dns(DnsCache())

            if (SP.proxy != "") {
                val uri = Uri.parse(SP.proxy)
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(uri.host, uri.port))
                builder.proxy(proxy)
            }

            return enableTls12OnPreLollipop(builder).build()

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}