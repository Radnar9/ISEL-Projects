package pt.isel.daw.project.pipeline.argumentresolvers

import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import pt.isel.daw.project.exception.Error.BadRequest.Locations.QUERY_STRING
import pt.isel.daw.project.exception.Error.BadRequest.Message.Pagination.PAGE_TYPE_MISMATCH
import pt.isel.daw.project.exception.Error.BadRequest.Message.TYPE_MISMATCH_REQ_QUERY
import pt.isel.daw.project.exception.InvalidParameter
import pt.isel.daw.project.exception.InvalidParameterException
import pt.isel.daw.project.model.PaginationDto
import pt.isel.daw.project.model.PaginationDto.Companion.DEFAULT_LIMIT
import pt.isel.daw.project.model.PaginationDto.Companion.DEFAULT_PAGE

class PaginationArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        parameter.parameterType == PaginationDto::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): PaginationDto {
        val page: Int
        try {
            page = webRequest.getParameter("page") ?.toInt() ?: DEFAULT_PAGE
        } catch (e: NumberFormatException) {
            throw InvalidParameterException(
                TYPE_MISMATCH_REQ_QUERY,
                listOf(InvalidParameter("page", QUERY_STRING, PAGE_TYPE_MISMATCH))
            )
        }
        val limit = DEFAULT_LIMIT

        if (page < 0) throw InvalidParameterException(
            TYPE_MISMATCH_REQ_QUERY,
            listOf(InvalidParameter("page", QUERY_STRING, PAGE_TYPE_MISMATCH))
        )
        return PaginationDto(page, limit)
    }
}