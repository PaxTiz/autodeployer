package fr.vcernuta.models

import kotlinx.serialization.Serializable

@Serializable
data class Project(
	val name: String,
	val repo: String,
	val path: String,
	val url: String,
	val scripts: List<List<String>>,
	val manualFiles: List<String> = emptyList()
)
