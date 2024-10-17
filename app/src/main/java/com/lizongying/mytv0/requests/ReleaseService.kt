package com.lizongying.mytv0.requests

import com.lizongying.mytv0.data.ReleaseResponse
import retrofit2.Call
import retrofit2.http.GET

interface ReleaseService {
    @GET("kitkat/version.json")
    fun getRelease(
    ): Call<ReleaseResponse>
}