package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AuthState

@Composable
fun LoginScreen(
  authState: AuthState,
  pendingEmail: String,
  pendingDisplayName: String,
  totpSecret: String,
  onStartGoogleAuth: (email: String, displayName: String) -> Unit,
  onVerifyTotp: (code: String) -> Boolean,
  onClearError: () -> Unit,
  onResetToLogin: () -> Unit
) {
  var accountInput by remember { mutableStateOf("student@cutmac.ap.in") }
  var verificationCodeInput by remember { mutableStateOf("") }
  var localError by remember { mutableStateOf<String?>(null) }
  var showSecretModal by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val scrollState = rememberScrollState()

  val effectiveErrorMessage = when (authState) {
    is AuthState.Error -> authState.message
    else -> localError
  }

  // Pure White Theme Palette matching the UI screenshot structure
  val whiteCanvasBg = Color(0xFFF8FAFC)        // Slate 50
  val whiteCardBg = Color(0xFFFFFFFF)          // Pure White
  val cyanAccent = Color(0xFF0097A7)           // Deep Cyan for white background readability
  val cyanButtonBg = Color(0xFF00D4E6)         // Vibrant Cyan for main action button
  val buttonTextColor = Color(0xFF0F172A)      // Dark Navy text
  val inputBorderColor = Color(0xFFCBD5E1)      // Light Slate Border
  val inputBgColor = Color(0xFFFAFAFA)          // Off-white input background
  val lockBadgeBg = Color(0xFFF59E0B)           // Warm Amber/Orange Lock Badge

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(whiteCanvasBg)
      .padding(20.dp)
      .verticalScroll(scrollState)
      .testTag("login_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("2fa_login_card"),
      colors = CardDefaults.cardColors(containerColor = whiteCardBg),
      shape = RoundedCornerShape(20.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Cyan Rounded Square Shield Icon
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE0F7FA)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Shield Icon",
            tint = cyanAccent,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PACKETIVEX Subtitle
        Text(
          text = "PACKETIVEX SECURE NETWORK MONITOR",
          style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
          ),
          color = cyanAccent
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Two-Factor Authentication Title
        Text(
          text = "Two-Factor\nAuthentication",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
          ),
          color = Color(0xFF0F172A),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Instruction Text
        Text(
          text = "Enter the 6-digit verification code\nfrom Google Authenticator.",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF64748B),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error Card
        if (effectiveErrorMessage != null) {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp)
              .testTag("auth_error_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = effectiveErrorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF991B1B),
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Account Input Box
        OutlinedTextField(
          value = accountInput,
          onValueChange = {
            accountInput = it
            localError = null
            onClearError()
          },
          label = {
            Text(
              "Account (@cutmac.ap.in)",
              color = Color(0xFF64748B)
            )
          },
          leadingIcon = {
            Icon(
              Icons.Default.Mail,
              contentDescription = null,
              tint = cyanAccent
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("account_email_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputBgColor,
            unfocusedContainerColor = inputBgColor,
            disabledContainerColor = inputBgColor,
            focusedBorderColor = cyanAccent,
            unfocusedBorderColor = inputBorderColor,
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A)
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6-Digit Verification Code Input Box with Orange Lock Icon
        OutlinedTextField(
          value = verificationCodeInput,
          onValueChange = {
            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
              verificationCodeInput = it
              localError = null
              onClearError()
            }
          },
          label = {
            Text(
              "6-Digit Verification Code",
              color = Color(0xFF64748B)
            )
          },
          leadingIcon = {
            Box(
              modifier = Modifier
                .padding(start = 8.dp, end = 4.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(lockBadgeBg),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
              )
            }
          },
          placeholder = {
            Text("123456", color = Color(0xFF94A3B8))
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("verification_code_input"),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputBgColor,
            unfocusedContainerColor = inputBgColor,
            disabledContainerColor = inputBgColor,
            focusedBorderColor = cyanAccent,
            unfocusedBorderColor = inputBorderColor,
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A)
          )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cyan Fill VERIFY CODE Button
        Button(
          onClick = {
            val email = accountInput.trim().lowercase()
            val domain = email.substringAfterLast("@")
            if (email.isEmpty()) {
              localError = "Please enter your college email address."
            } else if (!domain.equals("cutmac.ap.in", ignoreCase = true)) {
              localError = "Access Denied: Email must end with @cutmac.ap.in!"
            } else if (verificationCodeInput.length < 6) {
              localError = "Please enter a valid 6-digit verification code from Google Authenticator."
            } else {
              val name = email.substringBefore("@").replace(".", " ").split(" ")
                .joinToString(" ") { word -> word.replaceFirstChar { char -> char.uppercase() } }
              onStartGoogleAuth(email, name)
              val verified = onVerifyTotp(verificationCodeInput)
              if (!verified) {
                localError = "Invalid 6-digit TOTP code. Please check Google Authenticator."
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("verify_code_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = cyanButtonBg,
            contentColor = buttonTextColor
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = "VERIFY CODE",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Google Authenticator Setup Secret Link
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { showSecretModal = true }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("authenticator_secret_link"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Key,
            contentDescription = "Key Icon",
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Google Authenticator Setup Secret",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
          )
        }
      }
    }

    // Modal to display Google Authenticator Setup Secret & Quick 6-digit Code Helper
    if (showSecretModal) {
      Dialog(onDismissRequest = { showSecretModal = false }) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("authenticator_secret_dialog"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = whiteCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.QrCode,
              contentDescription = null,
              tint = cyanAccent,
              modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Google Authenticator Setup",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Add this key manually to your Google Authenticator app for student@cutmac.ap.in",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF64748B),
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Key Display Box
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = totpSecret,
                style = MaterialTheme.typography.bodyMedium.copy(
                  letterSpacing = 2.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = cyanAccent
              )

              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Authenticator Secret", totpSecret)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "Secret copied to clipboard", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(
                  Icons.Default.ContentCopy,
                  contentDescription = "Copy Secret",
                  tint = Color(0xFF64748B)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = { showSecretModal = false },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
              Text("Close", color = Color.White)
            }
          }
        }
      }
    }
  }
}
