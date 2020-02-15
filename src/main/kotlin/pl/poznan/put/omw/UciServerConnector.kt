package pl.poznan.put.omw

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import mu.KLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class UciServerConnector(
        private val client: OkHttpClient,
        private val json: Json,
        private val uciServerConfig: UciServerConfig,
        private val programParams: Params
) {
    private companion object : KLogging() {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val AUTH_PATH = "/user/login"
        const val USER_LOGOUT = "/user/logout"
        const val ENGINE_START_PATH = "/engine/start"
        const val ENGINE_STOP_PATH = "/engine/stop"
        const val WEBSOCKET_PATH = "/ws_engine"
    }

    private var engineStarted = false
    private var authResponse: AuthorizationResponse? = null
    private var connected = false
    private var listener: UciWebSocketListener? = null

    fun connect(onUciConnection: (newGame: ()-> GameConnection) -> Unit): () -> Unit {
        require(!connected) { "can connect only once" }
        try {
            makeConnection {
                onUciConnection {
                    requireNotNull(listener) { "connection already closed" }
                    listener!!.newGame()
                }
            }
        } catch (e: Throwable) {
            close()
            throw e
        }
        return {
            listener?.close()
            listener = null
            close()
        }
    }

    private fun makeConnection(onUciConnection: () -> Unit) {
        client.logoutUser(true)
        authResponse = client.authorize()
        client.tryToCloseEngine(true)
        client.startEngine()
        client.manageWebSocket(onUciConnection)
        connected = true
    }

    private fun OkHttpClient.authorize(): AuthorizationResponse {
        val credentials = Credentials(uciServerConfig.login, uciServerConfig.password)
        val body = credentials.toRequest(Credentials.serializer(), json)
        val request = request(AUTH_PATH) {
            post(body)
        }
        return execute(request) {
            val authResult = json.parse(AuthorizationResponse.serializer(), it)
            require(authResult.success) { "Authorization failed for $credentials" }
            authResult
        }
    }

    private fun OkHttpClient.startEngine() {
        val body = EngineStartCommand(uciServerConfig.engine.name).toRequest(EngineStartCommand.serializer(), json)
        val request = request(ENGINE_START_PATH) {
            post(body)
        }
        return execute(request) {
            val startResult = json.parse(EngineManageCommandResponse.serializer(), it)
            require(startResult.success) {
                val info = if (startResult.info.isNotBlank()) ": ${startResult.info}" else ""
                "Could not start engine$info"
            }
            engineStarted = true
        }
    }

    private fun OkHttpClient.manageWebSocket(onUciConnection: () -> Unit) {
        val request = request(WEBSOCKET_PATH)
        listener = UciWebSocketListener(uciServerConfig.engine, programParams, onUciConnection)
        newWebSocket(request, listener!!)
        dispatcher.executorService.shutdown()
    }

    private fun close() {
        if (!engineStarted) return
        client.tryToCloseEngine()
        client.logoutUser()
        logger.info { "uci connector closed" }
    }

    private fun OkHttpClient.tryToCloseEngine(silent: Boolean = false) {
        try {
            val req = request(ENGINE_STOP_PATH) {
                emptyPost()
            }
            execute(req) {
                if (silent) return
                logger.info { "engine close response: $it" }
            }
            engineStarted = false
        } catch (t: Throwable) {
            if (silent) return
            logger.error(t) { "could not gracefully close UciServerConnector" }
        }
    }

    private fun OkHttpClient.logoutUser(silent: Boolean = false) {
        try {
            val req = request(USER_LOGOUT) {
                emptyPost()
            }
            execute(req) {
                if (!silent)
                    logger.info { "user logout result: $req" }
                authResponse = null
            }
        } catch (t: Throwable) {
            if (silent) return
            logger.error(t) { "could not logout user" }
        }
    }

    private fun Request.Builder.emptyPost() = post("{}".toRequestBody(JSON_MEDIA_TYPE))

    private inline fun <reified T> OkHttpClient.execute(request: Request, onResponse: (String) -> T) =
            newCall(request).execute().use {
                if (!it.isSuccessful) {
                    logger.error { "Request ${request.url} failed: ${it.body}" }
                    throw RuntimeException(it.message)
                }
                onResponse(requireNotNull(it.body).string())
            }

    private inline fun request(path: String, builder: Request.Builder.() -> Request.Builder = { this }) =
            Request.Builder()
                    .url(uciServerConfig.url + path)
                    .builder()
                    .run { authResponse?.let { auth(it) } ?: this }
                    .build()

    private fun Request.Builder.auth(authResult: AuthorizationResponse) =
            header("Authorization", "Bearer ${authResult.token}")

    private inline fun <reified T> T.toRequest(serializer: SerializationStrategy<T>, json: Json) =
            json.stringify(serializer, this).toRequestBody(JSON_MEDIA_TYPE)
}

class ConnectionManager(
        val close: () -> Unit,
        val newGame: () -> GameConnection
)
