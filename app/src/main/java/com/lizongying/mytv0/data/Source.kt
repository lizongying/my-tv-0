package com.lizongying.mytv0.data

import java.util.UUID

data class Source(
    var id: String? = null,
    var uri: String,
    var checked: Boolean = false,
) {
    init {
        if (id.isNullOrEmpty()) {
            id = UUID.randomUUID().toString()
        }
    }
}
