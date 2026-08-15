package com.example.util

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TotpUtils {
  private const val TIME_STEP_SECONDS = 30L
  private const val CODE_DIGITS = 6
  private const val DIGITS_POWER = 1_000_000

  // Default Base32 secret for institutional account
  const val DEFAULT_SECRET = "CUTMACAPINSECRET" // Valid Base32 string (only A-Z and 2-7)

  fun generateCurrentTotp(secretBase32: String, timeMs: Long = System.currentTimeMillis()): String {
    val timeStep = timeMs / 1000 / TIME_STEP_SECONDS
    return generateTotpForStep(secretBase32, timeStep)
  }

  fun verifyTotp(
    secretBase32: String,
    userInputCode: String,
    timeMs: Long = System.currentTimeMillis(),
    window: Int = 1 // +/- 1 step tolerance (30 seconds before or after)
  ): Boolean {
    val cleanCode = userInputCode.trim().filter { it.isDigit() }
    if (cleanCode.length != CODE_DIGITS) return false

    val currentStep = timeMs / 1000 / TIME_STEP_SECONDS
    for (i in -window..window) {
      val validCode = generateTotpForStep(secretBase32, currentStep + i)
      if (validCode == cleanCode) {
        return true
      }
    }
    return false
  }

  fun generateTotpForStep(secretBase32: String, timeStep: Long): String {
    val key = decodeBase32(secretBase32)
    val msg = ByteBuffer.allocate(8).putLong(timeStep).array()

    val mac = Mac.getInstance("HmacSHA1")
    val macKey = SecretKeySpec(key, "RAW")
    mac.init(macKey)
    val hash = mac.doFinal(msg)

    val offset = (hash[hash.size - 1].toInt() and 0x0f)
    val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
        ((hash[offset + 1].toInt() and 0xff) shl 16) or
        ((hash[offset + 2].toInt() and 0xff) shl 8) or
        (hash[offset + 3].toInt() and 0xff)

    val otp = binary % DIGITS_POWER
    return otp.toString().padStart(CODE_DIGITS, '0')
  }

  private fun decodeBase32(base32: String): ByteArray {
    val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    val clean = base32.uppercase().filter { it in base32Chars }
    val bytes = ByteArray(clean.length * 5 / 8)
    var buffer = 0
    var bitsLeft = 0
    var count = 0

    for (c in clean) {
      val value = base32Chars.indexOf(c)
      if (value < 0) continue
      buffer = (buffer shl 5) or value
      bitsLeft += 5
      if (bitsLeft >= 8) {
        if (count < bytes.size) {
          bytes[count++] = (buffer shr (bitsLeft - 8)).toByte()
        }
        bitsLeft -= 8
      }
    }
    return bytes
  }
}
