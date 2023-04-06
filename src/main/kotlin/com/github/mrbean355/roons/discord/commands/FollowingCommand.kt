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

package com.github.mrbean355.roons.discord.commands

import com.github.mrbean355.roons.repository.DiscordBotSettingsRepository
import com.github.mrbean355.roons.repository.loadSettings
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class FollowingCommand(
    private val discordBotSettingsRepository: DiscordBotSettingsRepository
) : BotCommand {

    override val name get() = "following"
    override val description get() = "Check who is being followed."

    override fun handleCommand(event: SlashCommandInteractionEvent) {
        val guild = event.guild ?: return
        val followedUser = discordBotSettingsRepository.loadSettings(guild.id).followedUser

        if (followedUser == null) {
            event.reply("I'm not following anyone at the moment.").setEphemeral(true).queue()
        } else {
            guild.retrieveMemberById(followedUser).queue { member ->
                event.reply("I'm following ${member.effectiveName}.").setEphemeral(true).queue()
            }
        }
    }
}