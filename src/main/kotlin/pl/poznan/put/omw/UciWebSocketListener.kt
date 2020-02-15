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
        val options = when {
            params.variationsNumber.wasSet -> engine.options + (MULTI_PV_PROP to params.variationsNumber.value)
            engine.options[MULTI_PV_PROP]?.let { it.toIntOrNull()?.let { v -> v < 2 } } ?: true -> {
                logger.error { "Invalid value was provided for engine options [$MULTI_PV_PROP]" }
                engine.options + (MULTI_PV_PROP to params.variationsNumber.value)
            }
            else -> engine.options
        }
        options.forEach { (option, value) ->
            webSocket.send("setoption name $option value $value")
        }
    }
}
