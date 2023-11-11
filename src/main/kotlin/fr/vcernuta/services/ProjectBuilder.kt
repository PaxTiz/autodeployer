package fr.vcernuta.services

import fr.vcernuta.models.GitlabWebhook
import fr.vcernuta.models.Project
import java.io.File
import kotlin.io.path.Path

typealias ManualFiles = Map<String, Long>

enum class SlackAction {
	GitCommandFailed,
	ManualActionRequired,
	ProjectCommandFailed,
	BuildSuccessful,
}

class ProjectBuilder(private val project: Project, private val webhook: GitlabWebhook) {
	
	fun build() {
		val manualFiles = getManualFiles()
		
		if (!getLatestChanges()) {
			println("ERROR: Failed to get latest changes")
			notifyOnSlack(SlackAction.GitCommandFailed)
			return
		}
		
		if (isManualChangeRequired(manualFiles)) {
			println("ERROR: A manual change is required in order to update the project")
			notifyOnSlack(SlackAction.ManualActionRequired)
			return
		}
		
		if (!runProjectCommands()) {
			println("ERROR: There was an error while running deployment commands")
			notifyOnSlack(SlackAction.ProjectCommandFailed)
			return
		}
		
		notifyOnSlack(SlackAction.BuildSuccessful)
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
	
	private fun notifyOnSlack(action: SlackAction) {
		// TODO: Send a message on a Slack channel
		when (action) {
			SlackAction.GitCommandFailed -> {
			
			}
			
			SlackAction.ManualActionRequired -> {
			
			}
			
			SlackAction.ProjectCommandFailed -> {
			
			}
			
			SlackAction.BuildSuccessful -> {
			
			}
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
