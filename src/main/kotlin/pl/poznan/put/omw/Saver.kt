package pl.poznan.put.omw

import java.io.File
import java.lang.StringBuilder

object Saver {
    fun save(filename: String, header: String, outputPosition: List<OutputPosition>) {
        val file = File(filename)
        val builder = StringBuilder()
        builder.append(header)
        builder.append('\n')


        outputPosition.forEach {
            builder.append("[FEN \"${it.FEN}\"]")
            builder.append("\n")

            var i = it.gameVariations.first().index
            it.gameVariations.forEach { gameVariation ->
                if(i != gameVariation.index)
                    builder.append('\n')
                i = gameVariation.index
                builder.append("$gameVariation ")
            }
            builder.append("\n\n")
        }

        file.writeText(builder.toString())
    }
}