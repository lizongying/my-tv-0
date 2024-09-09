package com.lizongying.mytv0.requests

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class InternalSSLSocketFactory(private val mSSLSocketFactory: SSLSocketFactory) : SSLSocketFactory() {

    override fun getDefaultCipherSuites(): Array<String> {
        return mSSLSocketFactory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return mSSLSocketFactory.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket {
        return enableTLSOnSocket(mSSLSocketFactory.createSocket())
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return enableTLSOnSocket(mSSLSocketFactory.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return enableTLSOnSocket(mSSLSocketFactory.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return enableTLSOnSocket(mSSLSocketFactory.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return enableTLSOnSocket(mSSLSocketFactory.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return enableTLSOnSocket(mSSLSocketFactory.createSocket(address, port, localAddress, localPort))
    }

    private fun enableTLSOnSocket(socket: Socket): Socket {
        if (socket is SSLSocket) {
            socket.enabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")
        }
        return socket
    }
}
