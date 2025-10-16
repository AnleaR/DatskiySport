package me.anlear

import api.telegramWebhookRoute
import bot.TelegramService
import config.ConfigProvider
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import me.anlear.util.Utils

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting()
    Utils.ensureLogsDirectory()

    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        json()
    }

    routing {
        telegramWebhookRoute { update ->
            // здесь вызов UpdateHandler.handle(update)
        }
    }

    val config = ConfigProvider.get();
    val token = config.property("app.telegram.botToken").getString();
    TelegramService(token);
}