package com.example.findu.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToLong

object LocationUtils {
    private fun getFusedLocationClient(context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * 检查系统定位服务是否已开启
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        return suspendCancellableCoroutine { continuation ->
            val client = getFusedLocationClient(context)
            // 使用 Priority.PRIORITY_HIGH_ACCURACY 获取高精度位置
            // CancellationTokenSource 用于取消操作
            val cancellationTokenSource = CancellationTokenSource()
            
            client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = (location.latitude * 100000).roundToLong() / 100000.0
                    val longitude = (location.longitude * 100000).roundToLong() / 100000.0
                    continuation.resume(Pair(latitude, longitude))
                } else {
                    // 如果无法获取最新位置（例如模拟器尚未有GPS fix），尝试获取最后已知位置
                    client.lastLocation.addOnSuccessListener { lastLocation: Location? ->
                        if (lastLocation != null) {
                            val latitude = (lastLocation.latitude * 100000).roundToLong() / 100000.0
                            val longitude = (lastLocation.longitude * 100000).roundToLong() / 100000.0
                            continuation.resume(Pair(latitude, longitude))
                        } else {
                            continuation.resume(null)
                        }
                    }.addOnFailureListener {
                        // lastLocation 也失败，返回 null
                        continuation.resume(null)
                    }
                }
            }.addOnFailureListener { throwable: Throwable ->
                continuation.resumeWithException(throwable)
            }
            
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }
}
