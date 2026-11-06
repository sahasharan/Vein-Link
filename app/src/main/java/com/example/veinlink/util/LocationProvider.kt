
package com.example.veinlink.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val TAG = "LocationProvider"

    suspend fun getCurrentLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Location permission not granted.")
            return null
        }

        // Try to get the last known location first. It's faster.
        val lastLocation = suspendCancellableCoroutine<Location?> { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }

        if (lastLocation != null) {
            Log.d(TAG, "Returning last known location: $lastLocation")
            return lastLocation
        }

        Log.d(TAG, "Last known location was null. Requesting a fresh location.")
        // If last location is null, request a fresh location.
        return suspendCancellableCoroutine { continuation ->
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                interval = 10000
                fastestInterval = 5000
                numUpdates = 1
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    val location = locationResult.lastLocation
                    if (location != null) {
                        Log.d(TAG, "Successfully fetched a new location: $location")
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    } else {
                        Log.w(TAG, "New location result was null.")
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (!locationAvailability.isLocationAvailable) {
                        Log.w(TAG, "Location is not available.")
                        fusedLocationClient.removeLocationUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            continuation.invokeOnCancellation {
                Log.d(TAG, "Location request cancelled.")
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}
