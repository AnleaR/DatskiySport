package bot

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

class TelegramService(private val botToken: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    // Отправка сообщения
    suspend fun sendMessage(chatId: Long, text: String): TelegramMessageResponse {
        val method = "sendMessage"
        val url = "https://api.telegram.org/bot$botToken/$method"
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(chatId, text))
        }.body()
    }

    // Редактирование сообщения
    suspend fun editMessage(chatId: Long, messageId: Int, text: String) {
        val method = "editMessage"
        val url = "https://api.telegram.org/bot$botToken/$method"
        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(EditMessageTextRequest(chatId, messageId, text))
        }
    }

    // Закрепление сообщения
    suspend fun pinMessage(chatId: Long, messageId: Int) {
        val url = "https://api.telegram.org/bot$botToken/pinChatMessage"
        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(PinMessageRequest(chatId, messageId))
        }
    }
    

    // --- DTOs ---
    @Serializable
    data class SendMessageRequest(val chat_id: Long, val text: String)

    @Serializable
    data class EditMessageTextRequest(val chat_id: Long, val message_id: Int, val text: String)

    @Serializable
    data class PinMessageRequest(val chat_id: Long, val message_id: Int)

    @Serializable
    data class TelegramMessageResponse(val ok: Boolean, val result: TelegramMessage? = null)

    @Serializable
    data class TelegramMessage(val message_id: Int, val chat: TelegramChat, val text: String)

    @Serializable
    data class TelegramChat(val id: Long, val type: String)
}