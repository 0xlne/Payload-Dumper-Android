package com.rajmani7584.payloaddumper.models

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.rajmani7584.payloaddumper.MainActivity
import com.rajmani7584.payloaddumper.ui.screens.HomeScreens
import com.rajmani7584.payloaddumper.ui.screens.LogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataViewModel(application: Application): AndroidViewModel(application) {

    private val settingsDataStore = SettingsData(application)

    val isDarkTheme =
        settingsDataStore.darkTheme.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isDynamicColor =
        settingsDataStore.dynamicColor.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val concurrency =
        settingsDataStore.concurrency.stateIn(viewModelScope, SharingStarted.Eagerly, 4)
    val isListView =
        settingsDataStore.listView.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val autoDelete =
        settingsDataStore.autoDelete.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _hasPermission = mutableStateOf<Boolean?>(null)
    val hasPermission: State<Boolean?> = _hasPermission
    private val _log = mutableStateListOf<LogEntry>()
    val logs: List<LogEntry> get() = _log

    fun println(msg: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _log.add(LogEntry(message = msg, timestamp = timestamp))
    }
    fun setDarkTheme(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveDarkTheme(value)
        }
    }

    fun setDynamicColor(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveDynamicColor(value)
        }
    }

    fun setConcurrency(value: Int) {
        viewModelScope.launch {
            settingsDataStore.saveConcurrency(value)
        }
    }

    fun setListView(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveListView(value)
        }
    }

    fun setAutoDelete(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoDelete(value)
        }
    }

    fun setPermission(activity: MainActivity) {
        _hasPermission.value = Utils.hasPermission(activity)
    }

    fun requestPermission(activity: MainActivity) {
        Utils.requestPermission(activity, this)
        _hasPermission.value = Utils.hasPermission(activity)
    }


    val externalStorage: String = Environment.getExternalStorageDirectory().absolutePath

    private val _payloadPath = mutableStateOf<String?>(null)
    val payloadPath: State<String?> = _payloadPath
    private val _payloadRaw = mutableStateOf<String?>(null)
    val payloadRaw: State<String?> = _payloadRaw
    private val _outputDirectory = mutableStateOf(externalStorage)
    val outputDirectory: State<String> = _outputDirectory
    private val _lastDirectory = mutableStateOf(externalStorage)
    val lastDirectory: State<String> = _lastDirectory
    private val _payloadError = mutableStateOf<String?>(null)
    val payloadError: State<String?> = _payloadError
    private val _isSelecting = mutableStateOf(false)
    val isSelecting: State<Boolean> = _isSelecting
    private val _isExtracting = mutableStateOf(false)
    val isExtracting: State<Boolean> = _isExtracting
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    private val _payload = mutableStateOf<Payload?>(null)
    val payload: State<Payload?> = _payload

    fun initPayload(path: String, navController: NavHostController) {
        println("Selected ${path.split("/").last()}")
        _payloadPath.value = null
        _selectedPartition.value = emptyList()
        _completedPartition.value = emptyMap()
        _payload.value = null
        _isSelecting.value = false
        _isExtracting.value = false
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _payloadError.value = null
            delay(100)
            _payloadPath.value = path
            PayloadDumper.getPartitions(this@DataViewModel, path).onSuccess { payload ->
                _payload.value = payload
                _outputDirectory.value = Utils.setupOutDir(
                    "${_lastDirectory.value}/${
                        payload.name.let {
                            it.removeSuffix(".${it.split(".").last()}")
                        }
                    }", 0
                )
                viewModelScope.launch(Dispatchers.Main) {
                    navController.navigate(HomeScreens.EXTRACT)
                }
                println("Success: Payload Info Fetched")
            }.onFailure {
                _payload.value = null
                _payloadError.value = it.message
                println("Error: ${payloadError.value}")
                this.cancel()
            }
        }.invokeOnCompletion {
            _isLoading.value = false
            viewModelScope.launch(Dispatchers.IO) {
                val res = PayloadDumper.getRaw(this@DataViewModel, path)
                if (res.startsWith("Error")) {
                    _payloadRaw.value = null
                    this@DataViewModel.println(res)
                } else {
                    _payloadRaw.value =
                        res.trim().removeSuffix("\"").removePrefix("\"").replace("\\", "")
                    this@DataViewModel.println("Payload Raw Data fetched Successfully")
                }
            }
        }
    }

    fun setLastDirectory(s: String) {
        _lastDirectory.value = s
    }

    fun setOutputDirectory(currentPath: String) {
        _outputDirectory.value = currentPath
    }

    private val _selectedPartition = mutableStateOf<List<String>>(emptyList())
    val selectedPartition: State<List<String>> = _selectedPartition

    private val _completedPartition = MutableStateFlow<Map<String, PartitionState>>(emptyMap())
    val completedPayload: StateFlow<Map<String, PartitionState>> = _completedPartition

    fun selectPartition(partition: Partition, value: Boolean) {
        _selectedPartition.value = _selectedPartition.value.toMutableList().apply {
            if (value) add(partition.name)
            else remove(partition.name)
        }
        _isSelecting.value = _selectedPartition.value.isNotEmpty()
    }
    fun extract() {
        if (_payload.value == null) {
            println("Error: No Payload Selected")
            return
        }
        _completedPartition.value = _completedPartition.value.toMutableMap().apply {
            _selectedPartition.value.forEach { put(it, PartitionState(PartitionState.SELECTED)) }
        }
        if (_completedPartition.value.isEmpty()) {
            println("Error: No Partition Selected to Extract")
            return
        }
        _isExtracting.value = true
        _isSelecting.value = false

        println("Extract - ${_selectedPartition.value.joinToString()}\nconcurrency: ${concurrency.value}")
        viewModelScope.extract(_selectedPartition.value.toSet(), concurrency.value) { name ->
            withContext(Dispatchers.IO) {
                extractSelected(name)
            }
        }.invokeOnCompletion { c ->
            println(c?.message)
            _isExtracting.value = false
            _selectedPartition.value = emptyList()
        }
    }

    private fun extractSelected(name: String) {
        if (!_selectedPartition.value.contains(name)) return
        _selectedPartition.value = _selectedPartition.value.filter { it != name }

        _payload.value?.let { payload ->
            val partition = payload.partitions.find { it.name == name }
            if (partition == null) {
                println("Error: No Partition named $name found in payload")
                println("${payload.partitions}")
                return
            }
            val outputName = "${_outputDirectory.value}/${
                Utils.setupPartitionName(
                    _outputDirectory.value,
                    partition.name,
                    0
                )
            }.img"
            updateStatus(partition.name, PartitionState(PartitionState.EXTRACTING, 0))

            println("Extracting ${partition.name}")

            PayloadDumper.extract(
                payload.path,
                partition.name,
                outputName,
                onProgress = {
                    val status = _completedPartition.value[partition.name]
                    updateStatus(partition.name, status?.copy(progress = it, message = "Extracting to $outputName: $it%"))
                },
                onVerify = {
                    val status = _completedPartition.value[partition.name]
                    when (it) {
                        0 -> {
                            updateStatus(partition.name, status?.copy(statusCode = PartitionState.VERIFYING, message = "Verifying"))
                            println("Verifying hash for ${partition.name}")
                        }
                        1 -> {
                            updateStatus(partition.name, status?.copy(statusCode = PartitionState.EXTRACTED, message = "Extracted to $outputName"))
                            println("Success:Extracted ${partition.name} successfully to $outputName")
                        }
                        2 -> {
                            updateStatus(partition.name, status?.copy(statusCode = PartitionState.FAILED, message = "Hash mismatched"))
                            println("Hash verification failed for ${partition.name}")
                        }
                    }
                }
            ).onFailure { err ->
                val status = _completedPartition.value[partition.name]
                updateStatus(partition.name, status?.copy(statusCode = PartitionState.FAILED, message = "Error: Failed to extract ${partition.name}\n: ${err.message}"))
                println("Error: Failed to extract ${partition.name}\n${err.message}")
                if (autoDelete.value) {
                    File(outputName).delete()
                    println("Auto Deleted $outputName")
                }
            }
        }
    }

    private fun updateStatus(name: String, status: PartitionState?) {
        if (!_completedPartition.value.containsKey(name)) return
        _completedPartition.value = _completedPartition.value.toMutableMap().apply { put(name, status ?: PartitionState()) }
    }

    fun selectAll(value: Boolean) {
        if (isExtracting.value) return
        _payload.value?.let {
            it.partitions.forEach { p -> selectPartition(p, value) }
            println("${if (value) "S" else "Des"}elected All")
        }
    }
    fun invertSelection() {
        if (isExtracting.value) return
        _payload.value?.let { it.partitions.forEach { p -> selectPartition(p, !_selectedPartition.value.contains(p.name)) } }
        println("Selection inverted")
    }

    private fun <L, F> CoroutineScope.extract(
        list: Set<L>,
        threads: Int,
        function: suspend (L) -> F
    ): Job {
        val semaphore = Semaphore(threads)

        return launch {
            list.map { name ->
                async {
                    semaphore.withPermit {
                        function(name)
                    }
                }
            }.awaitAll()
        }
    }

}
data class PartitionState(val statusCode: Int = this.SELECTED, val progress: Long = 0, val message: String = "") {
    companion object {
        const val SELECTED = 0
        const val EXTRACTING = 1
        const val VERIFYING = 2
        const val EXTRACTED = 3
        const val FAILED = 4
    }
}