package com.example.findu.ui.components

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.maps.MapView

/**
 * Jetpack Compose wrapper for AMap MapView.
 * Correctly handles the lifecycle of the MapView.
 */
@Composable
fun AmapView(
    onMapLoaded: (MapView) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Remember the MapView instance so it survives recompositions
    val mapView = remember {
        MapView(context)
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> {} // No separate onStart for MapView usually needed if onCreate called
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> {} // No separate onStop
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Note: onDestroy is called by the lifecycle observer
        }
    }

    // Call onMapLoaded when the view is attached
    // We use AndroidView to display the MapView
    AndroidView(
        factory = {
            // Initial setup if needed, but onCreate is handled by Lifecycle observer.
            // However, for the very first time, we might need to ensure it's created if the lifecycle state is already started.
            // But usually, adding observer triggers current state. 
            // Safer to call onCreate here if we are not sure about the initial state, 
            // but MapView documentation says onCreate(Bundle) must be called.
            
            // A common pattern for MapView in Compose:
            // The factory block is called once. 
            // We can rely on the lifecycle observer to call onCreate/onResume if we add it early enough.
            // Or we can manually call them here if the lifecycle is already at least CREATED.
            
            // To be safe and simple: Let the LifecycleEventObserver handle it, 
            // but for the immediate display, we manually trigger onCreate if needed?
            // Actually, simply adding the observer might not replay ON_CREATE if we are already RESUMED.
            // Let's manually sync the state based on current lifecycle state.
            
            val lifecycleState = lifecycleOwner.lifecycle.currentState
            if (lifecycleState.isAtLeast(Lifecycle.State.CREATED)) {
                 mapView.onCreate(Bundle())
            }
            if (lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
                mapView.onResume()
            }
            
            onMapLoaded(mapView)
            mapView
        }
    )
}
