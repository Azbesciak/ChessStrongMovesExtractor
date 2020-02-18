package pl.poznan.put.omw

import java.lang.RuntimeException

class EngineResult(val fen: String, val result: String)
{
    val depth: Int
    val moves: List<String>
    val centipaws: Int

    init {
        result.split(' ').let {
            if(it.size < 19)
                throw RuntimeException("Wrong response from the server")
            depth = it[2].toInt()
            centipaws = it[9].toInt()
            moves = it.subList(19, it.size)
        }
    }

}