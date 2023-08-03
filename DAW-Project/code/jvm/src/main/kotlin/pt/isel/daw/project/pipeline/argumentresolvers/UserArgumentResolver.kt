package pt.isel.daw.project.pipeline.argumentresolvers

import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import pt.isel.daw.project.auth.Authentication
import pt.isel.daw.project.model.user.UserDto
import pt.isel.daw.project.model.user.UserSession

class UserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == UserSession::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UserSession =
        webRequest.getAttribute(Authentication.USER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST) as UserSession
}