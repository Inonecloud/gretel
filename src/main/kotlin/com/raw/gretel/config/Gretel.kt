package com.raw.gretel.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class Gretel(
    @Value("\${bot.token}")
    private val token: String
) : TelegramLongPollingBot(token) {

    override fun getBotUsername(): String {
        TODO("Not yet implemented")
    }

    override fun onUpdateReceived(update: Update?) {
        TODO("Not yet implemented")
    }
}