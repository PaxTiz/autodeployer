package fr.vcernuta.models

import kotlinx.serialization.Serializable

enum class SlackAction {
	GitCommandFailed,
	ManualActionRequired,
	ProjectCommandFailed,
	BuildSuccessful,
}

@Serializable
data class SlackConfiguration(
	val token: String,
	val channel: String,
	val username: String,
)

@Serializable
data class SlackPostMessageRequest(
	val token: String,
	val channel: String,
	val text: String,
	val username: String,
)

@Serializable
data class SlackFindUserByEmailResponse(
	val ok: Boolean,
	val user: SlackUser?,
	val error: String?
)

@Serializable
data class SlackUser(
	val id: String,
	val name: String
)
