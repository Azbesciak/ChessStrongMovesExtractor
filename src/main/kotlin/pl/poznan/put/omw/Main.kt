package pl.poznan.put.omw

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.OkHttpClient
import kotlin.concurrent.thread


fun main(args: Array<String>) = ProgramExecutor {
    val game = ChessBoardReader.getGames(inputPath)
    val mainPathMovesGame = VariantMovesFilter.filter(game)
    val client = OkHttpClient()
    val json = Json(JsonConfiguration.Stable)
    val uciServerConfig = readServerConfig(json)
    println(uciServerConfig)
    println(mainPathMovesGame)
    UciServerConnector(client, json, uciServerConfig, this).run {
        val connectionManager = connect()
        registerCloseTask { connectionManager.close() }
        mainPathMovesGame.forEach {
            val gameConnection = connectionManager.newGame()
            val player = GamePlayer(it, gameConnection)
            player.play()
            gameConnection.close()
        }
    }
}.main(args)

private fun Params.readServerConfig(json: Json) =
        ServerConfigReader(json) read uciServerConfigPath

fun registerCloseTask(task: () -> Unit) {
    Runtime.getRuntime().addShutdownHook(thread(start = false, block = task))
}
