package com.lizongying.mytv0

import java.net.Inet4Address
import java.net.NetworkInterface

object PortUtil {

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