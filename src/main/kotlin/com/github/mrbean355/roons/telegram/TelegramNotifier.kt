/*
 * Copyright 2023 Michael Johnston
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mrbean355.roons.telegram

import com.github.mrbean355.roons.SendHtmlMessage
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot

private const val CHANNEL_ID = "@bulldog_sounds"

@Component
class TelegramNotifier @VisibleForTesting constructor(
    private val bot: TelegramLongPollingBot,
    private val logger: Logger,
    private val chatId: String?
) {

    @Autowired
    constructor(bot: TelegramLongPollingBot, logger: Logger) : this(bot, logger, System.getenv("TELEGRAM_CHAT"))

    fun sendPrivateMessage(text: String) {
        if (chatId != null) {
            bot.execute(SendHtmlMessage(chatId, text))
        } else {
            logger.info(text)
        }
    }

    fun sendChannelMessage(text: String) {
        if (chatId != null) {
            bot.execute(SendHtmlMessage(CHANNEL_ID, text))
        } else {
            logger.info("$CHANNEL_ID: $text")
        }
    }
}
