package pt.isel.daw.project.exception

import org.springframework.http.HttpStatus
import java.net.URI

open class BaseException(
    override val message: String,
    open val detail: String? = null,
    open val data: Any? = null,
): Exception(message)

abstract class StandardException(
    override val message: String,
    override val detail: String? = null,
    override val data: Any? = null,
): BaseException(message) {
    abstract val type: URI
    abstract val status: HttpStatus
}

data class UnauthorizedException(
    override val message: String,
    override val detail: String? = null,
    override val data: Any? = null,
): BaseException(message, detail, data)

data class InvalidParameterException(
    override val message: String,
    val invalidParameters: List<InvalidParameter>? = null,
    override val detail: String? = null,
    override val data: Any? = null,
): BaseException(message, detail, data)

data class NotFoundException(
    override val message: String,
    override val detail: String? = null,
    override val data: Any? = null,
): StandardException(message, detail, data) {
    override val type: URI = Error.NotFound.TYPE
    override val status: HttpStatus = Error.NotFound.STATUS
}

data class InternalServerException(
    override val message: String,
    override val detail: String? = null,
    override val data: Any? = null,
): StandardException(message, detail, data) {
    override val type: URI = Error.InternalServerError.TYPE
    override val status: HttpStatus = Error.InternalServerError.STATUS
}

data class ArchivedIssueException(
    override val message: String,
    override val detail: String? = null,
    override val data: Any? = null,
): StandardException(message, detail, data) {
    override val type: URI = Error.ArchivedIssue.TYPE
    override val status: HttpStatus = Error.ArchivedIssue.STATUS
}

data class InvalidParameter(
    var name: String,
    var local: String,
    var reason: String
)

