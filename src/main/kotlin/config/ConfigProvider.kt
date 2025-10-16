package config

import io.ktor.server.config.*
import io.ktor.server.config.yaml.*
import java.io.File

object ConfigProvider {

    private val config: ApplicationConfig by lazy { loadConfig() }

    /**
     * Получение конфигурации
     */
    fun get(): ApplicationConfig = config

    /**
     * Логика загрузки конфига:
     * 1. Если есть application.local.yaml -> используем его
     * 2. Иначе используем application.yaml
     *    - Если default=true -> выбрасываем ошибку
     *    - Иначе продолжаем
     */
    private fun loadConfig(): ApplicationConfig {
        val localPath = "src/main/resources/application.local.yaml"
        val defaultPath = "src/main/resources/application.yaml"
        val localFile = File(localPath)
        val defaultFile = File(defaultPath)

        val appConfig: ApplicationConfig = when {
            localFile.exists() -> YamlConfigLoader().load(localPath)
                ?: throw IllegalStateException("Не удалось загрузить конфиг из $localPath")

            defaultFile.exists() -> {
                val cfg = YamlConfigLoader().load(defaultPath)
                    ?: throw IllegalStateException("Не удалось загрузить конфиг из $defaultPath")
                val defaultFlag = cfg.propertyOrNull("default")?.getString()?.toBoolean() ?: true
                if (defaultFlag) {
                    throw IllegalStateException(
                        "Используется дефолтный конфиг `application.yaml`. Пожалуйста, создайте" +
                                " application.local.yaml с реальными значениями или измените `application.yaml`," +
                                " убрав поле `default` или поставив ему значение `false`!"
                    )
                }
                cfg
            };
            else -> throw IllegalStateException("Не найден ни один конфигурационный файл YAML!")
        }

        return appConfig
    }
}