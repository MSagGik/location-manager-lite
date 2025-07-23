package io.github.msaggik.locationmanagerlite

import android.content.Context
import io.github.msaggik.locationmanagerlite.api.LocationManagerApi
import io.github.msaggik.locationmanagerlite.api.impl.LocationManagerApiImpl

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
 * Lightweight location manager wrapper providing access to location services.
 *
 * This class acts as a factory and configuration holder for [LocationManagerApi] implementation.
 * It manages default parameters such as timeout duration and maximum acceptable age of cached location data.
 *
 * @property context Android context required to access system location services.
 * @property timeoutMillis Timeout in milliseconds for location requests. Default is 10,000 ms.
 * @property maxAgeMillis Maximum age in milliseconds for cached location data to be considered valid. Default is 60,000 ms.
 */
class LocationManagerLite(
    private val context: Context,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT,
    private val maxAgeMillis: Long = DEFAULT_MAX_AGE_DATA_LOCATION
) {

    /**
     * Lazily initialized [LocationManagerApi] implementation.
     */
    private val locationManager: LocationManagerApi by lazy {
        LocationManagerApiImpl(
            context = context.applicationContext,
            timeoutMillis = timeoutMillis,
            maxAgeMillis = maxAgeMillis
        )
    }

    /**
     * Provides an instance of [LocationManagerApi] for location retrieval.
     *
     * @return configured [LocationManagerApi] instance.
     */
    fun provide(): LocationManagerApi = locationManager

    companion object {
        private const val DEFAULT_TIMEOUT = 10_000L
        private const val DEFAULT_MAX_AGE_DATA_LOCATION = 60_000L
    }
}