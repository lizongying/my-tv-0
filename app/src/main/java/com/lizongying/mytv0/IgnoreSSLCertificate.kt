package com.lizongying.mytv0

import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object IgnoreSSLCertificate {

    fun ignore() {
        try {
            val trustAllCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? {
                        return null
                    }

                    override fun checkClientTrusted(
                        certs: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        certs: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }
                }
            )

            // Install the all-trusting trust manager
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)

            // Create all-trusting host name verifier
            val allHostsValid = HostnameVerifier { _, _ -> true }

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
