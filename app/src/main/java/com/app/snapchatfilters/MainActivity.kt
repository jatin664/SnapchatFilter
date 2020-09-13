package com.app.snapchatfilters

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    private var faceRenderable : ModelRenderable? = null
    private var faceTexture : Texture? = null
    private var faceNodeMap = HashMap<AugmentedFace,AugmentedFaceNode>()
    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = fragment as ArFragment

        loadModel()

        arFragment.arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST //make camera renders before moels

        arFragment.arSceneView.scene.addOnUpdateListener {
            if(faceRenderable!=null && faceTexture!=null){
                addTrackingToFace()
                removeUntrackedFaces()
            }
        }
    }

    private fun loadModel(){
        val modelRenderable = ModelRenderable.Builder().apply {
            setSource(this@MainActivity,R.raw.fox_face)
        }.build()

        val texture = Texture.builder().apply {
            setSource(this@MainActivity,R.drawable.clown_face_mesh_texture)
        }.build()

        //they both are CompletableFuture because they are loaded in the background and we can get
        //result when loading is finished
        CompletableFuture.allOf(modelRenderable,texture).thenAccept {
            faceRenderable = modelRenderable.get().apply {
                //disable shadows
                isShadowCaster = false
                isShadowCaster = false //other models don't apply their shadow
            }
            faceTexture = texture.get()
        }.exceptionally {
            Toast.makeText(this@MainActivity, "Error loading models $it", Toast.LENGTH_SHORT).show()
            null
        }
    }

    /*
        Hashmap for key value pairs of augumented face and augumented nodes.
        augumented face contains info about a face and augumented nodes will contains position,
        rotation, scale etc of the face so augumented nodes are there to place the face at the right
        position so we get augumented face then will be augumented nodes for that
     */

    private fun addTrackingToFace(){
        val session = arFragment.arSceneView.session ?: return

        //all augmented faces currently being detected
        val facelist = session.getAllTrackables(AugmentedFace::class.java)

        for(face in facelist){
            if(!faceNodeMap.contains(face)){
                //create new face node
                AugmentedFaceNode(face).apply {
                    setParent(arFragment.arSceneView.scene)
                    faceRegionsRenderable = faceRenderable
                    faceMeshTexture = faceTexture
                    faceNodeMap[face] = this
                }
            }
        }

    }

    private fun removeUntrackedFaces(){
        val entries = faceNodeMap.entries

        for(entry in entries){
            val face = entry.key //save current face
            if(face.trackingState == TrackingState.STOPPED){ //check if we are tracking
                val faceNode = entry.value
                faceNode.setParent(null) //remove parent
                entries.remove(entry)
            }
        }

    }

}