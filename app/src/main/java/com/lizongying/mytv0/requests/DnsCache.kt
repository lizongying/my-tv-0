package com.lizongying.mytv0.requests

import okhttp3.Dns
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap


class DnsCache : Dns {
    private val dnsCache = ConcurrentHashMap<String, List<InetAddress>>()

    override fun lookup(hostname: String): List<InetAddress> {
        if (hostname.isEmpty()) {
            return Dns.SYSTEM.lookup(hostname);
        }

        dnsCache[hostname]?.let {
            return it
        }

        val ipv4Addresses = mutableListOf<InetAddress>()
        val ipv6Addresses = mutableListOf<InetAddress>()

        for (address in InetAddress.getAllByName(hostname)) {
            if (address is Inet4Address) {
                ipv4Addresses.add(address)
            } else if (address is Inet6Address) {
                ipv6Addresses.add(address)
            }
        }

        val addressesNew = ipv4Addresses + ipv6Addresses

        if (addressesNew.isNotEmpty()) {
            dnsCache[hostname] = addressesNew
        }

        return addressesNew
    }
}