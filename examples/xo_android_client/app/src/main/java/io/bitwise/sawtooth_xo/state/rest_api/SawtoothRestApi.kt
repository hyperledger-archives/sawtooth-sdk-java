package io.bitwise.sawtooth_xo.state.rest_api

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SawtoothRestApi {

    @POST("/batches")
    fun postBatchList(@Body payload: RequestBody): Call<BatchListResponse>

    @GET("/batch_statuses")
    fun getBatchStatus(@Query("id") batch_id: String, @Query("wait") wait: Int): Call<BatchStatusResponse>

    @GET("/state")
    fun getState(@Query("address") address: String): Call<StateResponse>
}