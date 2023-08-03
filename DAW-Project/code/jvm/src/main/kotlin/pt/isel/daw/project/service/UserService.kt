package pt.isel.daw.project.service

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pt.isel.daw.project.dao.UserDao
import pt.isel.daw.project.exception.UnauthorizedException
import pt.isel.daw.project.model.user.UserCredentials
import pt.isel.daw.project.model.user.UserDto
import pt.isel.daw.project.utils.deserializeJsonTo
import pt.isel.daw.project.exception.Error
import pt.isel.daw.project.model.user.UserSession
import java.util.*

@Service
class UserService(jdbi: Jdbi) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    }

    private val userDao = jdbi.onDemand<UserDao>()

    fun validateUserCredentials(userCredentials: UserCredentials): UserSession {
        val user = userDao.validateUserCredentials(userCredentials).getString("userRep")
        if (user == null) {
            logger.info("User credentials are not valid")
            throw UnauthorizedException(Error.Unauthorized.Message.INVALID_CREDENTIALS)
        }
        return user.deserializeJsonTo(UserSession::class.java)
    }

    fun getUserInfo(userToken: UUID): UserDto {
        return userDao.getUserInfo(userToken).deserializeJsonTo(UserDto::class.java)
    }
}