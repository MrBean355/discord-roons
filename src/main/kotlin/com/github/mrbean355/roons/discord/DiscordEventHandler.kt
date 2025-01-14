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

package com.github.mrbean355.roons.discord

import com.github.mrbean355.roons.discord.commands.BotCommand
import com.github.mrbean355.roons.repository.DiscordBotSettingsRepository
import com.github.mrbean355.roons.repository.DiscordBotUserRepository
import com.github.mrbean355.roons.repository.MetadataRepository
import com.github.mrbean355.roons.repository.takeStartupMessage
import com.github.mrbean355.roons.telegram.TelegramNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class DiscordEventHandler(
    private val commands: List<BotCommand>,
    private val discordBotUserRepository: DiscordBotUserRepository,
    private val discordBotSettingsRepository: DiscordBotSettingsRepository,
    private val metadataRepository: MetadataRepository,
    private val telegramNotifier: TelegramNotifier,
) : ListenerAdapter() {

    private val botScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReady(event: ReadyEvent) = runBlocking(Dispatchers.IO) {
        // Update slash commands:
        event.jda.updateCommands().queue()
        supervisorScope {
            event.jda.guilds.forEach {
                launch {
                    updateSlashCommands(it)
                }
            }
        }

        // Show startup message if there is one:
        val message = metadataRepository.takeStartupMessage()
            ?.replace("\\n", "\n")

        if (!message.isNullOrBlank()) {
            supervisorScope {
                event.jda.guilds.forEach {
                    launch {
                        it.findWelcomeChannel()?.sendMessage(message)?.queue()
                    }
                }
            }
        }

        val reconnects = AtomicInteger()
        // Reconnect to previous voice channels:
        supervisorScope {
            discordBotSettingsRepository.findAll().forEach { settings ->
                launch {
                    settings.lastChannel?.let { lastChannel ->
                        val guild = event.jda.getGuildById(settings.guildId)
                        val channel = guild?.getVoiceChannelById(lastChannel)
                        if (channel != null) {
                            guild.audioManager.openAudioConnection(channel)
                            reconnects.incrementAndGet()
                        }
                        discordBotSettingsRepository.save(settings.copy(lastChannel = null))
                    }
                }
            }
        }

        telegramNotifier.sendPrivateMessage("⚙️ <b>Started up</b>:\nReconnected to <b>${reconnects.get()}</b> voice channels.")
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        botScope.launch {
            val guild = event.guild
            updateSlashCommands(guild)
            telegramNotifier.sendPrivateMessage("🎉 <b>Joined a guild</b>:\n${guild.name}, ${guild.memberCount} members")

            guild.findWelcomeChannel()?.sendMessage(
                """
                **ALLO, ${guild.name}!** :wave:
                
                Type `/join` for me to join your current voice channel.
                Type `/leave` when you want me to leave the voice channel.
                Type `/follow` for me to follow you when you join & leave voice channels.
                Type `/help` for more commands.
                """.trimIndent()
            )?.queue()
        }
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        botScope.launch {
            telegramNotifier.sendPrivateMessage("😔 <b>Left a guild</b>:\n${event.guild.name}")
            val guildId = event.guild.id
            discordBotUserRepository.deleteByGuildId(guildId)
            discordBotSettingsRepository.deleteByGuildId(guildId)
        }
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        botScope.launch {
            if (event.member.user.isBot) {
                return@launch
            }
            val settings = discordBotSettingsRepository.findOneByGuildId(event.guild.id) ?: return@launch
            if (settings.followedUser == event.member.id) {
                if (event.channelJoined != null) {
                    event.guild.audioManager.openAudioConnection(event.channelJoined)
                } else {
                    event.guild.audioManager.closeAudioConnection()
                }
            }
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        botScope.launch {
            if (event.isFromType(ChannelType.PRIVATE) && !event.author.isBot) {
                event.channel.asPrivateChannel().sendMessage(":no_entry: Please send me commands through a text channel in your server.").queue()
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        botScope.launch {
            if (event.guild == null) {
                event.reply("Please use that command in a server's text channel.").setEphemeral(true).queue()
                return@launch
            }
            commands.find { it.name == event.name }
                ?.handleCommand(event)
        }
    }

    private fun updateSlashCommands(guild: Guild) {
        guild.updateCommands()
            .addCommands(commands.map { Commands.slash(it.name, it.description).apply(it::buildCommand) })
            .queue()
    }

    /** @return the first (if any) [TextChannel] which the bot can read & write to. */
    private fun Guild.findWelcomeChannel(): TextChannel? {
        val self = selfMember
        val defaultChannel = defaultChannel
        if (defaultChannel?.type == ChannelType.TEXT && self.canReadAndWrite(defaultChannel)) {
            return defaultChannel.asTextChannel()
        }
        return textChannels.firstOrNull {
            self.canReadAndWrite(it)
        }
    }

    /** @return `true` if this [Member] can read & write to the [channel]. */
    private fun Member.canReadAndWrite(channel: GuildChannel): Boolean {
        return hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
    }
}