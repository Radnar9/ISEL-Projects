package pt.isel.daw.project.pipeline.interceptors

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.daw.project.auth.Authentication
import pt.isel.daw.project.model.user.UserDto
import pt.isel.daw.project.service.UserService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import pt.isel.daw.project.exception.UnauthorizedException
import pt.isel.daw.project.model.Uris
import pt.isel.daw.project.model.Uris.Users.LOGIN_PATH
import pt.isel.daw.project.model.Uris.Users.LOGOUT_PATH
import pt.isel.daw.project.model.user.UserCredentials
import pt.isel.daw.project.model.user.UserSession
import pt.isel.daw.project.utils.Validator
import pt.isel.daw.project.utils.deserializeJsonTo
import java.util.*
import java.util.stream.Collectors
import javax.servlet.http.Cookie


@Component
class AuthorizationInterceptor(val userService: UserService) : HandlerInterceptor {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AuthorizationInterceptor::class.java)
    }

    /**
     *  Verifies if the user credentials are valid, if so returns an UserDto, otherwise
     *  @throws UnauthorizedException
     */
    fun validateCredentials(userCredentials: UserCredentials): UserSession {
        return userService.validateUserCredentials(userCredentials)
    }

    fun generateCookie(token: UUID): ResponseCookie {
        return ResponseCookie
            .from("SESSION_KEY", token.toString())
            .sameSite("Strict")
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .httpOnly(true)
            .secure(false)
            .build()
    }

    fun cleanCookie(): ResponseCookie {
        return ResponseCookie
            .from("SESSION_KEY", null.toString())
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .httpOnly(true)
            .secure(false)
            .build()
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        if (!request.requestURI.equals(LOGIN_PATH) && !request.requestURI.equals(LOGOUT_PATH)) {
            return true
        }
        val bodyValue = request.reader.lines().collect(Collectors.joining())
        val requestUri = request.requestURI
        if (requestUri.equals(LOGIN_PATH) && bodyValue.isNotEmpty()) {
            val userCredentials = bodyValue.deserializeJsonTo(UserCredentials::class.java)
            val userSession = validateCredentials(userCredentials)

            request.setAttribute(Authentication.USER_ATTRIBUTE, userSession)
            response.addHeader(HttpHeaders.SET_COOKIE, generateCookie(userSession.token).toString())
        }
        if (requestUri.equals(LOGOUT_PATH)) {
            response.addHeader(HttpHeaders.SET_COOKIE, cleanCookie().toString())
        }
        return true
    }
}