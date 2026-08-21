package com.example.data.vpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.ActivityResultLauncher

/**
 * Dedicated Manager providing helper functions to prepare and request
 * Android system VPN permissions required by [PacketCaptureService] and [VpnService.Builder].
 */
object VpnPermissionManager {

  /**
   * Checks if the system VPN permission is already granted.
   *
   * @param context Application or Activity context.
   * @return `true` if the permission is already granted and no system dialog is needed, `false` otherwise.
   */
  fun isVpnPermissionGranted(context: Context): Boolean {
    return VpnService.prepare(context) == null
  }

  /**
   * Retrieves the system VPN preparation Intent if user consent is needed.
   *
   * @param context Application or Activity context.
   * @return An [Intent] to launch the system VPN dialog, or `null` if permission is already granted.
   */
  fun getPrepareVpnIntent(context: Context): Intent? {
    return VpnService.prepare(context)
  }

  /**
   * Helper function to check and request VPN permission.
   *
   * If permission is already granted, executes [onPermissionGranted] immediately.
   * Otherwise, launches the system VPN permission dialog via the provided [launcher].
   *
   * @param context Application or Activity context.
   * @param launcher ActivityResultLauncher configured to handle the [VpnService.prepare] Intent.
   * @param onPermissionGranted Callback executed immediately if permission was already granted.
   * @return `true` if permission was already granted, `false` if the permission request was launched.
   */
  fun requestVpnPermissionIfNeeded(
    context: Context,
    launcher: ActivityResultLauncher<Intent>,
    onPermissionGranted: () -> Unit
  ): Boolean {
    val prepareIntent = getPrepareVpnIntent(context)
    return if (prepareIntent != null) {
      launcher.launch(prepareIntent)
      false
    } else {
      onPermissionGranted()
      true
    }
  }
}
