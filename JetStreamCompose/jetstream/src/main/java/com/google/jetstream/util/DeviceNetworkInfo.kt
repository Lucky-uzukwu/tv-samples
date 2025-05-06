package com.google.jetstream.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

object DeviceNetworkInfo {

    fun getMacAddress(context: Context): String {
        // For Android 6.0 and later, we can't directly get WiFi MAC address
        // due to privacy changes, so we use NetworkInterfaces
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in interfaces) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes = nif.hardwareAddress ?: return "02:00:00:00:00:00"

                val builder = StringBuilder()
                for (b in macBytes) {
                    builder.append(String.format("%02X:", b))
                }

                if (builder.isNotEmpty()) {
                    builder.deleteCharAt(builder.length - 1)
                }

                return builder.toString()
            }
        } catch (e: Exception) {
            return "02:00:00:00:00:00" // Default MAC if error occurs
        }

        return "02:00:00:00:00:00" // Default fallback
    }

    /**
     * Get device IP address
     */
    fun getIPAddress(useIPv4: Boolean = true): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        val isIPv4 = addr is Inet4Address

                        if (useIPv4) {
                            if (isIPv4) return sAddr
                        } else {
                            // For IPv6 addresses
                            if (!isIPv4) {
                                // Remove scopeId if present
                                val delim = sAddr.indexOf('%')
                                return if (delim < 0) sAddr else sAddr.substring(0, delim)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "Unknown IP"
    }

    /**
     * Get device name
     */
    fun getDeviceName(context: Context): String {
        // Try to get the device name set by user
        val deviceName = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        if (!deviceName.isNullOrEmpty()) {
            return deviceName
        }

        // Fallback to manufacturer and model
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.capitalize()
        } else {
            "${manufacturer.capitalize()} $model"
        }
    }

    // Helper extension function
    private fun String.capitalize(): String {
        return if (this.isEmpty()) this else this.substring(0, 1).uppercase() + this.substring(1)
    }
}