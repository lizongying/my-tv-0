package com.lizongying.mytv0.requests


data class TimeResponse(
    val data: Time
) {
    data class Time(
        val t: String
    )
}