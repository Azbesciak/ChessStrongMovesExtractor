package pl.poznan.put.omw

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class UciServerEngine(
        val name: String,
        val options: Map<String, String>
)

@Serializable
data class UciServerConfig(
        val url: String,
        val login: String,
        val password: String,
        val engine: UciServerEngine
)

/**
 * Reads UCI server configuration. Expected format is JSON like:
 * {
 *   "url": "http://localhost:8080",
 *   "login": "test",
 *   "password": "111111",
 *   "engine": {
 *     "name": "Stockfish 10 x64",
 *     "options": {
 *       "Threads": "4",
 *       "Hash": "1024"
 *     }
 *   }
 * }
 * where options are UCI engine options.
 * As example you can refer to https://github.com/official-stockfish/Stockfish
 */
class ServerConfigReader(private val json: Json) {
    infix fun read(path: String) = File(path).run {
        require(exists()) {
            "uci server configuration file at '${path}' does not exist"
        }
        val deserializer = UciServerConfig.serializer()
        json.parse(deserializer, readText())
    }
}
