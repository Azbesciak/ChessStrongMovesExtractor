package pl.poznan.put.omw

import mu.KLogging
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class UciWebSocketListener(
        private val engine: UciServerEngine,
        private val params: Params,
        private val onReady: () -> Unit
) : WebSocketListener(), AutoCloseable {
    private companion object : KLogging() {
        const val MULTI_PV_PROP = "MultiPV"
        const val SIDE_PROP = "Analysis Contempt"
        const val SIDE_VALUE = "White"

        private class MessagesConsumer(
                val id: Long,
                val fenPosition: String,
                val onMessage: (String) -> Unit
        )
    }

    private var gameState = GameState()
    private var ws: WebSocket? = null
    private var messagesConsumer: MessagesConsumer? = null

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
        logger.debug { "state: $gameState, message: $text" }
        when {
            text == "readyok" -> {
                gameState.isReady = true
                messagesConsumer?.let {
                    if (it.id == gameState.moveId) {
                        webSocket.send("position fen ${it.fenPosition}")
                        webSocket.send("go depth ${params.engineDepth}")
                    }
                }
                return
            }
            !gameState.isReady || messagesConsumer == null -> return
            messagesConsumer?.id != gameState.moveId -> return
        }
        messagesConsumer!!.onMessage(text)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.info("socket opened with response $response")
        ws = webSocket
        setOptions()
        startNewConversation()
        onReady()
    }

    fun newGame(): GameConnection {
        requireNotNull(ws) { "uci web socket is not available" }
        gameState.newGame()
        ws?.run {
            messagesConsumer = null
            send("ucinewgame")
            startNewConversation()
        }
        val thisGameId = gameState.gameId
        return GameConnection(
                gameId = thisGameId,
                nextPosition = { position, callback ->
                    if (gameState.gameId != thisGameId) return@GameConnection
                    ws?.run {
                        messagesConsumer = MessagesConsumer(gameState.newMove(), position, callback)
                        startNewConversation()
                    }
                },
                close = {
                    if (gameState.gameId != thisGameId) return@GameConnection
                    gameState.newGame()
                    messagesConsumer = null
                    startNewConversation()
                }
        )
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
            gameState.isReady = false
            send("isready")
        }
    }

    private fun updateMultiPvValueToUserPreference(options: MutableMap<String, String>) {
        options[MULTI_PV_PROP] = params.variationsNumber.value.toString()
    }

    override fun close() {
        ws?.runCatching {
            send("quit")
            gameState.isReady = false
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

class GameConnection(
        val gameId: Long,
        val nextPosition: (positionCommand: String, responseCallback: (String) -> Unit) -> Unit,
        val close: () -> Unit
)

private data class GameState(
        var isReady: Boolean = false,
        var gameId: Long = 0,
        var moveId: Long = 0
) {
    fun newGame(): Long {
        ++moveId
        isReady = false
        return ++gameId
    }

    fun newMove(): Long {
        isReady = false
        return ++moveId
    }
}
