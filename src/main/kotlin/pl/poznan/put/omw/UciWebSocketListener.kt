package pl.poznan.put.omw

import mu.KLogging
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class UciWebSocketListener(
        private val engine: UciServerEngine
) : WebSocketListener() {
    private companion object : KLogging()

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
        engine.options.forEach { (option, value) ->
            webSocket.send("setoption name $option value $value")
        }
        webSocket.send("position fen r4rk1/pp5p/2p2ppB/3pP3/2P2Q2/P1N2P2/1q4PP/n4R1K w - - 0 21")
        webSocket.send("go depth 30")
    }
}
