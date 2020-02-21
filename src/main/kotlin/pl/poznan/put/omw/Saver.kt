package pl.poznan.put.omw

import java.io.File
import java.lang.StringBuilder

object Saver {
    fun save(filename: String, header: String, fen: String, interestingMoves: ArrayList<GameVariation>) {
        val file = File(filename)
        val builder = StringBuilder()
        builder.append(header)
        builder.append(fen)
        builder.append("\n")
        var i = interestingMoves.first().index
        interestingMoves.forEach {
            if(i != it.index)
                builder.append('\n')
            i = it.index
            builder.append("$it ")
        }
        file.writeText(builder.toString())
    }
}