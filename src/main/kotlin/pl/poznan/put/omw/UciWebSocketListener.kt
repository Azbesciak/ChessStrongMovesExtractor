package pl.poznan.put.omw

import mu.KLogging
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class UciWebSocketListener(
        private val engine: UciServerEngine,
        private val params: Params
) : WebSocketListener() {
    private companion object : KLogging() {
        const val MULTI_PV_PROP = "MultiPV"
        const val SIDE_PROP = "Analysis Contempt"
        const val SIDE_VALUE = "White"
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logger.info("socket closed: ($code) $reason")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logger.info("closing socket: ($code) $reason")
        webSocket.close(100, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.error(t) { "connection with websocket failure: $response" }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        logger.info("message: $text")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.info("socket opened with response $response")
        setOptions(webSocket)
    }

    private fun setOptions(webSocket: WebSocket) {
        // https://github.com/official-stockfish/Stockfish
        val options = engine.options.toMutableMap()
        when {
            params.variationsNumber.wasSet -> options[MULTI_PV_PROP] = params.variationsNumber.value.toString()
            options[MULTI_PV_PROP]?.let { it.toIntOrNull()?.let { v -> v < 2 } } ?: true -> {
                logger.error { "Invalid value was provided for engine options [$MULTI_PV_PROP]" }
                options[MULTI_PV_PROP] = params.variationsNumber.value.toString()
            }
        }
        options.computeIfAbsent(SIDE_PROP) { SIDE_VALUE }
        options.forEach { (option, value) ->
            webSocket.send("setoption name $option value $value")
        }
    }
}
