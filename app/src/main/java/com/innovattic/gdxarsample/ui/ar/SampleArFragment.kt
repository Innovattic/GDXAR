package com.innovattic.gdxarsample.ui.ar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.innovattic.gdxar.ar.ArSceneFragment
import com.innovattic.gdxarsample.R
import kotlinx.android.synthetic.main.fragment_ar.*

class SampleArFragment : ArSceneFragment() {
    override val scene = SampleArScene(::onLoadingChange)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arView = createArSceneView()
        arContainer.addView(arView)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun onLoadingChange(isLoading: Boolean) {
        // Loading change is not fired on UI thread
        requireActivity().runOnUiThread {
            messageView?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
