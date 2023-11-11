package fr.vcernuta.routes

import fr.vcernuta.AutoDeployerConfigurationKey
import fr.vcernuta.models.GitlabWebhook
import fr.vcernuta.services.ProjectBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Route.webhookRoutes() = route("/webhook") {
	post("/gitlab") {
		val webhook = call.receive<GitlabWebhook>()
		
		if (webhook.eventName != "push") {
			call.respond(HttpStatusCode.OK)
			return@post
		}
		
		val config = application.attributes[AutoDeployerConfigurationKey]
		val project = config.findByHostUrl(webhook.project.webUrl)
		
		if (project == null) {
			call.respond(HttpStatusCode.NotFound)
			return@post
		}
		
		launch(Dispatchers.IO) {
			ProjectBuilder(project, webhook).build(config)
		}
		
		call.respond(HttpStatusCode.OK)
	}
}
