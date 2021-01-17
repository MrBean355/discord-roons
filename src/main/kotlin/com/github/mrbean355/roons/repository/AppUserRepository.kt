/*
 * Copyright 2021 Michael Johnston
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

package com.github.mrbean355.roons.repository

import com.github.mrbean355.roons.AppUser
import org.springframework.data.repository.CrudRepository
import java.util.Date

interface AppUserRepository : CrudRepository<AppUser, Int> {
    fun countByGeneratedId(generatedId: String): Int
    fun findByGeneratedId(generatedId: String): AppUser?
    fun findByLastSeenAfter(date: Date): List<AppUser>
}

/** Update the user's last seen time to now. */
fun AppUserRepository.updateLastSeen(userId: String) {
    require(userId.isNotBlank())
    val user = findByGeneratedId(userId) ?: AppUser(0, userId, null)
    save(user.copy(lastSeen = Date()))
}
