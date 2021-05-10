package com.innovattic.gdxar.check

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.core.exceptions.UnavailableException

/**
 * This fragment handles the camera permission, ARCore compatibility, version checks,
 * and ARCore installation if necessary.
 * Extend this class to have your Fragment show options depending on AR availability.
 */
abstract class ArCheckFragment : Fragment() {

    private var userRequestedInstall = true

    abstract fun onCameraPermissionDeny()

    abstract fun onArCoreUnavailable(availability: Availability)

    abstract fun onArCoreInstallFail(exception: UnavailableException)

    abstract fun onArCoreInstallSuccess()

    override fun onResume() {
        super.onResume()
        performCheck()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            for (i in permissions.indices) {
                if (permissions[i] == Manifest.permission.CAMERA &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED
                ) {
                    checkArCore()
                    return
                }
            }
            onCameraPermissionDeny()
        }
    }

    /**
     * Performs the whole check
     */
    fun performCheck() {
        if (requestCameraPermission()) {
            checkArCore()
        }
    }

    /**
     * Requests the camera permission, if necessary.
     * @return whether camera permission is already granted. If so, the permission won't be requested.
     */
    private fun requestCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION)
        return false
    }

    private fun checkArCore() {
        if (!isResumed) {
            return
        }
        val availability = ArCoreApk.getInstance().checkAvailability(activity)
        if (availability.isTransient) {
            requireView().postDelayed(AR_CORE_CHECK_INTERVAL) { checkArCore() }
            return
        }
        when (availability) {
            Availability.SUPPORTED_INSTALLED ->
                onArCoreInstallSuccess()
            Availability.SUPPORTED_APK_TOO_OLD,
            Availability.SUPPORTED_NOT_INSTALLED ->
                startArCoreInstallation()
            else ->
                onArCoreUnavailable(availability)
        }
    }

    private fun startArCoreInstallation() {
        try {
            val installStatus =
                ArCoreApk.getInstance().requestInstall(activity, userRequestedInstall)
            when (installStatus) {
                InstallStatus.INSTALLED -> onArCoreInstallSuccess()
                InstallStatus.INSTALL_REQUESTED,
                null ->
                    // Ensures next invocation of requestInstall() will either return
                    // INSTALLED or throw an exception.
                    userRequestedInstall = false
            }
        } catch (exception: UnavailableException) {
            onArCoreInstallFail(exception)
        }
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1
        private const val AR_CORE_CHECK_INTERVAL = 200L
    }
}
