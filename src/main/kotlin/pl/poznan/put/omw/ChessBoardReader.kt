package pl.poznan.put.omw

import chess.parser.pgn.PGNGame
import chess.parser.pgn.PGNReader
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object ChessBoardReader {
    /**
     * Reads chess games from given path.
     * File is expected to be in PGN format.
     */
    fun getGames(path: String): List<PGNGame> =
            BufferedReader(FileReader(File(path))).use {
                PGNReader().read(it)
            }
}
