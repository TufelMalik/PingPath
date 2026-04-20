package com.techquantum.pingpath.modules.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    userLocation: GeoPoint? = null,
    destination: GeoPoint? = null,
    alertRadius: Double? = null, // in meters
    onMapClick: ((GeoPoint) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize OSMDroid configuration
    Configuration.getInstance().userAgentValue = context.packageName

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            if (userLocation != null) {
                controller.setCenter(userLocation)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.overlays.clear()
            
            if (onMapClick != null) {
                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        if (p != null) onMapClick(p)
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean { return false }
                })
                view.overlays.add(mapEventsOverlay)
            }
            
            // User Location Marker
            if (userLocation != null) {
                val userMarker = Marker(view).apply {
                    position = userLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "You are here"
                }
                view.overlays.add(userMarker)
            }

            // Destination Marker
            if (destination != null) {
                val destMarker = Marker(view).apply {
                    position = destination
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Destination"
                }
                view.overlays.add(destMarker)
            }

            // Alert Zone Circle Overlay
            if (destination != null && alertRadius != null) {
                val polygon = Polygon(view).apply {
                    points = Polygon.pointsAsCircle(destination, alertRadius)
                    fillPaint.color = android.graphics.Color.argb(50, 0, 255, 0)
                    outlinePaint.color = android.graphics.Color.GREEN
                    outlinePaint.strokeWidth = 2f
                }
                view.overlays.add(polygon)
            }
            
            if (userLocation != null && destination == null) {
                 view.controller.setCenter(userLocation)
            }

            view.invalidate()
        }
    )
}
