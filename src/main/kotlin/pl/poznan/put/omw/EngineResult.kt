package pl.poznan.put.omw

import java.lang.RuntimeException

class EngineResult(val fen: String, val result: String, val isBestMove: Boolean = false)
{
    val depth: Int
    val moves: List<String>
    val centipaws: Int

    init {
        result.split(' ').let {
            if(isBestMove)
            {
                depth = -1
                centipaws = -1
                moves = it.subList(1, 2)
            }
            else
            {
                if(it.size < 19)
                    throw RuntimeException("Wrong response from the server")
                moves = it.subList(19, it.size)
                centipaws = it[9].toInt()
                depth = it[2].toInt()
            }
        }
    }

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