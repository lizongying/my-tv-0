package com.lizongying.mytv0.requests

import android.text.TextUtils
import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap


class DnsCache : Dns {
    private val dnsCache: MutableMap<String, List<InetAddress>> = ConcurrentHashMap()

    override fun lookup(hostname: String): List<InetAddress> {
        if (TextUtils.isEmpty(hostname)) {
            return Dns.SYSTEM.lookup(hostname);
        }

        dnsCache[hostname]?.let {
            return it
        }

        val addressesNew: MutableList<InetAddress> = ArrayList()

        val addresses = InetAddress.getAllByName(hostname).toList()
        for (address in addresses) {
            if (address is Inet4Address) {
                addressesNew.add(0, address);
            } else {
                addressesNew.add(address);
            }
        }

        if (addressesNew.isNotEmpty()) {
            dnsCache[hostname] = addressesNew
        }

        return addressesNew
    }
}