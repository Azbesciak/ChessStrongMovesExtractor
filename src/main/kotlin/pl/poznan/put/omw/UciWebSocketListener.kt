package pl.poznan.put.omw

import mu.KLogging
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class UciWebSocketListener(
        private val engine: UciServerEngine,
        private val params: Params
) : WebSocketListener(), AutoCloseable {
    private companion object : KLogging() {
        const val MULTI_PV_PROP = "MultiPV"
        const val SIDE_PROP = "Analysis Contempt"
        const val SIDE_VALUE = "White"
    }

    private var isReady = false
    private var ws: WebSocket? = null

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logger.info("socket closed: ($code) $reason")
        ws = null
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logger.info("closing socket: ($code) $reason")
        closeWs()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.error(t) { "connection with websocket failure: $response" }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (text == "readyok") {
            isReady = true
            return
        } else if (!isReady) return
        logger.info("message: $text")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.info("socket opened with response $response")
        ws = webSocket
        setOptions()
        startNewConversation()
    }

    private fun setOptions() {
        // https://github.com/official-stockfish/Stockfish
        // http://wbec-ridderkerk.nl/html/UCIProtocol.html
        val options = engine.options.toMutableMap()
        val originalMultiPV = options[MULTI_PV_PROP]
        when {
            params.variationsNumber.wasSet -> updateMultiPvValueToUserPreference(options)
            originalMultiPV == null -> updateMultiPvValueToUserPreference(options)
            originalMultiPV.toIntOrNull()?.let { v -> v < 2 } ?: true -> {
                logger.error { "Invalid value was provided for engine options [$MULTI_PV_PROP: $originalMultiPV]" }
                updateMultiPvValueToUserPreference(options)
            }
        }
        options.computeIfAbsent(SIDE_PROP) { SIDE_VALUE }
        ws?.run {
            options.forEach { (option, value) ->
                send("setoption name $option value $value")
            }
        }
    }

    private fun startNewConversation() {
        ws?.run {
            send("stop")
            isReady = false
            send("isready")
        }
    }

    private fun updateMultiPvValueToUserPreference(options: MutableMap<String, String>) {
        options[MULTI_PV_PROP] = params.variationsNumber.value.toString()
    }

    override fun close() {
        ws?.runCatching {
            send("quit")
            isReady = false
            closeWs()
        }
    }

    private fun closeWs() {
        ws?.runCatching {
            close(1006, null)
            ws = null
        }
    }
}
