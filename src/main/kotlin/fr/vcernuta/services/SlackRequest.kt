package fr.vcernuta.services

import fr.vcernuta.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

private val httpClient = HttpClient(CIO) {
	install(ContentNegotiation) {
		json()
	}
}

class SlackRequest(
	private val slack: SlackConfiguration,
	private val project: Project,
	private val author: CommitAuthor,
) {
	
	suspend fun send(action: SlackAction) {
		when (action) {
			SlackAction.GitCommandFailed -> gitCommandFailed()
			SlackAction.ManualActionRequired -> manualActionRequired()
			SlackAction.ProjectCommandFailed -> projectCommandFailed()
			SlackAction.BuildSuccessful -> buildSuccessful()
		}
	}
	
	private suspend fun gitCommandFailed() {
		val message = """
			*[${project.name}] 🔴 Deployment failed*
			There was an error while fetching the latest changes from the remote repository.
			
			You can view the commit history on ${project.repo}.
		""".trimIndent()
		
		sendRequest(message)
	}
	
	private suspend fun manualActionRequired() {
		val message = """
			*[${project.name}] 🟠 Deployment has been suspended*
			A manual intervention is required in order to deploy the latest version.
		""".trimIndent()
		
		sendRequest(message)
	}
	
	private suspend fun projectCommandFailed() {
		val message = """
			*[${project.name}] 🔴 Deployment failed*
			There was an error while deploying the latest version.)
		""".trimIndent()
		
		sendRequest(message)
	}
	
	private suspend fun buildSuccessful() {
		val slackUser = findSlackUserByEmail()
		
		val userHandle = if (slackUser == null) {
			author.username
		} else {
			"<@${slackUser.id}>"
		}
		
		val message = """
			*[${project.name}] 🟢 Deployment Successful*
			The latest push on `main` by *${userHandle}* has been deployed successfully.
			
			The new version is available at ${project.url}.
		""".trimIndent()
		
		sendRequest(message)
	}
	
	private suspend fun sendRequest(message: String) {
		val request = getPostMessageRequest(message)
		
		httpClient.post("https://slack.com/api/chat.postMessage") {
			contentType(ContentType.Application.Json)
			setBody(request)
			
			headers { bearerAuth(slack.token) }
		}
	}
	
	private suspend fun findSlackUserByEmail(): SlackUser? {
		val response = httpClient.get("https://slack.com/api/users.lookupByEmail") {
			contentType(ContentType.Application.Json)
			url { parameters.append("email", author.email) }
			
			headers { bearerAuth(slack.token) }
		}
		
		if (response.status != HttpStatusCode.OK) {
			return null
		}
		
		try {
			val responseData = response.body<SlackFindUserByEmailResponse>()
			if (!responseData.ok) {
				return null
			}
			
			return responseData.user
		} catch (e: Exception) {
			return null
		}
	}
	
	private fun getPostMessageRequest(message: String): SlackPostMessageRequest {
		return SlackPostMessageRequest(
			token = slack.token,
			channel = slack.channel,
			username = slack.username,
			text = message
		)
	}
	
}
