package pl.poznan.put.omw

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import mu.KLogging
import okhttp3.OkHttpClient
import pl.poznan.put.omw.filters.MoveFilter
import pl.poznan.put.omw.filters.NotRecaptureMoveFilter
import pl.poznan.put.omw.filters.NotSimpleDefendFilter
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
           // NotMinorCaptureMoveFilter(),
            NotRecaptureMoveFilter(),
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
                    // assign cp to bestmoves
                    var lastBest = 0
                    for (i in result.indices)
                    {
                        if (result[i].isBestMove)
                        {
                            // result with bestmove as the first move should be before bestmove in the result list
                            for (j in i-1 downTo lastBest+1)
                            {
                                if(result[j].getMove() == result[i].getMove())
                                {
                                    // result with the same move as bestmove is found
                                    // assign cp to bestmove
                                    result[i].centipaws = result[j].centipaws
                                    result[i].depth = result[j].depth
                                    break
                                }
                            }
                            lastBest = i
                        }
                    }
                    gameConnection.close()
                    val resultsFilter = GameFilter(result, filters)
                    val interestingMoves = resultsFilter.filterInterestingMoves(engineDepth)

//                    val GameMoveList = PgnToStockfish.getSanList(game)
                    val UclMoveList = PgnToStockfish.getStockfishFormat(game)
                    val groupedGameVariationList = OutputPosition.createGameVariationList(interestingMoves
                            as ArrayList<EngineResult>?, UclMoveList);
                    val finalGameVariationList = OutputPosition.setBestAndFlatten(groupedGameVariationList)
                    val outputPosition = OutputPosition(result.last().fen, finalGameVariationList)
                    val header = ProgramHelpers.formatHeader(headerTypes, game)
                    Saver.save(outputPath, header, result.last().fen, finalGameVariationList)
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
