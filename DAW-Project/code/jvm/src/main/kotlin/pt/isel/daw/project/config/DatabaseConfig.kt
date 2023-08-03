package pt.isel.daw.project.config

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
class DatabaseConfig(private val db: ProductionDatabase) {

    @Bean
    fun jdbi(): Jdbi = Jdbi.create(db.connectionString).apply {
        installPlugin(KotlinSqlObjectPlugin())
        installPlugin(PostgresPlugin())
        installPlugin(KotlinPlugin())
    }
}