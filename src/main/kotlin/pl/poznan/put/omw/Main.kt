package pl.poznan.put.omw

fun main(args: Array<String>) = ProgramExecutor {
    val game = ChessBoardReader.getGames(inputPath)
    val mainPathMovesGame = VariantMovesFilter.filter(game)
    println(mainPathMovesGame)
}.main(args)
