package io.github.fcastell.qrdpc.restrictions

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import io.github.fcastell.qrdpc.payload.QrPayload
import io.github.fcastell.qrdpc.payload.toApplicationRestrictionsBundle

private const val TAG = "RestrictionsApplier"

/**
 * Applies [payload]'s restrictions to its target package via the delegated
 * `DevicePolicyManager.setApplicationRestrictions` call (admin component `null`), which
 * requires this app to already hold the `APP_RESTRICTIONS` delegation scope for that
 * package from the active device policy controller.
 */
fun applyRestrictions(
    context: Context,
    payload: QrPayload,
): ApplyResult {
    if (!isPackageInstalled(context, payload.packageName)) {
        return ApplyResult.PackageNotInstalled
    }

    val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
    return try {
        devicePolicyManager.setApplicationRestrictions(
            null,
            payload.packageName,
            payload.toApplicationRestrictionsBundle(),
        )
        ApplyResult.Success
    } catch (e: SecurityException) {
        Log.w(TAG, "APP_RESTRICTIONS delegation missing for ${payload.packageName}", e)
        ApplyResult.DelegationNotGranted
    } catch (e: IllegalArgumentException) {
        ApplyResult.OtherFailure(e.message ?: "Restrictions were rejected by the system.")
    }
}

private fun isPackageInstalled(
    context: Context,
    packageName: String,
): Boolean =
    try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        Log.d(TAG, "Target package $packageName is not installed", e)
        false
    }
