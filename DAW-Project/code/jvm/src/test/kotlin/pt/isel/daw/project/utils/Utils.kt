package pt.isel.daw.project.utils

import java.io.File

object Utils {
    const val DOMAIN = "http://localhost:"

    object LoadScript {
        private val classLoader = javaClass.classLoader

        fun getResourceFile(resourceName: String) =
            File(classLoader.getResource(resourceName)!!.toURI()).readText()

    }
}