package me.anlear.util

import java.io.File

object Utils {

    /**
     * Проверяет наличие папки logs в корне проекта.
     * Если её нет — создаёт.
     */
    fun ensureLogsDirectory() {
        val logsDir = File("logs")

        if (!logsDir.exists()) {
            val created = logsDir.mkdirs()
            if (created) {
                println("[INIT] Папка 'logs' успешно создана.")
            } else {
                println("[INIT] Не удалось создать папку 'logs'!")
            }
        }
    }
}
