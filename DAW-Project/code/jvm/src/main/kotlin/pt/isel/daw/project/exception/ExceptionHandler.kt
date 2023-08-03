package pt.isel.daw.project.exception

import org.jdbi.v3.core.JdbiException
import org.postgresql.util.PSQLException
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import pt.isel.daw.project.model.ProblemJsonModel
import java.net.URI
import javax.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import pt.isel.daw.project.exception.Error.BadRequest.Message.MISSING_MISMATCH_REQ_BODY
import pt.isel.daw.project.exception.Error.BadRequest.Message.MUST_HAVE_TYPE
import pt.isel.daw.project.exception.Error.BadRequest.Message.TYPE_MISMATCH_REQ_PATH
import pt.isel.daw.project.exception.Error.Database.UniqueConstraint.Message.RESOURCE_ALREADY_EXISTS
import pt.isel.daw.project.exception.Error.InternalServerError.Message.UNKNOWN_ERROR
import pt.isel.daw.project.exception.Error.MethodNotAllowed.Message.METHOD_NOT_ALLOWED
import java.sql.SQLException

@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

    fun buildExceptionResponse(
        type: URI,
        title: String,
        instance: String,
        status: HttpStatus,
        detail: String? = null,
        data: Any? = null,
        headers: HttpHeaders = HttpHeaders(),
        invalidParameters: List<InvalidParameter>? = null,
    ): ResponseEntity<Any> {
        return ResponseEntity
            .status(status)
            .contentType(ProblemJsonModel.MEDIA_TYPE)
            .headers(headers)
            .body(ProblemJsonModel(type, title, detail, instance, invalidParameters, data))
    }

    /**
     * Function used on those exceptions that are standard, this is, set of exceptions that return always the same
     * properties, such as [NotFoundException]
     */
    @ExceptionHandler(StandardException::class)
    fun handleStandard(
        ex: StandardException,
        req: HttpServletRequest
    ): ResponseEntity<Any> = buildExceptionResponse(
        ex.type,
        ex.message,
        req.requestURI,
        ex.status,
        ex.detail,
        ex.data
    )

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(
        ex: UnauthorizedException,
        req: HttpServletRequest
    ): ResponseEntity<Any> {
        val headers = HttpHeaders()
        headers.add(Error.Unauthorized.WWW_AUTH_HEADER, Error.Unauthorized.WWW_AUTH_HEADER_VALUE)
        return buildExceptionResponse(
            Error.Unauthorized.TYPE,
            ex.message,
            req.requestURI,
            Error.Unauthorized.STATUS,
            ex.detail,
            ex.data,
            headers
        )
    }

    @ExceptionHandler(InvalidParameterException::class)
    fun handleInvalidParameters(
        ex: InvalidParameterException,
        req: HttpServletRequest
    ): ResponseEntity<Any> = buildExceptionResponse(
        Error.BadRequest.TYPE,
        ex.message,
        req.requestURI,
        Error.BadRequest.STATUS,
        ex.detail,
        ex.data,
        invalidParameters = ex.invalidParameters
    )

    /**
     * Exception thrown on a type mismatch when trying to set a bean property. Like inserting a character in the URI
     * path when it is expected an integer.
     */
    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = buildExceptionResponse(
        Error.BadRequest.TYPE,
        TYPE_MISMATCH_REQ_PATH,
        (request as ServletWebRequest).request.requestURI,
        Error.BadRequest.STATUS,
        invalidParameters = listOf(InvalidParameter(
            (ex as MethodArgumentTypeMismatchException).name,
            Error.BadRequest.Locations.PATH,
            Error.makeMessage(MUST_HAVE_TYPE, ex.requiredType.toString())
        )),
    )

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = buildExceptionResponse(
        Error.MethodNotAllowed.TYPE,
        METHOD_NOT_ALLOWED,
        (request as ServletWebRequest).request.requestURI,
        Error.MethodNotAllowed.STATUS,
    )

    /**
     * Exception thrown when the request body has a missing parameter or a type mismatch.
     */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = buildExceptionResponse(
        Error.BadRequest.TYPE,
        MISSING_MISMATCH_REQ_BODY,
        (request as ServletWebRequest).request.requestURI,
        Error.BadRequest.STATUS,
    )

    @ExceptionHandler(JdbiException::class)
    private fun handleJdbiException(
        ex: JdbiException,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val cause = ex.cause as SQLException
        val psqlError = cause.sqlState

        if (psqlError == Error.Database.UniqueConstraint.SQL_STATE) {
            return buildExceptionResponse(
                Error.Database.UniqueConstraint.TYPE,
                RESOURCE_ALREADY_EXISTS,
                request.requestURI,
                Error.Database.UniqueConstraint.STATUS,
                (cause as PSQLException).serverErrorMessage?.message
            )
        }
        return buildExceptionResponse(
            Error.InternalServerError.TYPE,
            UNKNOWN_ERROR,
            request.requestURI,
            Error.InternalServerError.STATUS,
        )
    }
}