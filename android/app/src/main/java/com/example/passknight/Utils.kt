package com.example.passknight

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Utils {
    fun currentTimestamp(): String {
        return Instant.now().toEpochMilli().toString()
    }

    fun timestampStringToDate(iso: String): String {
        val instant = Instant.ofEpochMilli(iso.toLong())
        return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.systemDefault()).format(instant).toString()
    }
}

