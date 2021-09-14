package com.example.w4_d1_basic_3d_app

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import android.graphics.Point
import android.net.Uri
import android.os.StrictMode
import android.util.Log
import android.view.View
import com.google.ar.core.Plane
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var arFrag: ArFragment
    private var viewRenderable: ViewRenderable? = null
    private var modelRenderable: ModelRenderable? = null

    private fun getScreenCenter(): Point {
        // find the root view of the activity
        val vw = findViewById<View>(android.R.id.content)
        // returns center of the screen as a Point object
        return Point(vw.width / 2, vw.height / 2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_add_tree.setOnClickListener {
            add3dObject()
        }

        arFrag = supportFragmentManager.findFragmentById(
            R.id.sceneform_fragment
        ) as ArFragment
         StrictMode.setThreadPolicy(
             StrictMode.ThreadPolicy.Builder()
            .permitAll().build())
         // (CC BY 4.0) Donated by Cesium for glTF testing.
         ModelRenderable.builder()
         .setSource(this, Uri.parse("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/CesiumMan/glTF/CesiumMan.gltf"))
             .setIsFilamentGltf(true)
             .setAsyncLoadEnabled(true)
             .setRegistryId("CesiumMan")
             .build()
             .thenAccept { modelRenderable = it }
             .exceptionally {
                 Log.e(TAG, "something went wrong ${it.localizedMessage}")
                 null
             }

        arFrag.setOnTapArPlaneListener { hitResult: HitResult?, _, _ ->
            viewRenderable ?: return@setOnTapArPlaneListener
            //Creates a new anchor at the hit location
            val anchor = hitResult!!.createAnchor()
            //Creates a new anchorNode attaching it to anchor
            val anchorNode = AnchorNode(anchor)
            // Add anchorNode as root scene node's child
            anchorNode.setParent(arFrag.arSceneView.scene)
            // Can be selected, rotated...
            val viewNode = TransformableNode(arFrag.transformationSystem)
            viewNode.renderable = viewRenderable
            // Add viewNode as anchorNode's child
            viewNode.setParent(anchorNode)
            // Sets this as the selected node in the TransformationSystem
            viewNode.select()

            viewNode.setOnTapListener { hitTestResult, motionEvent ->
                Log.e("txt","clicked")
            }
        }
    }

    private fun add3dObject() {
        val frame = arFrag.arSceneView.arFrame
        if (frame != null && modelRenderable != null) {
            val pt = getScreenCenter()
            val hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane) {
                    val anchor = hit!!.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFrag.arSceneView.scene)
                    val mNode =
                        TransformableNode(arFrag.transformationSystem)
                    mNode.renderable = modelRenderable
                    mNode.scaleController.minScale = 0.05f
                    mNode.scaleController.maxScale = 2.0f
                    mNode.localScale = Vector3(0.2f, 0.2f, 0.2f)
                    mNode.setParent(anchorNode)
                    mNode.select()
                    break
                }
            }
        }
    }
}