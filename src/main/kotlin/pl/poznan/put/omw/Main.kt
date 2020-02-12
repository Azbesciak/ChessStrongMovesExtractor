package pl.poznan.put.omw

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

const val AUTH_PATH = "/user/login"
const val ENGINE_START_PATH = "/engine/start"
const val WEBSOCKET_PATH = "/ws_engine"

fun main(args: Array<String>) = ProgramExecutor {
    val game = ChessBoardReader.getGames(inputPath)
    val mainPathMovesGame = VariantMovesFilter.filter(game)
    val json = Json(JsonConfiguration.Stable)
    val uciServerConfig = readServerConfig(json)
    println(uciServerConfig)
    println(mainPathMovesGame)
    runBlocking {
        HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
        }.use {
            it.connectWithServer(uciServerConfig)
        }
    }
}.main(args)

private fun Params.readServerConfig(json: Json) =
        ServerConfigReader(json) read uciServerConfigPath

private suspend fun HttpClient.connectWithServer(uciServerConfig: UciServerConfig) {
    val credentials = Credentials(uciServerConfig.login, uciServerConfig.password)
    val authResult: AuthorizationResponse = post {
        request(uciServerConfig, AUTH_PATH)
        body = credentials
    }
    require(authResult.success) { "Authorization failed for $credentials" }
    val startResult: EngineStartCommandResponse = post {
        request(uciServerConfig, ENGINE_START_PATH, authResult)
        body = EngineStartCommand(uciServerConfig.engine.name)
    }
    require(startResult.success) {
        val info = if (startResult.info.isNotBlank()) ": ${startResult.info}" else ""
        "Could not start engine$info"
    }
}

private fun HttpRequestBuilder.request(
        uciServerConfig: UciServerConfig,
        path: String,
        auth: AuthorizationResponse? = null
) {
    url(uciServerConfig.url + path)
    contentType(ContentType.Application.Json)
    auth?.let {
        headers {
            set("Authorization", "Bearer ${auth.token}")
        }
    }
}
