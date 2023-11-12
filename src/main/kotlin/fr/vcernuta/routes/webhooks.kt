package fr.vcernuta.routes

import fr.vcernuta.AutoDeployerConfigurationKey
import fr.vcernuta.models.GitlabWebhook
import fr.vcernuta.services.ProjectBuilder
import fr.vcernuta.services.SlackRequest
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
		
		val config = application.attributes[AutoDeployerConfigurationKey]
		val project = config.findByHostUrl(webhook.project.webUrl)
		
		if (project == null) {
			call.respond(HttpStatusCode.NotFound)
			return@post
		}
		
		if (webhook.eventName != "push") {
			val slackRequest = SlackRequest(config.slack)
			slackRequest.unknownWebhookEvent(project, webhook.eventName)
			
			call.respond(HttpStatusCode.OK)
			return@post
		}
		
		launch(Dispatchers.IO) {
			ProjectBuilder(project, webhook).build(config)
		}
		
		call.respond(HttpStatusCode.OK)
	}
}
