/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 21-07-2024.
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

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NearbyError
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.primex.core.Rose
import com.zs.domain.store.MediaProvider
import com.zs.foundation.toast.Toast
import com.zs.gallery.R
import com.zs.gallery.folders.FoldersViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val TAG = "FoldersViewModel"

class FoldersViewModel(
    private val provider: MediaProvider
) : KoinViewModel(), FoldersViewState {
    // Trigger for refreshing the list
    private val trigger = MutableStateFlow(false)
    private var _ascending: Boolean by mutableStateOf(false)
    private var _order: Int by mutableIntStateOf(FoldersViewState.ORDER_BY_DATE_MODIFIED)

    private fun invalidate() {
        trigger.value = !trigger.value
    }

    override var ascending: Boolean
        get() = _ascending
        set(value) {
            _ascending = value
            invalidate()
        }

    override var order: Int
        get() = _order
        set(value) {
            _order = value
            invalidate()
        }

    override val data = provider
        // Observe the changes in the URI
        .observer(MediaProvider.EXTERNAL_CONTENT_URI)
        // Observe the changes in trigger also
        .combine(trigger) { _, _ ->
            val folders = provider.fetchFolders()
            val result = when (_order) {
                FoldersViewState.ORDER_BY_SIZE -> folders.sortedBy { it.size }
                FoldersViewState.ORDER_BY_DATE_MODIFIED -> folders.sortedBy { it.lastModified }
                FoldersViewState.ORDER_BY_NAME -> folders.sortedBy { it.name }
                else -> error("Oops invalid id passed $_order")
            }
            if (ascending) result else result.reversed()
        }
        // Catch any exceptions in upstream flow and emit using the snackbar.
        .catch { exception ->
            Log.e(TAG, "provider: ${exception.message}")
            // Handle any exceptions that occur during the flow.
            // This might involve logging the exception using Firebase Crashlytics.
            // Display a toast message to the user, indicating something went wrong and suggesting they report the issue.
            val action = showToast(
                exception.message ?: getText(R.string.msg_unknown_error),
                getText(R.string.report),
                Icons.Outlined.NearbyError,
                Color.Rose,
                Toast.PRIORITY_HIGH
            )
        }
        // Convert to state.
        .stateIn(viewModelScope, started = SharingStarted.Lazily, null)
}