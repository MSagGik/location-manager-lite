package io.github.msaggik.locationmanagerlite.model

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
 * Represents a result wrapper that can be either a success with nullable data or an error with a message.
 *
 * This sealed interface models an operation outcome, commonly used for handling responses
 * from network calls, database operations, or any asynchronous processes.
 *
 * Example:
 * ```
 * fun loadData(): Response<String> {
 *     return try {
 *         val data = "Example data from local source"
 *         Response.Success(data)
 *     } catch (e: Exception) {
 *         Response.Error(e.message)
 *     }
 * }
 * ```
 *
 * @param T The type of the successful result data.
 */
sealed interface Response<out T> {

    /**
     * Represents a successful result, possibly containing null data.
     *
     * @param data The successful result data, may be null.
     */
    data class Success<out T>(val data: T?) : Response<T>

    /**
     * Represents an error result containing an error message.
     *
     * @param message A description of the error.
     */
    data class Error<out T>(val message: String?) : Response<T>
}

/**
 * Extracts the data and error message from a [Response] as a pair.
 *
 * @return A [Pair] where the first value is the data (or null) and the second is the error message (or null).
 *
 * Example:
 * ```
 * val (value, error) = response.toDataAndError()
 * if (value != null) println("Data: $value")
 * if (error != null) println("Error: $error")
 * ```
 */
fun <T> Response<T>.toDataAndError(): Pair<T?, String?> = when (this) {
    is Response.Success -> data to null
    is Response.Error -> null to message
}

/**
 * Maps the (nullable) data of a successful [Response] to a new type.
 *
 * @param transform A function to transform the nullable data.
 * @return A new [Response] of type [R].
 *
 * Example:
 * ```
 * val input: Response<String?> = Response.Success(null)
 * val result = input.map { it?.length } // Response.Success(null)
 * ```
 */
fun <T, R> Response<T>.map(transform: (T) -> R): Response<R> = when (this) {
    is Response.Success -> Response.Success(transform(data ?: throw IllegalStateException("data in Response is null")))
    is Response.Error -> Response.Error(message ?: throw IllegalStateException("message in Response is null"))
}

/**
 * Applies one of two functions depending on whether this [Response] is [Success] or [Error].
 *
 * @param onSuccess Function to apply if [Success].
 * @param onError Function to apply if [Error].
 * @return The result of the applied function.
 *
 * Example:
 * ```
 * val output = response.fold(
 *     onSuccess = { "Loaded: ${it.orEmpty()}" },
 *     onError = { "Failed: ${it.orEmpty()}" }
 * )
 * println(output)
 * ```
 */
inline fun <T, R> Response<T>.fold(
    onSuccess: (T?) -> R,
    onError: (String?) -> R
): R = when (this) {
    is Response.Success -> onSuccess(data)
    is Response.Error -> onError(message)
}

/**
 * Executes [action] if this [Response] is [Success].
 *
 * @param action The function to invoke with the (nullable) data.
 * @return This [Response] unchanged.
 *
 * Example:
 * ```
 * response.onSuccess { println("Success: $it") }
 * ```
 */
inline fun <T> Response<T>.onSuccess(action: (T?) -> Unit): Response<T> {
    if (this is Response.Success) action(data)
    return this
}

/**
 * Executes [action] if this [Response] is [Error].
 *
 * @param action The function to invoke with the (nullable) message.
 * @return This [Response] unchanged.
 *
 * Example:
 * ```
 * response.onError { println("Error: $it") }
 * ```
 */
inline fun <T> Response<T>.onError(action: (String?) -> Unit): Response<T> {
    if (this is Response.Error) action(message)
    return this
}

/**
 * Returns the successful data or `null` if this is an [Error].
 *
 * Example:
 * ```
 * val result = response.getOrNull()
 * if (result != null) println("Data is $result")
 * ```
 */
fun <T> Response<T>.getOrNull(): T? = when (this) {
    is Response.Success -> data
    is Response.Error -> null
}

/**
 * Returns the successful data, or the result of [default] if this is an [Error] or data is null.
 *
 * @param default A lambda providing the default value.
 * @return The data or the fallback value.
 *
 * Example:
 * ```
 * val data = response.getOrElse { "Default value" }
 * println(data)
 * ```
 */
fun <T> Response<T>.getOrElse(default: () -> T): T = when (this) {
    is Response.Success -> data ?: default()
    is Response.Error -> default()
}