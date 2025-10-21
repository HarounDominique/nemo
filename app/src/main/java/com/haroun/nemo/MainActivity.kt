package com.haroun.nemo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            MapScreen(
                onBack = { finish() },
                onAccept = { geo ->
                    val resultIntent = Intent().apply {
                        putExtra("latitude", geo.latitude)
                        putExtra("longitude", geo.longitude)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: (() -> Unit)? = null, onAccept: ((GeoPoint) -> Unit)? = null) {
    val ctx = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var markerRef by remember { mutableStateOf<Marker?>(null) }
    var selectedLocation by remember { mutableStateOf(GeoPoint(40.4168, -3.7038)) } // Madrid

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Seleccionar ubicación") },
            navigationIcon = {
                IconButton(onClick = {
                    onBack?.invoke() ?: run { (ctx as? ComponentActivity)?.finish() }
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val mapView = MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(selectedLocation)
                    }

                    val marker = Marker(mapView).apply {
                        position = selectedLocation
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Ubicación de la pérdida 🐾"
                    }
                    mapView.overlays.add(marker)

                    mapViewRef = mapView
                    markerRef = marker

                    val gestureDetector =
                        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                                val projection = mapView.projection
                                val geoPoint =
                                    projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                                marker.position = geoPoint
                                mapView.controller.animateTo(geoPoint)
                                mapView.invalidate()
                                selectedLocation = geoPoint
                                return true
                            }
                        })

                    mapView.setOnTouchListener { _, event ->
                        gestureDetector.onTouchEvent(event)
                        false
                    }

                    mapView
                }
            )
        }

        // 🔹 Fila con botones "Volver" y "Aceptar ubicación"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onBack?.invoke() ?: run { (ctx as? ComponentActivity)?.finish() } },
                modifier = Modifier.weight(1f)
            ) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    onAccept?.invoke(selectedLocation)
                    if (onAccept == null) (ctx as? ComponentActivity)?.finish()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Aceptar ubicación")
            }
        }
    }
}
