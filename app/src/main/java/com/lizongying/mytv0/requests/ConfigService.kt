package com.lizongying.mytv0.requests

import retrofit2.Call
import retrofit2.http.Url

interface ConfigService {
    fun getConfig(
        @Url url: String
    ): Call<String>
}