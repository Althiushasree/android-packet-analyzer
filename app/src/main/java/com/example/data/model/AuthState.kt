package com.example.data.model

sealed interface AuthState {
  data object LoggedOut : AuthState
  data object GoogleAuthenticating : AuthState
  data object DomainChecking : AuthState
  data object TotpRequired : AuthState
  data object Authenticated : AuthState
  data class Error(val message: String) : AuthState
}
