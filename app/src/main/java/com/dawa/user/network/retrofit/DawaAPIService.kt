package com.dawa.user.network.retrofit


import com.dawa.user.network.data.*
import io.reactivex.Flowable
import okhttp3.MultipartBody
import retrofit2.http.*


interface DawaAPIService {


    @POST("user/auth/register")
    fun registerUser(@Body request: UserRegisterRequest): Flowable<UserRegisterResponse>

    @POST("user/auth/login")
    fun loginUser(@Body tokenRequest: UserTokenRequest): Flowable<ResponseWrapper<UserProfileResponse>>

    @GET("medication/list")
    fun listMedications(@Query("q") query: String?,
                        @Query("size") size: String,
                        @Query("page") page: String): Flowable<Pageable<MedicationsDTO>>

    @GET("medication/list/{id}")
    fun listMedicationDetails(@Path("id") id: Int): Flowable<MedicationsDTO>

    @GET("medication/categories")
    fun listMedicationsCategories(): Flowable<List<MedicineCategoryDTO>>

    @POST("medication/preserve/{id}")
    fun medicinePreservation(@Path("id") id: Int): Flowable<ErrorClass>

    @Multipart
    @POST("medication/add")
    fun addMedication(@Part file: MultipartBody.Part?,
                      @Part("body") medicationCreationForm: MedicationCreationForm): Flowable<ResponseWrapper<Any>>

    @Multipart
    @PUT("user/auth/user")
    fun changeUserProfile(@Part("body") body: ChangeProfileRequest,
                          @Part file: MultipartBody.Part?): Flowable<ErrorClass>


    @GET("medication/list/mine")
    fun listMyMedications(): Flowable<ResponseWrapper<List<MedicationsDTO>>>

    @DELETE("medication/mine/{id}/delete")
    fun deleteMyMedication(@Path("id") id: Int): Flowable<ResponseWrapper<Any>>

    @GET("medication/mine/{id}/preserver")
    fun getMyMedicationPreserver(@Path("id") id: Int): Flowable<ResponseWrapper<MedicineUserDTO>>


    @GET("notifications/list/{userId}")
    fun getAllNotifications(@Path("userId") userId: Int): Flowable<ResponseWrapper<List<NotificationDTO>>>

}