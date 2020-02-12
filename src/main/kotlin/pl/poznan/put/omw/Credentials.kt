package pl.poznan.put.omw

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(
        val login: String,
        val password: String
)

@Serializable
data class AuthorizationResponse(
        val success: Boolean,
        val token: String? = null
)

@Serializable
data class EngineStartCommand(
        val engine: String
)

@Serializable
data class EngineStartCommandResponse(
        val success: Boolean,
        val info: String
)
