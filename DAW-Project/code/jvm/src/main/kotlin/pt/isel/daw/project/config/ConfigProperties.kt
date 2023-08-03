package pt.isel.daw.project.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("productiondatabase")
data class ProductionDatabase(val connectionString: String)

/*@ConstructorBinding
@ConfigurationProperties("testdatabase")
data class TestDatabase(val connectionString: String)*/
