package com.app.snapchatfilters

import android.os.Bundle
import android.view.View
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.util.*

class FaceArFragment : ArFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        planeDiscoveryController.hide() // remove those detected planes
        planeDiscoveryController.setInstructionView(null) // remove white hand
    }

    override fun getSessionFeatures(): MutableSet<Session.Feature> {
        return EnumSet.of(Session.Feature.FRONT_CAMERA) //we will use front camera
    }

    override fun getSessionConfiguration(session: Session?): Config {
        return Config(session).apply {
            augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        }
    }

}