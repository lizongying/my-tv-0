package com.lizongying.mytv0

import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket

object PortUtil {

    fun findFreePort(): Int {
        return try {
            ServerSocket(34567).use { socket ->
                socket.localPort
            }
        } catch (e: IOException) {
            try {
                ServerSocket(0).use { socket ->
                    socket.localPort
                }
            } catch (e: IOException) {
                e.printStackTrace()
                -1 // Return -1 to indicate an error
            }
        }
    }

    fun lan(): String? {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val inetAddresses = networkInterfaces.nextElement().inetAddresses
            while (inetAddresses.hasMoreElements()) {
                val inetAddress = inetAddresses.nextElement()
                if (inetAddress is Inet4Address) {
                    if (inetAddress.hostAddress == "127.0.0.1") {
                        continue
                    }
                    return inetAddress.hostAddress
                }
            }
        }
        return null
    }
}