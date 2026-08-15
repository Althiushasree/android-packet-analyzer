package com.example.data.model

data class UserSession(
  val email: String,
  val displayName: String,
  val photoUrl: String? = null,
  val isAuthenticated: Boolean = true,
  val domainVerified: Boolean = true
)
