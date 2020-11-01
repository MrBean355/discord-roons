package com.github.mrbean355.roons.controller

import com.github.mrbean355.roons.DotaModDto
import com.github.mrbean355.roons.annotation.DOTA_MOD_CACHE_NAME
import com.github.mrbean355.roons.annotation.DotaModCache
import com.github.mrbean355.roons.asDto
import com.github.mrbean355.roons.repository.DotaModRepository
import com.github.mrbean355.roons.repository.MetadataRepository
import com.github.mrbean355.roons.repository.adminToken
import com.github.mrbean355.roons.telegram.TelegramNotifier
import org.springframework.cache.CacheManager
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mods")
class ModController(
        private val dotaModRepository: DotaModRepository,
        private val metadataRepository: MetadataRepository,
        private val telegramNotifier: TelegramNotifier,
        private val cacheManager: CacheManager
) {

    @GetMapping
    @DotaModCache
    fun listMods(): Iterable<DotaModDto> = dotaModRepository.findAll().map { it.asDto() }

    @GetMapping("{key}")
    @DotaModCache
    fun getMod(@PathVariable("key") key: String): ResponseEntity<DotaModDto> {
        val mod = dotaModRepository.findById(key)
        if (!mod.isPresent) {
            return ResponseEntity(NOT_FOUND)
        }
        return ResponseEntity.ok(mod.get().asDto())
    }

    @PatchMapping("{key}")
    fun patchMod(
            @PathVariable("key") key: String,
            @RequestParam("hash") hash: String,
            @RequestParam("size") size: Int,
            @RequestParam("token") token: String
    ): ResponseEntity<Void> {
        if (token != metadataRepository.adminToken) {
            return ResponseEntity(UNAUTHORIZED)
        }
        val mod = dotaModRepository.findById(key)
        if (!mod.isPresent) {
            return ResponseEntity(NOT_FOUND)
        }
        dotaModRepository.save(mod.get().copy(size = size, hash = hash))
        cacheManager.getCache(DOTA_MOD_CACHE_NAME)?.clear()
        // TODO: Change to channel message once we're in prod.
        telegramNotifier.sendMessage("The \"${mod.get().name}\" mod has been updated.")
        return ResponseEntity.ok().build()
    }

    @GetMapping("refresh")
    fun refreshMods(@RequestParam("token") token: String): ResponseEntity<Void> {
        if (token != metadataRepository.adminToken) {
            return ResponseEntity(UNAUTHORIZED)
        }
        cacheManager.getCache(DOTA_MOD_CACHE_NAME)?.clear()
        return ResponseEntity.ok().build()
    }
}