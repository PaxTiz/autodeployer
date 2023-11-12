package fr.vcernuta.services

import fr.vcernuta.models.*
import java.io.File
import kotlin.io.path.Path

typealias ManualFiles = Map<String, Long>

class ProjectBuilder(private val project: Project, private val webhook: GitlabWebhook) {
	
	suspend fun build(config: Configuration) {
		val manualFiles = getManualFiles()

		if (!getLatestChanges()) {
			println("ERROR: Failed to get latest changes")
			notifyOnSlack(config.slack, SlackAction.GitCommandFailed)
			return
		}

		if (isManualChangeRequired(manualFiles)) {
			println("ERROR: A manual change is required in order to update the project")
			notifyOnSlack(config.slack, SlackAction.ManualActionRequired)
			return
		}

		if (!runProjectCommands()) {
			println("ERROR: There was an error while running deployment commands")
			notifyOnSlack(config.slack, SlackAction.ProjectCommandFailed)
			return
		}
		
		notifyOnSlack(config.slack, SlackAction.BuildSuccessful)
	}
	
	private fun getLatestChanges(): Boolean {
		return execute("git", "pull")
	}
	
	private fun isManualChangeRequired(manualFiles: ManualFiles): Boolean {
		val newManualFiles = getManualFiles()
		
		for ((path, modifiedAt) in newManualFiles) {
			if (modifiedAt > manualFiles[path]!!) {
				return true
			}
		}
		
		return false
	}
	
	private fun runProjectCommands(): Boolean {
		for (command in project.scripts) {
			if (!execute(*command.toTypedArray())) {
				return false
			}
		}
		
		return true
	}
	
	private suspend fun notifyOnSlack(config: SlackConfiguration, action: SlackAction) {
		val commitAuthor = webhook.getLatestCommit().author
		val genericCommitAuthor = CommitAuthor(commitAuthor.name, commitAuthor.email)
		val slackRequest = SlackRequest(config)
		
		when (action) {
			SlackAction.GitCommandFailed -> slackRequest.gitCommandFailed(project)
			SlackAction.ManualActionRequired -> slackRequest.manualActionRequired(project)
			SlackAction.ProjectCommandFailed -> slackRequest.projectCommandFailed(project)
			SlackAction.BuildSuccessful -> slackRequest.buildSuccessful(project, genericCommitAuthor)
		}
	}
	
	private fun getManualFiles(): ManualFiles {
		return project.manualFiles.fold(mutableMapOf()) { acc, path ->
			acc[path] = Path(project.path, path).toFile().lastModified()
			return acc
		}
	}
	
	private fun execute(vararg args: String): Boolean {
		val result = ProcessBuilder(args.toList())
			.directory(File(project.path))
			.start()
		
		val resultCode = result.waitFor()
		return result.exitValue() == 0 && resultCode == 0
	}
	
}
