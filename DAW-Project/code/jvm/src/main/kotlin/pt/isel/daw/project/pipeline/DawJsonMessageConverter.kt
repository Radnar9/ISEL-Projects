package pt.isel.daw.project.pipeline

import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import pt.isel.daw.project.model.DawJsonModel

class DawJsonMessageConverter : MappingJackson2HttpMessageConverter() {

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?) =
        (mediaType == null || mediaType == DawJsonModel.MEDIA_TYPE) && DawJsonModel::class.java.isAssignableFrom(clazz)

    override fun canWrite(mediaType: MediaType?) = mediaType == null || mediaType == DawJsonModel.MEDIA_TYPE

    override fun getSupportedMediaTypes() = listOf(
        DawJsonModel.MEDIA_TYPE,
    )
}