package pl.poznan.put.omw

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

fun main(args: Array<String>) = ProgramExecutor {
    val game = ChessBoardReader.getGames(inputPath)
    val mainPathMovesGame = VariantMovesFilter.filter(game)
    val json = Json(JsonConfiguration.Stable)
    val uciServerConfigReader = ServerConfigReader(json)
    val uciServerConfig = uciServerConfigReader read uciServerConfigPath
    println(uciServerConfig)
    println(mainPathMovesGame)
}.main(args)
