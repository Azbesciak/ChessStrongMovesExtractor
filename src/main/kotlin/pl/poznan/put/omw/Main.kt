package pl.poznan.put.omw

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import mu.KLogging
import okhttp3.OkHttpClient
import pl.poznan.put.omw.filters.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess


fun main(args: Array<String>) = ProgramExecutor {
    val logger = KLogging()
    val game = ChessBoardReader.getGames(inputPath)
    val mainPathMovesGame = VariantMovesFilter.filter(game)
    val client = OkHttpClient()
    val json = Json(JsonConfiguration.Stable)
    val uciServerConfig = readServerConfig(json)
    val filters = arrayListOf<MoveFilter>(
            NotMinorCaptureMoveFilter(),
            NotRecaptureMoveFilter(),
            NotCaptureWithBestValueMoveFilter(),
            NotSimpleDefendFilter()
    )
    println(uciServerConfig)
    println(mainPathMovesGame)
    UciServerConnector(client, json, uciServerConfig, this).run {
        val closeConnection = connect { newGame ->
            GlobalScope.launch {
                mainPathMovesGame.forEachIndexed { i, game ->
                    logger.logger.debug("STARTING NEW GAME ($i)")
                    val gameConnection = newGame()
                    val player = GamePlayer(game, gameConnection)
                    val result = player.play(engineDepth)
                    gameConnection.close()
                    val resultsFilter = GameFilter(result, filters)
                    val interestingMoves = resultsFilter.filterInterestingMoves(engineDepth, centipawns)

                    val finalGameVariationList = GameVariation.createGameVariationList(interestingMoves)
                    val outputPosition =
                            OutputPosition.createOutputPositions(finalGameVariationList)
                                    .sortedBy { it.gameVariations.first().index }
                    val header = ProgramHelpers.formatHeader(headerTypes, game)
                    Saver.save(outputPath, header, outputPosition)
                    logger.logger.debug("GAME $i CLOSING!")
                }
                logger.logger.info("Processing finished")
                exitProcess(0)
            }
        }
        registerCloseTask { closeConnection() }
    }
}.main(args)

private fun Params.readServerConfig(json: Json) =
        ServerConfigReader(json) read uciServerConfigPath

fun registerCloseTask(task: () -> Unit) {
    Runtime.getRuntime().addShutdownHook(thread(start = false, block = task))
}
