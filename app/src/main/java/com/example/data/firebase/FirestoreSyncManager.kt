package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.MessageRole
import com.example.data.intelligence.DefensiveSecurityAlert
import com.example.data.model.PcapFileEntity
import com.example.data.model.UserSession
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Manages Firebase Authentication (Google Sign-In via Credential Manager)
 * and bidirectional data synchronization with Cloud Firestore.
 */
class FirestoreSyncManager(private val context: Context) {
  companion object {
    private const val TAG = "FirestoreSyncManager"
  }

  private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
  private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
  private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

  /**
   * Triggers Google Sign-In using Android Jetpack CredentialManager and links with Firebase Auth.
   */
  suspend fun signInWithGoogle(serverClientId: String = ""): Result<UserSession> = withContext(Dispatchers.IO) {
    try {
      val clientId = if (serverClientId.isNotBlank()) serverClientId else "839771228314-dummy-web-client-id.apps.googleusercontent.com"
      val googleIdOption = GetSignInWithGoogleOption.Builder(clientId).build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result: GetCredentialResponse = credentialManager.getCredential(
        request = request,
        context = context
      )

      val credential = result.credential
      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
        val authResult = firebaseAuth.signInWithCredential(authCredential).await()
        val user = authResult.user

        val userSession = UserSession(
          email = user?.email ?: googleIdToken.id,
          displayName = user?.displayName ?: googleIdToken.displayName ?: "Student User",
          photoUrl = user?.photoUrl?.toString() ?: googleIdToken.profilePictureUri?.toString(),
          isAuthenticated = true,
          domainVerified = (user?.email ?: "").endsWith("cutmac.ap.in", ignoreCase = true)
        )

        // Sync user profile to Firestore
        syncUserProfile(userSession)

        Result.success(userSession)
      } else {
        Result.failure(Exception("Unsupported credential type returned: ${credential.type}"))
      }
    } catch (e: Exception) {
      Log.w(TAG, "CredentialManager Google Sign-In not completed: ${e.message}")
      Result.failure(e)
    }
  }

  /**
   * Syncs user profile metadata to Cloud Firestore.
   */
  suspend fun syncUserProfile(session: UserSession): Boolean = withContext(Dispatchers.IO) {
    try {
      val uid = firebaseAuth.currentUser?.uid ?: session.email.replace(".", "_")
      val docData = hashMapOf(
        "email" to session.email,
        "displayName" to session.displayName,
        "photoUrl" to (session.photoUrl ?: ""),
        "lastActiveTimestamp" to System.currentTimeMillis(),
        "domainVerified" to session.domainVerified
      )
      firestore.collection("users").document(uid).set(docData, SetOptions.merge()).await()
      true
    } catch (e: Exception) {
      Log.w(TAG, "Firestore syncUserProfile skipped/failed: ${e.message}")
      false
    }
  }

  /**
   * Persists a saved PCAP capture file record to Cloud Firestore.
   */
  suspend fun syncPcapRecord(pcap: PcapFileEntity): Boolean = withContext(Dispatchers.IO) {
    try {
      val uid = firebaseAuth.currentUser?.uid ?: "anonymous"
      val docData = hashMapOf(
        "id" to pcap.id,
        "fileName" to pcap.fileName,
        "fileSizeBytes" to pcap.fileSizeBytes,
        "fileSizeFormatted" to pcap.fileSizeFormatted,
        "packetCount" to pcap.packetCount,
        "dateFormatted" to pcap.dateFormatted,
        "notes" to pcap.notes,
        "syncedByUid" to uid,
        "syncTimestamp" to System.currentTimeMillis()
      )
      firestore.collection("pcap_vault").document("pcap_${pcap.id}").set(docData, SetOptions.merge()).await()
      true
    } catch (e: Exception) {
      Log.w(TAG, "Firestore syncPcapRecord skipped: ${e.message}")
      false
    }
  }

  /**
   * Persists security anomalies/threat alerts into Cloud Firestore.
   */
  suspend fun syncDefensiveAlert(alert: DefensiveSecurityAlert): Boolean = withContext(Dispatchers.IO) {
    try {
      val docData = hashMapOf(
        "id" to alert.id,
        "timestamp" to alert.timestamp,
        "timeFormatted" to alert.timeFormatted,
        "severity" to alert.severity.name,
        "title" to alert.title,
        "deviceIp" to alert.deviceIp,
        "sourceAddress" to alert.sourceAddress,
        "destinationAddress" to alert.destinationAddress,
        "protocol" to alert.protocol,
        "port" to alert.port,
        "evidence" to alert.evidence,
        "confidence" to alert.confidence,
        "explanation" to alert.explanation,
        "syncTimestamp" to System.currentTimeMillis()
      )
      firestore.collection("threat_alerts").document("alert_${alert.id}").set(docData, SetOptions.merge()).await()
      true
    } catch (e: Exception) {
      Log.w(TAG, "Firestore syncDefensiveAlert skipped: ${e.message}")
      false
    }
  }

  /**
   * Persists a Gemini chat message to Cloud Firestore conversation history.
   */
  suspend fun syncChatMessage(sessionId: String, message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
    try {
      val uid = firebaseAuth.currentUser?.uid ?: "student_user"
      val docData = hashMapOf(
        "id" to message.id,
        "sessionId" to sessionId,
        "role" to message.role.name,
        "content" to message.content,
        "timestamp" to message.timestamp,
        "modelUsed" to (message.modelUsed?.name ?: ""),
        "isThinking" to message.isThinking,
        "latencyMs" to (message.latencyMs ?: 0L),
        "groundingSourcesCount" to message.groundingSources.size,
        "searchQueries" to message.searchQueries,
        "uid" to uid
      )
      firestore.collection("chat_sessions")
        .document(sessionId)
        .collection("messages")
        .document(message.id)
        .set(docData, SetOptions.merge())
        .await()
      true
    } catch (e: Exception) {
      Log.w(TAG, "Firestore syncChatMessage skipped: ${e.message}")
      false
    }
  }

  /**
   * Signs out the current user from Firebase Auth and clears CredentialManager state.
   */
  suspend fun signOut() = withContext(Dispatchers.IO) {
    try {
      firebaseAuth.signOut()
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
      Log.w(TAG, "Error during signOut: ${e.message}")
    }
  }
}
