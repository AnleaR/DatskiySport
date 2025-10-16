package bot

import io.ktor.client.*
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

    suspend fun sendMessage(chatId: String, text: String) {
        val url = "https://api.telegram.org/bot$botToken/sendMessage"
        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(chatId, text))
        }
    }

    suspend fun editMessageText(chatId: String, messageId: Int, text: String) {
        val url = "https://api.telegram.org/bot$botToken/editMessageText"
        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(EditMessageTextRequest(chatId, messageId, text))
        }
    }

    @Serializable
    data class SendMessageRequest(
        val chatId: String,
        val text: String
    )

    @Serializable
    data class EditMessageTextRequest(
        val chatId: String,
        val messageId: Int,
        val text: String
    )
}