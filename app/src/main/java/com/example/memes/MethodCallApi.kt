package com.example.memes

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface MethodCallApi {

    @GET("gimme")
    fun getGimme() : Call<Gimme>
}