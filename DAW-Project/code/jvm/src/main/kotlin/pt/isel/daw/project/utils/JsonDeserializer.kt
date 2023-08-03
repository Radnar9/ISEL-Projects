package pt.isel.daw.project.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/*
 * Deserializes a JSON string into a domain object
 */
val mapper = ObjectMapper().registerKotlinModule()
fun <T> String.deserializeJsonTo(clazz: Class<T>): T {
    return mapper.readValue(this, clazz)
}

fun <T> Any.serializeJsonTo(): String {
    return mapper.writeValueAsString(this)
}