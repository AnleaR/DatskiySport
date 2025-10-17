package bot

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

class TelegramWebhook(
    private val botToken: String,
    private val baseUrl: String
) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Volatile
    private var secretToken: String = generateSecretToken()
    private val webhookPath = "/webhook/telegram"
    private var refreshJob: Job? = null


    suspend fun start(application: Application, secretLifetime: Int) {
        setWebhook(secretToken)

        // Создаём маршрут для приёма апдейтов
        application.routing {
            get("/") {
                call.respondText("In development...")
            }
            post(webhookPath) {
                val headerToken = call.request.header("X-Telegram-Bot-Api-Secret-Token")
                if (headerToken != secretToken) {
                    call.respond(HttpStatusCode.Forbidden, "Invalid secret token")
                    return@post
                }

                val update = call.receive<TelegramUpdate>()
                // Фильтруем только сообщения и нажатия кнопок
                if (update.message != null || update.callbackQuery != null) {
                    handleUpdate(application, update)
                }
            }
        }

        // Каждые 4 часа обновляем токен и webhook
        refreshJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(secretLifetime.hours)
                secretToken = generateSecretToken()
                deleteWebhook()
                setWebhook(secretToken)
            }
        }
    }

    suspend fun stop() {
        deleteWebhook()
        refreshJob?.cancel()
    }

    private fun handleUpdate(application: Application, update: TelegramUpdate) {
        // TODO: logic
        if (update.message != null) {
            application.log.info("Получено сообщение: ${update.message.text} от ${update.message.chat.id}");
        } else if (update.callbackQuery != null) {
            application.log.info("Нажата кнопка: ${update.callbackQuery.data} пользователем ${update.callbackQuery.from.id}");
        }
    }

    private suspend fun setWebhook(secret: String) {
        val url = "https://api.telegram.org/bot$botToken/setWebhook"
        val webhookUrl = "$baseUrl$webhookPath"
        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(
                SetWebhookRequest(
                    url = webhookUrl,
                    allowed_updates = listOf("message", "callback_query"),
                    secret_token = secret
                )
            )
        }
    }

    private suspend fun deleteWebhook() {
        val url = "https://api.telegram.org/bot$botToken/deleteWebhook"
        client.post(url)
    }

    private fun generateSecretToken(length: Int = 256): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-"
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }


    // --- DTOs ---
    @Serializable
    data class SetWebhookRequest(val url: String, val allowed_updates: List<String>, val secret_token: String)

    @Serializable
    data class TelegramUpdate(
        val update_id: Long,
        val message: TelegramMessage? = null,
        val callbackQuery: CallbackQuery? = null
    )

    @Serializable
    data class TelegramMessage(val message_id: Int, val chat: Chat, val text: String? = null)

    @Serializable
    data class CallbackQuery(val id: String, val from: User, val data: String?)

    @Serializable
    data class Chat(val id: Long, val type: String)

    @Serializable
    data class User(val id: Long, val first_name: String, val last_name: String? = null)
}
