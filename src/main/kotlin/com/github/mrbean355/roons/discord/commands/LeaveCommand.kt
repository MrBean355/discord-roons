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

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class LeaveCommand : BotCommand {

    override val name get() = "leave"
    override val description get() = "Leave the current voice channel."

    override fun handleCommand(event: SlashCommandInteractionEvent) {
        val member = event.member ?: return

        if (member.guild.audioManager.isConnected) {
            val channelName = member.guild.audioManager.connectedChannel?.name
            member.guild.audioManager.closeAudioConnection()
            event.reply("I'm disconnecting from `$channelName`.").queue()
        } else {
            event.reply("I'm not connected to a voice channel.").setEphemeral(true).queue()
        }
    }
}