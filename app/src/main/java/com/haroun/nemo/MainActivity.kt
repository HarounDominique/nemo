package com.haroun.nemo

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración básica de osmdroid
        Configuration.getInstance().userAgentValue = packageName

        // Solicita permisos de ubicación (no bloqueante aquí)
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* puedes manejar resultados si quieres */ }

        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            MapScreen()
        }
    }
}

@Composable
fun MapScreen() {
    val ctx = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var markerRef by remember { mutableStateOf<Marker?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // Crear MapView
            val mapView = MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(40.4168, -3.7038)) // Madrid por defecto
            }

            // Crear marcador inicial
            val marker = Marker(mapView).apply {
                position = GeoPoint(40.4168, -3.7038)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Ejemplo: mascota perdida aquí 🐾"
            }
            mapView.overlays.add(marker)

            mapViewRef = mapView
            markerRef = marker

            // GestureDetector para detectar single taps sobre el MapView
            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    try {
                        // Convierte coordenadas de pantalla a GeoPoint
                        val projection = mapView.projection
                        val x = e.x.toInt()
                        val y = e.y.toInt()
                        val geoPoint = projection.fromPixels(x, y) as GeoPoint

                        // Mueve el marcador y centra el mapa
                        markerRef?.position = geoPoint
                        mapView.controller.animateTo(geoPoint)
                        mapView.invalidate()
                    } catch (t: Throwable) {
                        // evita crashes por conversiones inesperadas
                        t.printStackTrace()
                    }
                    return true
                }
            })

            // Asignar touch listener que pase eventos al GestureDetector
            mapView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                // Importante: devolver false para que el MapView también procese gestos (zoom, pan)
                false
            }

            mapView
        },
        update = { mapView ->
            // Si necesitas actualizar referencias desde Compose, o restaurar estado, aquí
            // (por ejemplo, mover marcador según un state externo)
            // mapViewRef = mapView
        }
    )
}
