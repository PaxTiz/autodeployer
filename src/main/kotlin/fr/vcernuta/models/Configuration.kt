package fr.vcernuta.models

import io.ktor.util.logging.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.system.exitProcess

private val LOGGER = KtorSimpleLogger("Configuration")

@Serializable
data class Configuration(
	val projects: List<Project>,
	val slack: SlackConfiguration,
) {
	companion object {
		fun readFile(devMode: Boolean): Configuration {
			val parser = Json { ignoreUnknownKeys = true }
			
			val path = if (devMode) {
				LOGGER.info("Application started in development mode")
				Path("config.json")
			} else {
				Path("/", "etc", "autodeployer.json")
			}
			
			val file = path.toFile()
			if (!file.exists()) {
				LOGGER.error("Config file at \"$path\" not found")
				exitProcess(1)
			}
			
			LOGGER.info("Loaded configuration from \"$path\"")
			val configurationAsString = file.readText()
			return parser.decodeFromString<Configuration>(configurationAsString)
		}
	}
	
	fun findByHostUrl(url: String) = projects.find { it.repo == url }
	
}
