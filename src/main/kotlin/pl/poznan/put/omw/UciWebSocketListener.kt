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
                val onMessage: (String) -> Unit,
                var wasSend: Boolean = false
        )
    }

    private var gameState = GameState()
    @Volatile
    private var ws: WebSocket? = null
    @Volatile
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
                messagesConsumer?.let {
                    gameState.isReady = true
                    if (it.id == gameState.moveId && !it.wasSend) {
                        logger.debug("sending position to engine ${it.fenPosition}")
                        webSocket.send("position fen ${it.fenPosition}")
                        webSocket.send("go depth ${params.engineDepth}")
                        it.wasSend = true
                    }
                } ?: logger.debug("engine was ready, but no messenger pressent")
                return
            }
            !gameState.isReady && messagesConsumer != null -> {
                startNewConversation() // we are telling engine to just shut up.
                return
            }
            messagesConsumer == null -> return
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
        logger.debug("new game ${gameState.gameId} started")
        ws?.run {
            messagesConsumer = null
            send("ucinewgame")
            startNewConversation()
        }
        val thisGameId = gameState.gameId
        return GameConnection(
                gameId = thisGameId,
                nextPosition = { position, callback ->
                    if (gameState.gameId != thisGameId) return@GameConnection {}
                    val moveId = gameState.newMove()
                    messagesConsumer = MessagesConsumer(moveId, position, callback)
                    startNewConversation()
                    var canceled = false
                    result@{
                        logger.debug("cancel for move $moveId required")
                        if (canceled) return@result
                        if (messagesConsumer?.let { it.id == moveId } == true) {
                            canceled = true
                            messagesConsumer = null
                            startNewConversation()
                            logger.debug("move $moveId cancellation send")
                        }
                    }
                },
                close = {
                    logger.debug("closing game...")
                    if (gameState.gameId != thisGameId) return@GameConnection
                    gameState.newGame()
                    messagesConsumer = null
                    startNewConversation()
                    logger.debug("game $thisGameId closed")
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
            logger.debug("sending move reset request")
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
        val nextPosition: (positionCommand: String, responseCallback: (String) -> Unit) -> Cancellation,
        val close: () -> Unit
)
typealias Cancellation = () -> Unit

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
