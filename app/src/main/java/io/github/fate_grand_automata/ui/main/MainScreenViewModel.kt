package io.github.fate_grand_automata.ui.main

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.runner.ScriptRunnerService
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.util.StorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    val prefsCore: PrefsCore,
    val prefs: IPreferences,
    val storageProvider: StorageProvider
) : ViewModel() {

    init {
        // init the perServerConfigs
        CoroutineScope(Dispatchers.IO).launch {
            GameServer.values.filterNot {
                it.betterFgo
            }.forEach { server ->
                prefs.addPerServerConfigPref(server)
            }
        }
    }

    val autoStartService
        get() =
            prefsCore.autoStartService.get()
                    && oncePerActivityStart.getAndSet(false)
                    && !ScriptRunnerService.serviceStarted.value

    private val oncePerActivityStart = AtomicBoolean(false)
    fun activityStarted() = oncePerActivityStart.set(true)

    // Activity context is needed since we can't show AlertDialog with Application context.
    fun ensureRootDir(context: Context): Boolean {
        // 直接返回 true,因为我们已经在 StorageProvider 中设置了默认目录
        return true
    }

    fun onStartMediaProjectionResult(context: Context, intent: Intent?) {
        if (intent == null) {
            Timber.i("MediaProjection cancelled by user")
            ScriptRunnerService.stopService(context)
        } else {
            ScriptRunnerService.startService(context)
            ScriptRunnerService.mediaProjectionToken = intent
        }
    }
}