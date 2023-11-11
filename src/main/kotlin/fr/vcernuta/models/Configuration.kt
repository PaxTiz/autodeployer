package fr.vcernuta.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Configuration(val projects: List<Project>) {
	
	companion object {
		fun readFile(): Configuration {
			val parser = Json { ignoreUnknownKeys = true }
			
			val file = File("./config.json")
			val configurationAsString = file.readText()
			
			return parser.decodeFromString<Configuration>(configurationAsString)
		}
	}
	
	fun findByHostUrl(url: String) = projects.find { it.repo == url }
	
}
