package pt.isel.daw.project.pipeline

import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import pt.isel.daw.project.model.DawJsonModel
import pt.isel.daw.project.model.ProblemJsonModel
import java.lang.reflect.Type

class ModifiedJacksonMessageConverter : MappingJackson2HttpMessageConverter() {

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?) =
        !DawJsonModel::class.java.isAssignableFrom(clazz) && super.canWrite(clazz, mediaType)

    override fun canWrite(type: Type?, clazz: Class<*>, mediaType: MediaType?) =
        !DawJsonModel::class.java.isAssignableFrom(clazz) && super.canWrite(type, clazz, mediaType)

    override fun getSupportedMediaTypes() = listOf(
        MediaType("application","json"),
        ProblemJsonModel.MEDIA_TYPE,
    )
}