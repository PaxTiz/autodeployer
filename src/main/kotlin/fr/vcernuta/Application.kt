package fr.vcernuta

import fr.vcernuta.routes.webhookRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json


fun Application.configureSerialization() {
	install(ContentNegotiation) {
		json(json = Json { ignoreUnknownKeys = true })
	}
}

fun Application.configureRouting() = routing {
	webhookRoutes()
}
