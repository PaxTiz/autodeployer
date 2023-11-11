package fr.vcernuta.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitlabCommitAuthor(val name: String, val email: String)

@Serializable
data class GitlabCommit(
	val id: String,
	val title: String,
	val author: GitlabCommitAuthor,
	@SerialName("timestamp") val createdAt: Instant,
)

@Serializable
data class GitlabProject(
	@SerialName("web_url") val webUrl: String
)

@Serializable
data class GitlabWebhook(
	@SerialName("object_kind") val objectKind: String,
	@SerialName("event_name") val eventName: String,
	val project: GitlabProject,
	val commits: List<GitlabCommit>,
) {
	
	fun getLatestCommit(): GitlabCommit {
		return commits.maxByOrNull { it.createdAt }!!
	}
	
}
