package api

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.telegramWebhookRoute(updateHandler: suspend (TelegramUpdate) -> Unit) {
    post("/webhook/telegram") {
        val update = call.receive<TelegramUpdate>()

        // Можно проверять IP Telegram или токен, если передаём его через секретный query param
        updateHandler(update)

        call.respondText("ok") // Telegram ждёт ответ "ok"
    }
}

@Serializable
data class TelegramUpdate(
    val update_id: Long,
    val message: TelegramMessage? = null,
    val callback_query: TelegramCallbackQuery? = null,
    val web_app_data: TelegramWebAppData? = null
)

@Serializable
data class TelegramMessage(
    val message_id: Int,
    val chat: TelegramChat,
    val from: TelegramUser? = null,
    val text: String? = null
)

@Serializable
data class TelegramChat(val id: Long, val type: String)

@Serializable
data class TelegramUser(
    val id: Long,
    val first_name: String,
    val last_name: String? = null,
    val username: String? = null
)

@Serializable
data class TelegramCallbackQuery(val id: String, val from: TelegramUser, val data: String)

@Serializable
data class TelegramWebAppData(val data: String)
