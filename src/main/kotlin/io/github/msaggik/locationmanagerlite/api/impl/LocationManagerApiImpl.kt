package io.github.msaggik.locationmanagerlite.api.impl

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.annotation.RequiresPermission
import io.github.msaggik.locationmanagerlite.api.LocationManagerApi
import io.github.msaggik.locationmanagerlite.model.Response
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
 * Copyright 2025 Maxim Sagaciyang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Implementation of [LocationManagerApi] that provides location updates
 * using the Android LocationManager with coroutine support.
 *
 * @property context Application context used to access system services.
 * @property timeoutMillis Timeout in milliseconds to wait for a location update.
 * @property maxAgeMillis Maximum age in milliseconds for the cached last known location.
 */
class LocationManagerApiImpl(
    private val context: Context,
    private val timeoutMillis: Long,
    private val maxAgeMillis: Long
) : LocationManagerApi {

    /**
     * Attempts to retrieve the current location asynchronously.
     * First, tries to return the most recent cached location not older than [maxAgeMillis].
     * If no suitable cached location is available, requests a fresh single location update,
     * waiting up to [timeoutMillis].
     *
     * Requires either [android.Manifest.permission.ACCESS_FINE_LOCATION] or
     * [android.Manifest.permission.ACCESS_COARSE_LOCATION] permission.
     *
     * @return [Response.Success] with the location on success,
     * or [Response.Error] containing the error message on failure.
     */
    @RequiresPermission(anyOf = [
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    override suspend fun getLocation(): Response<Location> = runCatching {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val now = System.currentTimeMillis()

        findBestLast(lm, now)?.let { location ->
            return@runCatching location
        }

        requestSingleLocation(lm)
    }.fold(
        onSuccess = { location ->
            Response.Success(location)
        },
        onFailure = { e -> Response.Error("No data, exception=${e.message}") }
    )

    /**
     * Finds the best last known location from GPS and Network providers
     * that is not older than [maxAgeMillis].
     *
     * Requires either [android.Manifest.permission.ACCESS_FINE_LOCATION] or
     * [android.Manifest.permission.ACCESS_COARSE_LOCATION] permission.
     *
     * @param lm Android LocationManager instance.
     * @param now Current system time in milliseconds.
     * @return The most accurate last known location within max age, or null if none found.
     */
    @RequiresPermission(anyOf = [
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    private fun findBestLast(
        lm: LocationManager,
        now: Long
    ): Location? = listOfNotNull(
        lm.getLastKnownLocation(LocationManager.GPS_PROVIDER),
        lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    ).filter { now - it.time <= maxAgeMillis }.minByOrNull { it.accuracy }

    /**
     * Requests a single location update from enabled providers asynchronously,
     * suspending until the location is received or the timeout occurs.
     *
     * Requires either [android.Manifest.permission.ACCESS_FINE_LOCATION] or
     * [android.Manifest.permission.ACCESS_COARSE_LOCATION] permission.
     *
     * @param lm Android LocationManager instance.
     * @return The location obtained within the timeout period.
     * @throws TimeoutCancellationException if the location is not received in time.
     * @throws IllegalStateException if a location provider is disabled during the request.
     */
    @RequiresPermission(anyOf = [
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    private suspend fun requestSingleLocation(lm: LocationManager): Location =
        withTimeout(timeoutMillis) {

            val hasGps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!hasGps && !hasNetwork) {
                throw IllegalStateException("No location providers are enabled")
            }

            suspendCancellableCoroutine<Location> { cont ->
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (cont.isActive) {
                            cont.resume(location)
                            lm.removeUpdates(this)
                        }
                    }

                    override fun onProviderEnabled(p: String) {}
                    override fun onProviderDisabled(p: String) {
                        if (cont.isActive) {
                            cont.resumeWithException(
                                IllegalStateException("Provider $p disabled")
                            )
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(
                        provider: String?,
                        status: Int,
                        extras: Bundle?
                    ) {}
                }

                val looper = Looper.getMainLooper()
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0L, 0f, locationListener, looper
                    )
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener, looper
                    )
                }

                cont.invokeOnCancellation {
                    cleanListener(lm, locationListener)
                }
            }
        }

    /**
     * Removes the location updates listener from the LocationManager.
     *
     * @param lm Android LocationManager instance.
     * @param locListener The LocationListener to be removed.
     */
    private fun cleanListener(
        lm: LocationManager,
        locListener: LocationListener
    ) {
        lm.removeUpdates(locListener)
    }
}