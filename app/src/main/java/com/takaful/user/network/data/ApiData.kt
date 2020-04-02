package com.takaful.user.network.data

import java.io.Serializable
import java.sql.Timestamp

/*
  This kotlin file can hold an infinite number of data classes
  so wy we need to separate them into different files they all
  for one business cases
 */

data class UserRegisterRequest(
    val username: String,
    val password: String,
    val phone: String,
    val fullName: String,
    val pictureUrl: String
)

data class UserRegisterResponse(
    val success: Boolean,
    val message: String
)

data class UserTokenRequest(
    val username: String,
    val password: String
) : Serializable

data class TokenResponse(
    val success: Boolean,
    val jwtToken: String
)

data class ErrorClass(
    val success: Boolean,
    val message: String
)

data class PreservationsDTO(
    val id: Int,
    val timestamp: Timestamp
)

data class NotificationDTO(
    val id: Int,
    val title: String,
    val body: String,
    val timestamp: Timestamp
)

data class SuggestionsDTO(
    val id: Int,
    val type: String,
    val timestamp: Timestamp,
    val title: String,
    val body: String
)

data class ReportDTO(
    val id: Int,
    val payload: String,
    val timestamp: Timestamp
)

data class MedicationsDTO(
    val id: Int,
    val name: String,
    val lang: Double,
    val lat: Double,
    val imageUrl: String,
    val addressTitle: String,
    val userDTO: MedicineUserDTO,
    val categoryDTO: MedicineCategoryDTO,
    val preserver: PreservationsDTO
)

data class MedicineUserDTO(
    val id: Int,
    val username: String,
    val phone: String,
    val fullName: String,
    val pictureUrl: String
)

data class MedicineCategoryDTO(
    val id: Int,
    val name: String,
    val imageUrl: String
)

data class UserProfileResponse(
    val id: Int,
    val phone: String = "",
    val fullName: String = "",
    val pictureUrl: String = "",
    val token: String = ""
)
data class ChangeProfileRequest(
    val phone: String,
    val fullName: String,
//    val pictureUrl: String,
    val userName: String
)