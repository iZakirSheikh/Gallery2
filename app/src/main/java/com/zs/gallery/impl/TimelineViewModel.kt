/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 23-07-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zs.gallery.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearbyError
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.primex.core.Rose
import com.primex.preferences.value
import com.zs.api.store.MediaFile
import com.zs.api.store.MediaProvider
import com.zs.compose_ktx.toast.Toast
import com.zs.gallery.R
import com.zs.gallery.common.GroupSelectionLevel
import com.zs.gallery.files.TimelineViewState
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "TimelineViewModel"

open class TimelineViewModel(
    provider: MediaProvider
) : MainViewModel<MediaFile>(provider), TimelineViewState {
    // This property holds the current list of MediaFile items.
    // It should only be updated when the actual underlying data changes.
    // Since this is not observed anywhere so using observable state for this is bit too much
    override var values: List<MediaFile> = emptyList()

    // Point to the existing id property
    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    override val MediaFile.id: Long get() = id
    override var data: Map<String, List<MediaFile>>? by mutableStateOf(null)

    override fun evaluateGroupSelectionLevel(key: String): GroupSelectionLevel {
        // Return NONE if data is not available.
        val data = data?.get(key) ?: return GroupSelectionLevel.NONE
        // Count selected
        val count = data.count { it.id in selected }
        return when (count) {
            data.size -> GroupSelectionLevel.FULL // All items in the group are selected.
            in 1..data.size -> GroupSelectionLevel.PARTIAL // Some items in the group are selected.
            else -> GroupSelectionLevel.NONE // No items in the group are selected.
        }
    }

    override fun select(key: String) {
        // Return if data is not available.
        val data = data ?: return
        // Get the current selection level of the group.
        val level = evaluateGroupSelectionLevel(key)
        // Get the IDs of all items in the group.
        val all = data[key]?.map { it.id } ?: emptyList()
        // Update the selected items based on the group selection level.
        when (level) {
            GroupSelectionLevel.NONE -> selected.addAll(all) // Select all items in the group.
            GroupSelectionLevel.PARTIAL -> selected.addAll(all.filterNot { it in selected }) // Select only unselected items.
            GroupSelectionLevel.FULL -> selected.removeAll(all.filter { it in selected }) // Deselect all selected items.
        }
    }

    override suspend fun refresh() {
        // Fetch files from the provider, ordered by modification date in descending order.
        values = provider.fetchFiles(order = MediaProvider.COLUMN_DATE_MODIFIED, ascending = false)
        data = values
            // Group the files by their relative time span (e.g., "Today", "Yesterday").
            .groupBy {
                DateUtils.getRelativeTimeSpanString(
                    it.dateModified,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS
                ).toString()
            }
    }
}