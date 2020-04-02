package com.takaful.user.network.retrofit


import com.takaful.user.network.data.*
import io.reactivex.Flowable
import okhttp3.MultipartBody
import retrofit2.http.*


interface TakafulApiService {


    @POST("user/auth/register")
    fun registerUser(@Body request: UserRegisterRequest): Flowable<UserRegisterResponse>

    @POST("user/auth/login")
    fun loginUser(@Body tokenRequest: UserTokenRequest): Flowable<UserProfileResponse>

    @Multipart
    @PUT("user/auth/user")
    fun changeUserProfile(@Part changeProfileRequest: MultipartBody.Part, @Part image: MultipartBody.Part?): Flowable<ErrorClass>

}