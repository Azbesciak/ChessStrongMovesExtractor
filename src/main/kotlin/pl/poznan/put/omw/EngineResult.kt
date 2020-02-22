package pl.poznan.put.omw

import java.lang.RuntimeException

/**
 * fen - fen sent to a server
 * movePlayedInGame - move that was played by the player based on the fen
 * result - message received from the server
 * moveID - moveID assigned based on the number of requests
 * isBestMove - should be assigned when recognized request as a bestmove
 */
class EngineResult(val fen: String, private val movePlayedInGame: String, val result: String, val moveID: Int, val isWhitePlayerPlaying: Boolean, val isBestMove: Boolean = false)
{
    var depth: Int = -1
    val moves: List<String>
    var centipaws: Int = Int.MIN_VALUE // probably any move should get MIN_VALUE
    var secondBestIndex: Int = -1

    init {
        result.split(' ').let {
            if (isBestMove) {

                depth = -1
                centipaws = -1
                moves = it.subList(1, 2)
            } else {
                fun getIntFromArr(arr: List<String>, key: String): Int
                {
                    val keyIndex = it.indexOf(key)
                    if(keyIndex < 0)
                        return 0 // TODO - some messages don't contain cp
                    return it[keyIndex+1].toInt()
                }

                val movesPosition = it.indexOf("pv")
                if(movesPosition < 0)
                    throw RuntimeException("Wrong response from the server")
                moves = it.subList(movesPosition+1, it.size)

                centipaws = getIntFromArr(it, "cp")
                depth = getIntFromArr(it, "depth")
            }
        }
    }

    /**
     * Returns the first move in LAN format (returned by UCI) based on the specified fen.
     */
    fun getMove() = moves[0]

    /**
     * Returns the first move in SAN format based on the specified fen.
     */
    fun getSANMove() = ChessLibUtils.getMoveToSAN(fen, getMove())

    fun wasPlayedInGame() = getMove() == movePlayedInGame

    companion object {
        fun getReponseType(response: String) : ResultType {
            if(response.startsWith("info"))
            {
               if(response.contains("currmove"))
               {
                   return ResultType.Garbage
               }
               return ResultType.Move
            } else if(response.startsWith("bestmove"))
            {
               return ResultType.BestMove
            }
            return ResultType.Unknown
        }
        fun getResponseDepth(response: String) : Int {
            if (getReponseType(response) != ResultType.Move)
                return -1
            response.split(' ').let {
                if(it.size >= 3)
                    return it[2].toInt()
                return -1
            }
        }
    }
}

enum class ResultType {
    Unknown,
    Garbage,
    Move,
    BestMove
}