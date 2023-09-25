package io.realm.kotlin.internal.interop.sync

import io.realm.kotlin.internal.interop.RealmInterop
import io.realm.kotlin.internal.interop.RealmWebsocketProviderPointer
import io.realm.kotlin.internal.interop.RealmWebsocketHandlerCallbackPointer
import kotlinx.coroutines.Job

interface WebSocketTransport {
    fun post(handlerCallback: RealmWebsocketHandlerCallbackPointer)

    fun createTimer(
        delayInMilliseconds: Long,
        handlerCallback: RealmWebsocketHandlerCallbackPointer
    ): CancellableTimer

    fun connect(
        observer: WebSocketObserver,
        path: String,
        address: String,
        port: Long,
        isSsl: Boolean,
        numProtocols: Long,
        supportedProtocols: String
    ): WebSocketClient

    fun write(
        webSocketClient: WebSocketClient,
        data: ByteArray,
        length: Long,
        handlerCallback: RealmWebsocketHandlerCallbackPointer
    )

    fun runCallback(
        handlerCallback: RealmWebsocketHandlerCallbackPointer,
        status: Int = 0/* ok */,
        reason: String = ""
    ) {
        RealmInterop.realm_sync_socket_callback_complete(
            handlerCallback,
            cancelled = false,
            status,
            reason
        )
    }

    fun close()
}

class CancellableTimer(
    private val job: Job,
    private val handlerCallback: RealmWebsocketHandlerCallbackPointer
) {
    fun cancel() {
        // avoid double delete, if the Job has completed then the callback function was already been invoked and deleted from the heap
        if (!job.isCompleted && !job.isCancelled) {
            job.cancel()
            RealmInterop.realm_sync_socket_callback_complete(handlerCallback, cancelled = true)
        }
    }
}

interface WebSocketClient {
    fun send(message: ByteArray, handlerCallback: RealmWebsocketHandlerCallbackPointer)
    fun closeWebsocket()
}

class WebSocketObserver(private val webSocketObserverPointer: RealmWebsocketProviderPointer) {
    fun onConnected(protocol: String) {
        RealmInterop.realm_sync_socket_websocket_connected(webSocketObserverPointer, protocol)
    }

    fun onError() {
        RealmInterop.realm_sync_socket_websocket_error(webSocketObserverPointer)
    }

    fun onNewMessage(data: ByteArray) {
        RealmInterop.realm_sync_socket_websocket_message(webSocketObserverPointer, data)
    }

    fun onClose(wasClean: Boolean, errorCode: Int, reason: String) {
        RealmInterop.realm_sync_socket_websocket_closed(
            webSocketObserverPointer,
            wasClean,
            errorCode,
            reason
        )
    }
}