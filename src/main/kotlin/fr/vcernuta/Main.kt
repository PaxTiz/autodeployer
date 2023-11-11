package fr.vcernuta

import fr.vcernuta.models.Configuration
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.netty.*
import io.ktor.util.*

val AutoDeployerConfigurationKey = AttributeKey<Configuration>("config")

val ReadAutoDeployerConfigurationFile = createApplicationPlugin("Configuration") {
	on(MonitoringEvent(ApplicationStarted)) {
		it.attributes.put(AutoDeployerConfigurationKey, Configuration.readFile())
	}
}

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
	install(ReadAutoDeployerConfigurationFile)
	
	configureRouting()
	configureSerialization()
}

