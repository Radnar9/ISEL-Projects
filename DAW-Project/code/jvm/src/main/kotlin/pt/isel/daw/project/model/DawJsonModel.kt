package pt.isel.daw.project.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

class DawJsonModel(
    @JsonProperty("class")
    val clazz: List<String>,
    val rel: List<String>? = null,
    val properties: Any? = null,
    val entities: List<DawJsonModel>? = null,
    val actions: List<Action>? = null,
    val links: List<Link>,
) {
    class Property(
        val name: String,
        val type: String,
        val itemsType: String? = null,
        val required: Boolean? = null,
    )
    class Action(
        val name: String,
        val title: String,
        val method: HttpMethod,
        val href: String,
        val type: String? = null,
        val properties: List<Property>? = null,
    )
    class Link(
        val rel: List<String>,
        val href: String,
        val templated: Boolean? = null,
    )
    companion object {
        val MEDIA_TYPE = MediaType("application", "vnd.daw+json")
    }
}