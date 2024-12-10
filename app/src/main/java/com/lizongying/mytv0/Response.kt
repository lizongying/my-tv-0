package com.lizongying.mytv0

import okhttp3.Response
import okhttp3.ResponseBody


fun Response.bodyAlias(): ResponseBody? {
    return this.body
}

fun Response.codeAlias(): Int {
    return this.code
}