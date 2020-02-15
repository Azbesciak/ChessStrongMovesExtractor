package pl.poznan.put.omw

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.OkHttpClient


fun main(args: Array<String>) = ProgramExecutor {
    val game = ChessBoardReader.getGames(inputPath)
    val mainPathMovesGame = VariantMovesFilter.filter(game)
    val client = OkHttpClient()
    val json = Json(JsonConfiguration.Stable)
    val uciServerConfig = readServerConfig(json)
    println(uciServerConfig)
    println(mainPathMovesGame)
    val connector = UciServerConnector(client, json, uciServerConfig)
    connector.connect()
}.main(args)

private fun Params.readServerConfig(json: Json) =
        ServerConfigReader(json) read uciServerConfigPath
