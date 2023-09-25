@file:JvmName("HttpClientCacheJVM")
package io.realm.kotlin.mongodb.internal

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*

/**
 * Cache HttpClient on Android and JVM.
 * https://github.com/realm/realm-kotlin/issues/480 only seem to be a problem on macOS.
 */
internal actual class HttpClientCache actual constructor(timeoutMs: Long, customLogger: Logger?) {
    private val httpClient: HttpClient by lazy { createClient(timeoutMs, customLogger) }
    actual fun getClient(): HttpClient {
        return httpClient
    }
    actual fun close() {
        httpClient.close()
    }
}

public actual fun createPlatformClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    // Revert to OkHttp when https://youtrack.jetbrains.com/issue/KTOR-6266 is fixed
    return HttpClient(CIO) {
        this.apply(block)
    }
}
