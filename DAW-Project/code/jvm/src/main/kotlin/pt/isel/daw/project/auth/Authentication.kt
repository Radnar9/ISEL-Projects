package pt.isel.daw.project.auth

import org.springframework.util.Base64Utils
import pt.isel.daw.project.exception.UnauthorizedException
import pt.isel.daw.project.model.user.UserCredentials
import pt.isel.daw.project.exception.Error

/**
 * Object responsible for getting the user credentials depending on the scheme
 */
object Authentication {
    const val USER_ATTRIBUTE = "user"

    object Basic {
        const val SCHEME = "Basic"
        fun getUserCredentials(token: String): UserCredentials {
            val credentialsToken = String(Base64Utils.decodeFromString(token))
            val credentials = credentialsToken.split(":")
            if (credentials.size < 2) {
                throw UnauthorizedException(Error.Unauthorized.Message.INVALID_TOKEN_FORMAT)
            }
            return UserCredentials(credentials[0], credentials[1])
        }
    }
}
