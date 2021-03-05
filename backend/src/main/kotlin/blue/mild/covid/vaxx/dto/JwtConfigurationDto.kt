package blue.mild.covid.vaxx.dto

data class JwtConfigurationDto(
    val realm: String,
    val issuer: String,
    val audience: String,
    val registeredUserJwtExpirationInMinutes: Int,
    val patientUserJwtExpirationInMinutes: Int,
    val signingSecret: String
)
