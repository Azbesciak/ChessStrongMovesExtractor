package pl.poznan.put.omw

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import mu.KLogging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


const val AUTH_PATH = "/user/login"
const val ENGINE_START_PATH = "/engine/start"
const val WEBSOCKET_PATH = "/ws_engine"
val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

fun main(args: Array<String>) = ProgramExecutor {
    val game = ChessBoardReader.getGames(inputPath)
    val mainPathMovesGame = VariantMovesFilter.filter(game)
    val client = OkHttpClient()
    val json = Json(JsonConfiguration.Stable)
    val uciServerConfig = readServerConfig(json)
    println(uciServerConfig)
    println(mainPathMovesGame)
    val authResult = client.authorize(uciServerConfig, json)
    client.startEngine(uciServerConfig, authResult, json)
}.main(args)

private fun Params.readServerConfig(json: Json) =
        ServerConfigReader(json) read uciServerConfigPath

private fun OkHttpClient.authorize(uciServerConfig: UciServerConfig, json: Json): AuthorizationResponse {
    val credentials = Credentials(uciServerConfig.login, uciServerConfig.password)
    val body = credentials.toRequest(Credentials.serializer(), json)
    val request = request {
        url(uciServerConfig.url + AUTH_PATH).post(body)
    }
    return execute(request) {
        val authResult = json.parse(AuthorizationResponse.serializer(), it)
        require(authResult.success) { "Authorization failed for $credentials" }
        authResult
    }
}

private fun OkHttpClient.startEngine(uciServerConfig: UciServerConfig, authResult: AuthorizationResponse, json: Json) {
    val body = EngineStartCommand(uciServerConfig.engine.name).toRequest(EngineStartCommand.serializer(), json)
    val request = request {
        url(uciServerConfig.url + ENGINE_START_PATH)
                .auth(authResult)
                .post(body)
    }
    return execute(request) {
        val startResult = json.parse(EngineStartCommandResponse.serializer(), it)
        require(startResult.success) {
            val info = if (startResult.info.isNotBlank()) ": ${startResult.info}" else ""
            "Could not start engine$info"
        }
    }
}

private inline fun <reified T> OkHttpClient.execute(request: Request, onResponse: (String) -> T) =
        newCall(request).execute().use {
            onResponse(requireNotNull(it.body).string())
        }

private fun request(builder: Request.Builder.() -> Request.Builder) = Request.Builder().builder().build()

private fun Request.Builder.auth(authResult: AuthorizationResponse) = header("Authorization", "Bearer ${authResult.token}")

private inline fun <reified T> T.toRequest(serializer: SerializationStrategy<T>, json: Json) =
        json.stringify(serializer, this).toRequestBody(JSON_MEDIA_TYPE)

private fun OkHttpClient.manageWebSocket(uciServerConfig: UciServerConfig, authResult: AuthorizationResponse) {
    val request = request {
        url(uciServerConfig.url + WEBSOCKET_PATH).auth(authResult)
    }
    val listener = UciWebSocketListener(uciServerConfig.engine)
    newWebSocket(request, listener)
    dispatcher.executorService.shutdown()
}

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
