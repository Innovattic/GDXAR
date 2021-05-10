package com.innovattic.gdxarsample.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.exceptions.UnavailableException
import com.innovattic.gdxar.check.ArCheckFragment
import com.innovattic.gdxarsample.R
import com.innovattic.gdxarsample.extension.TAG
import kotlinx.android.synthetic.main.fragment_ar_check.*

class IntroFragment : ArCheckFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_ar_check, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toArButton.visibility = View.GONE
        toArButton.setOnClickListener {
            findNavController().navigate(IntroFragmentDirections.actionArCheckFragmentToHelloArFullFragment())
        }
        toNonArButton.setOnClickListener {
            findNavController().navigate(IntroFragmentDirections.actionArCheckFragmentToHelloNonArFragment())
        }
    }

    override fun onCameraPermissionDeny() {
        messageView.text = "Camera permission denied!"
    }

    override fun onArCoreUnavailable(availability: Availability) {
        messageView.text = "ARCore is not available: $availability"
    }

    override fun onArCoreInstallFail(exception: UnavailableException) {
        Log.e(TAG, "Could not install ARCore", exception)
        messageView.text = "Could not install ARCore: $exception"
    }

    override fun onArCoreInstallSuccess() {
        messageView.text = "ARCore is ready"
        toArButton.visibility = View.VISIBLE
    }
}
