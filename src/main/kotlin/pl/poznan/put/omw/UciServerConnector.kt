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
        const val ENGINE_START_PATH = "/engine/start"
        const val WEBSOCKET_PATH = "/ws_engine"
    }

    fun connect() {
        val authResult = client.authorize(uciServerConfig, json)
        client.startEngine(uciServerConfig, authResult, json)
        client.manageWebSocket(uciServerConfig, authResult)
    }

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
                if (!it.isSuccessful) {
                    logger.error { "Request ${request.url} failed: ${it.body}" }
                    throw RuntimeException(it.message)
                }
                onResponse(requireNotNull(it.body).string())
            }

    private fun request(builder: Request.Builder.() -> Request.Builder) =
            Request.Builder().builder().build()

    private fun Request.Builder.auth(authResult: AuthorizationResponse) =
            header("Authorization", "Bearer ${authResult.token}")

    private inline fun <reified T> T.toRequest(serializer: SerializationStrategy<T>, json: Json) =
            json.stringify(serializer, this).toRequestBody(JSON_MEDIA_TYPE)

    private fun OkHttpClient.manageWebSocket(uciServerConfig: UciServerConfig, authResult: AuthorizationResponse) {
        val request = request {
            url(uciServerConfig.url + WEBSOCKET_PATH).auth(authResult)
        }
        val listener = UciWebSocketListener(uciServerConfig.engine, programParams)
        newWebSocket(request, listener)
        dispatcher.executorService.shutdown()
    }
}
