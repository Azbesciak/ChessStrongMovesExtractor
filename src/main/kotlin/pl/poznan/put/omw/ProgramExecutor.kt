package pl.poznan.put.omw


import chess.parser.pgn.Meta
import chess.parser.pgn.PGNGame
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.transformAll
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import java.lang.StringBuilder

object ProgramDefaults {
    val headerTypes = HeaderType.MINIMAL
    const val centipawns = 50
    const val engineDepth = 30
    const val variationsNumber = 2
    const val uciServerConfigPath = "uciServer.json"
}

object ProgramHelpers {
    fun formatHeader(type: HeaderType, game: PGNGame): String {
        val headers = StringBuilder()

        val formatHeaderEntry: (Meta) -> String = { item: Meta -> "[${item.key}: ${item.value}]\n" }

        if (type == HeaderType.CONCISE) {
            val conciseHeaders = arrayListOf("White", "Black", "Site", "Date")
            game.meta.forEach {
                if (it.key in conciseHeaders) {
                    headers.append(formatHeaderEntry(it))
                }
            }
        }

        if (type == HeaderType.ALL) {
            game.meta.forEach {
                headers.append(formatHeaderEntry(it))
            }
        }

        return headers.toString()
    }

}

/**
 * Interface for input arguments.
 * All arguments are specified as fields.
 * You can also see whole help by calling the program with --help flag.
 */
class ProgramExecutor(private val task: Params.() -> Unit) : CliktCommand(name = "strong-moves-extract") {
    private val headerTypes by option(
            help = HeaderType.headersHelp,
            names = *arrayOf("-h", "--headers")
    ).enum<HeaderType> { it.name.toLowerCase() }.default(ProgramDefaults.headerTypes)

    private val centipawns by option(
            help = "Min. required cp (centipawns) difference between best and second best move shown by the engine; default: ${ProgramDefaults.centipawns}",
            names = *arrayOf("-cp", "--centipawns")
    ).int().default(ProgramDefaults.centipawns)
            .validate { it > 0 }

    private val engineDepth by option(
            help = "Min engine search depth for best and second best move shown by the engine (in multivariation mode); default: ${ProgramDefaults.engineDepth}",
            names = *arrayOf("-d", "--engine-depth")
    ).int().default(ProgramDefaults.engineDepth).validate { it > 0 }

    private val variationsNumber by option(
            help = "number of variations in multi-variation mode; default: 2 (minimal acceptable value)",
            names = *arrayOf("-n", "--variations-num")
    ).int().transformAll { values ->
        if (values.isEmpty()) {
            OptionalWithDefault(ProgramDefaults.variationsNumber, false)
        } else {
            require(values.size == 1) { "only one value acceptable for variations" }
            require(values.first() >= ProgramDefaults.variationsNumber) { "min allowed value for multi-variations: ${ProgramDefaults.variationsNumber}" }
            OptionalWithDefault(values.first(), true)
        }
    }

    private val uciServerConfigPath by option(
            help = "path to UCI Server configuration file (relative or absolute); default: ${ProgramDefaults.uciServerConfigPath}",
            names = *arrayOf("-e", "--uci-server-config-path")
    ).default(ProgramDefaults.uciServerConfigPath)

    private val inputPath by argument(
            help = "Path to input PGN file; may be relative or absolute",
            name = "input-path"
    )
    private val outputPath by argument(
            help = "Path to output PGN file; may be relative or absolute",
            name = "output-path"
    )


    override fun run() {
        Params(
                headerTypes = headerTypes,
                centipawns = centipawns,
                engineDepth = engineDepth,
                variationsNumber = variationsNumber,
                uciServerConfigPath = uciServerConfigPath,
                inputPath = inputPath,
                outputPath = outputPath
        ).task()
    }
}

data class OptionalWithDefault(
        val value: Int,
        val wasSet: Boolean
)

enum class HeaderType {
    MINIMAL, CONCISE, ALL;

    companion object {
        val headersHelp = """
            |What headers should be put to output PGN file; default: ${ProgramDefaults.headerTypes.name.toLowerCase()};${'\n'}
            |meaning:${'\n'}
                |data: ${'\n'}
                    [Event "12th World Teams 2019"]${'\n'}
                    [Site "Astana KAZ"]${'\n'}
                    [Date "2019.03.12"]${'\n'}
                    [Round "7.1"]${'\n'}
                    [White "Swiercz,D"]${'\n'}
                    [Black "Nepomniachtchi,I"]${'\n'}
                    [Result "1/2-1/2"]${'\n'}
                    [WhiteTitle "GM"]${'\n'}
                    [BlackTitle "GM"]${'\n'}
                    [WhiteElo "2655"]${'\n'}
                    [BlackElo "2771"]${'\n'}
                    [ECO "D90"]${'\n'}
                    [Opening "Gruenfeld"]${'\n'}
                    [EventDate "2019.03.05"]${'\n'}

                |all:${'\n'}
                    |all headers from the 'data' + ${'\n'}
                    |[FEN <extracted position in FEN format>]${'\n'}
    
                |concise:${'\n'}
                    |[White "Swiercz,D"]${'\n'}
                    |[Black "Nepomniachtchi,I"]${'\n'}
                    |[Site "Astana KAZ"]${'\n'}
                    |[Date "2019.03.12"]${'\n'}
                    |[FEN <extracted position in FEN format>]${'\n'}
    
                |minimal:${'\n'}
                    |[FEN <extracted position in FEN format>]            
            """.trimMargin("|")

    }
}

data class Params(
        val headerTypes: HeaderType,
        val centipawns: Int,
        val engineDepth: Int,
        val variationsNumber: OptionalWithDefault,
        val uciServerConfigPath: String,
        val inputPath: String,
        val outputPath: String
)
