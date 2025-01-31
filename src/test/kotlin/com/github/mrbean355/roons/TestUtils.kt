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

package com.github.mrbean355.roons

import com.github.mrbean355.roons.component.Clock
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File

fun loadTestResource(name: String): String {
    val pathname = Thread.currentThread().contextClassLoader.getResource(name)?.file
    require(pathname != null) { "Resource not found: $name" }
    return File(pathname).readText()
}

fun assertTimeIsRoughlyNow(time: Long?) {
    val diff = System.currentTimeMillis() - (time ?: 0)
    assertTrue(diff < 250, "Expected time $time to be the current time but was off by $diff millis")
}

class TestClock(time: Long) : Clock {
    override val currentTimeMs = time
}