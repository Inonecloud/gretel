package com.raw.gretel

import com.raw.gretel.service.GretelBot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


@SpringBootApplication
@EnableMongoRepositories(basePackages = ["com.raw.gretel.repository"])
class Application

fun main(args: Array<String>) {
    var ctx = runApplication<Application>(*args)
    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    botsApi.registerBot(ctx.getBean("gretelBot", GretelBot::class.java))
}
