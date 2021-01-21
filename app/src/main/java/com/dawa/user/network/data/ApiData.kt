package com.dawa.user.network.data

import java.io.Serializable
import java.sql.Timestamp

/*
  This kotlin file can hold an infinite number of data classes
  so wy we need to separate them into different files they all
  for one business cases
 */

data class ChangePasswordRequest(val phone: String, val password: String)

class Pageable<T> {
    var pageAbleList: List<T> = mutableListOf()
    var pagination: Pagination? = null
    var next = false
}


data class Pagination(var currentPage: Int = 0, var lastPage: Int = 0, var pageSize: Int = 0)

data class UserRegisterRequest(val username: String,
                               val password: String,
                               val phone: String,
                               val fullName: String,
                               val pictureUrl: String)

data class UserRegisterResponse(val success: Boolean, val message: String)

data class UserTokenRequest(val username: String, val password: String) : Serializable

data class TokenResponse(val success: Boolean, val jwtToken: String)

data class ErrorClass(val success: Boolean, val message: String)

data class PreservationsDTO(val id: Int, val timestamp: Timestamp)

data class NotificationDTO(val id: Int,
                           val title: String,
                           val body: String,
                           val timestamp: Timestamp)

data class SuggestionsDTO(val id: Int,
                          val type: String,
                          val timestamp: Timestamp,
                          val title: String,
                          val body: String)

data class ReportDTO(val id: Int, val payload: String, val timestamp: Timestamp)

data class MedicationsMock(val id: Int,
                           val name: String,
                           val lang: Double,
                           val lat: Double,
                           val imageUrl: String,
                           val addressTitle: String)


data class MedicationCreationForm(val name: String,
                                  val address: String,
                                  val lang: Double,
                                  val lat: Double,
                                  val categoryId: Int,
                                  val userId: Int)

data class MedicationsDTO(val id: Int,
                          val name: String,
                          val lang: Double,
                          val lat: Double,
                          val imageUrl: String,
                          val addressTitle: String,
                          val userDTO: MedicineUserDTO?,
                          val categoryDTO: MedicineCategoryDTO?,
                          val preserver: PreservationsDTO?)

data class MedicineUserDTO(val id: Int,
                           val username: String,
                           val phone: String,
                           val fullName: String,
                           val pictureUrl: String)

data class MedicineCategoryDTO(val id: Int, val name: String, val imageUrl: String?)

data class UserProfileResponse(val id: Int,
                               val phone: String = "",
                               val fullName: String = "",
                               val pictureUrl: String = "",
                               val token: String = "")

data class ChangeProfileRequest(val fullName: String,
                                val oldUsername: String,
                                val newUsername: String)

data class ResponseWrapper<T>(val success: Boolean, val message: String, val data: T?)

data class UserPreservationDTO(val id: Int?,
                               val medicine: MedicationsDTO?,
                               val timestamp: Timestamp?

)