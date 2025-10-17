package me.anlear

import bot.TelegramService
import bot.TelegramWebhook
import config.ConfigProvider
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.anlear.util.Utils

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.module() {
    Utils.ensureLogsDirectory()

    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        json()
    }

    val config = ConfigProvider.get();

    val appUrl = config.property("app.url").getString();
    val secretLifetime = config.property("app.secretLifetime").getString().toInt();
    val botToken = config.property("app.telegram.botToken").getString();
    val webhookManager = TelegramWebhook(botToken, appUrl)

    webhookManager.start(this, secretLifetime)
    monitor.subscribe(ApplicationStopped) {
        CoroutineScope(Dispatchers.Default).launch {
            webhookManager.stop()
        }
    }

    val dbUrl = config.property("app.database.url").getString()
    val dbUser = config.property("app.database.user").getString()
    val dbPassword = config.property("app.database.password").getString()

    TelegramService(botToken);
}