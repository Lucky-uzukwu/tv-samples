// ABOUTME: Data entity representing a sport type (Football, Basketball, etc.)
// ABOUTME: Contains sport classification info with logos and priority ordering

/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.wiltv.data.entities

import kotlinx.serialization.Serializable

@Serializable
data class SportType(
    val id: Int,
    val name: String,
    val coverImagePath: String?,
    val featuredImagePath: String?,
    val logoPath: String?,
    val priority: Int,
    val active: Boolean,
    val  logoUrl: String?,
    val coverImageUrl: String?,
    val featuredImageUrl: String?
)