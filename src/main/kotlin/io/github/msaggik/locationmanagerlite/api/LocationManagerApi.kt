package io.github.msaggik.locationmanagerlite.api

import android.Manifest
import android.location.Location
import androidx.annotation.RequiresPermission
import io.github.msaggik.locationmanagerlite.model.Response

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
 * Interface defining the contract for location retrieval.
 *
 * This API provides a suspend function to asynchronously obtain the current location.
 * It returns a [Response] wrapping the [Location] data or an error.
 *
 * Permissions:
 * Requires either [Manifest.permission.ACCESS_FINE_LOCATION] or [Manifest.permission.ACCESS_COARSE_LOCATION]
 * to be granted before calling [getLocation].
 *
 * Implementations should handle location requests respecting the Android permissions model.
 */
interface LocationManagerApi {

    /**
     * Retrieves the current location asynchronously.
     *
     * @return a [Response] object containing the current [Location] on success,
     * or an error message on failure.
     *
     * @throws SecurityException if the required location permissions are not granted.
     */
    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    suspend fun getLocation(): Response<Location>
}