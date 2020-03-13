package com.takaful.user.network.retrofit


import com.takaful.user.network.data.UserRegisterRequest
import com.takaful.user.network.data.UserRegisterResponse
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface TakafulApiService {


    @POST("user/auth/register")
    fun registerUser(@Body request: UserRegisterRequest): Flowable<UserRegisterResponse>

}