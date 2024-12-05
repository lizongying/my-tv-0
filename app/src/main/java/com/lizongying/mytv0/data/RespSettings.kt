package com.lizongying.mytv0.data

data class RespSettings(
    val channelUri: String,
    val channelText: String,
    val channelDefault: Int,
    val proxy: String,
    val epg: String,
    val history: List<Source>,
)
