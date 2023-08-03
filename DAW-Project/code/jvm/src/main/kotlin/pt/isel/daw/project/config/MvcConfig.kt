package pt.isel.daw.project.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.isel.daw.project.pipeline.DawJsonMessageConverter
import pt.isel.daw.project.pipeline.ModifiedJacksonMessageConverter
import pt.isel.daw.project.pipeline.argumentresolvers.PaginationArgumentResolver
import pt.isel.daw.project.pipeline.interceptors.AuthorizationInterceptor
import pt.isel.daw.project.pipeline.argumentresolvers.UserArgumentResolver

@Component
class MvcConfig(private val interceptor: AuthorizationInterceptor) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(interceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.apply {
            add(UserArgumentResolver())
            add(PaginationArgumentResolver())
        }
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.removeIf{ it is MappingJackson2HttpMessageConverter }

        converters.add(DawJsonMessageConverter().apply {
            objectMapper.apply {
                propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
        })

        converters.add(ModifiedJacksonMessageConverter().apply {
            objectMapper.apply {
                propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
        })
    }
}