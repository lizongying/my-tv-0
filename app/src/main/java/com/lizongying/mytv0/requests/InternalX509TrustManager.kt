package com.lizongying.mytv0.requests

import android.content.Context
import android.util.Log
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

//this code can use https://badssl.com to test
//it can deal with self-signed/expired/wrong host/untrusted root certificate
//it can't deal with certificate pinning and revoked certificate
//latest pem file can be downloaded from https://curl.se/ca/cacert.pem
class InternalX509TrustManager(private val context: Context) : X509TrustManager {
    private val TAG = "CustomX509TrustManager"
    private val trustManagers: Array<TrustManager>

    init {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificates = loadCertificatesFromAssets()

        certificates.forEachIndexed { index, certString ->
            val cert =
                certificateFactory.generateCertificate(ByteArrayInputStream(certString.toByteArray())) as X509Certificate
            keyStore.setCertificateEntry("ca$index", cert)
        }

        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        trustManagers = trustManagerFactory.trustManagers
    }

    private fun loadCertificatesFromAssets(): List<String> {
        val certificates = mutableListOf<String>()
        try {
            val inputStream = context.assets.open("cacert.pem")
            val content = inputStream.bufferedReader().use { it.readText() }

            val certRegex =
                "-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----".toRegex(RegexOption.DOT_MATCHES_ALL)
            certRegex.findAll(content).forEach { matchResult ->
                certificates.add(matchResult.value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading certificates from assets", e)
        }
        return certificates
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        (trustManagers[0] as X509TrustManager).checkClientTrusted(chain, authType)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        (trustManagers[0] as X509TrustManager).checkServerTrusted(chain, authType)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return (trustManagers[0] as X509TrustManager).acceptedIssuers
    }
}