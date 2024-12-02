package com.rajmani7584.payloaddumper

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import java.io.File


class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingDataStore(application)

    val isDarkTheme =
        settingsDataStore.darkTheme.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isDynamicColor =
        settingsDataStore.dynamicColor.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val concurrency =
        settingsDataStore.concurrency.stateIn(viewModelScope, SharingStarted.Eagerly, 4)
    val listView = settingsDataStore.listView.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val trueBlack =
        settingsDataStore.trueBlack.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setDarkTheme(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveDarkTheme(value)
        }
    }

    fun setTrueBlack(value: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveTrueBlack(value)
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


    private val _isSelecting = mutableStateOf(false)
    val isSelecting: State<Boolean> = _isSelecting

    fun setSelecting(value: Boolean) {
        _isSelecting.value = value
    }

    private val _isExtracting = mutableStateOf(false)
    val isExtracting: State<Boolean> = _isExtracting

    private val _outputDirectory =
        mutableStateOf("${Environment.getExternalStorageDirectory()}/PayloadDumperAndroid")
    val outputDirectory: State<String> = _outputDirectory

    private val _payloadError = mutableStateOf<String?>(null)
    val payloadError: State<String?> = _payloadError

    fun setOutputDirectory(value: String) {
        _outputDirectory.value = value
    }

    private val _lastDirectory =
        mutableStateOf(Environment.getExternalStorageDirectory().absolutePath)
    val lastDirectory: State<String> = _lastDirectory

    fun setLastDirectory(value: String) {
        _lastDirectory.value = value
    }

    private val _payloadPath = mutableStateOf<String?>(null)
    val payloadPath: State<String?> = _payloadPath

    fun initPayload(path: String) {
        _payloadPath.value = null
        _payloadInfo.value = null
        _isSelecting.value = false
        _isExtracting.value = false
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _payloadError.value = null
            delay(500)
            _payloadPath.value = path
            PayloadDumper.getPartitions(path).onSuccess { payload ->
                _payloadInfo.value = payload
                _outputDirectory.value = Utils.setupOutDir(
                    "${_lastDirectory.value}/${
                        payload.name.let {
                            it.removeSuffix(".${it.split(".").last()}")
                        }
                    }", 0
                )
                initializePartitionStatus(payload.partitions)
            }.onFailure {
                _payloadInfo.value = null
                _payloadError.value = it.message
                this.cancel()
            }
        }.invokeOnCompletion { _isLoading.value = false }
    }

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _payloadInfo = mutableStateOf<Payload?>(null)
    val payloadInfo: State<Payload?> = _payloadInfo

    private val _partitionStatus = mutableStateOf<Map<String, PartitionStatus>>(emptyMap())
    val partitionStatus: State<Map<String, PartitionStatus>> = _partitionStatus

    fun updatePartitionStatus(name: String, state: PartitionStatus) {
        _partitionStatus.value = _partitionStatus.value.toMutableMap().apply {
            put(name, state)
        }
    }

    fun initializePartitionStatus(partition: List<Partition>) {
        _partitionStatus.value = partition.associate {
            it.name to PartitionStatus(it.name, 0, "", null, null)
        }
    }

    fun selectCard(partitionStatus: PartitionStatus, value: Boolean) {
        updatePartitionStatus(partitionStatus.name, partitionStatus.copy(isSelected = value))
        _isSelecting.value =
            _partitionStatus.value.values.filter { it.isSelected == true }.isNotEmpty()
    }

    fun selectAll(value: Boolean) {
        _partitionStatus.value.values.forEach {
            selectCard(it, value)
        }
    }

    fun invertSelection() {
        _partitionStatus.value.values.forEach {
            selectCard(it, !it.isSelected)
        }
    }

    fun extract() {
        val partitionStatus = _partitionStatus.value.filter { it.value.isSelected }

        _isExtracting.value = true
        _isSelecting.value = false

        CoroutineScope(Dispatchers.IO).extractOnThread(
            partitionStatus,
            concurrency.value
        ) { name, status ->
            extractSelected(name, status)
            if (status.hasFailed) {
                status.output?.let {
                    File(it).delete()
                }
            }
        }.invokeOnCompletion {
            _partitionStatus.value.forEach {
                selectCard(it.value, false)
            }
            _isExtracting.value = false
        }
    }

    private fun <K, T, R> CoroutineScope.extractOnThread(
        list: Map<K, T>,
        thread: Int,
        function: suspend (K, T) -> R
    ): Job {
        val semaphore = Semaphore(thread)

        return launch {
            list.map { (name, status) ->
                async {
                    semaphore.withPermit {
                        function(name, status)
                    }
                }
            }.awaitAll()
        }
    }

    fun extractSelected(name: String, status: PartitionStatus) {
        updatePartitionStatus(
            name,
            status.copy(progress = 0, hasFailed = false, isCompleted = false)
        )

        payloadPath.value?.let { path ->
            val outputName = Utils.setupPartitionName(outputDirectory.value, name, 0)
            val out = "${outputDirectory.value}/$outputName.img"
            updatePartitionStatus(name, status.copy(output = out))
            val result = PayloadDumper.extract(
                path,
                name,
                out,
                onProgress = {
                    updatePartitionStatus(
                        name,
                        status.copy(
                            progress = it.toInt(),
                            message = "Extracting $name as $out ($it%)"
                        )
                    )
                },
                onVerify = {
                    when (it) {
                        0 -> updatePartitionStatus(
                            name,
                            status.copy(message = "Verifying", statusCode = 0)
                        )

                        1 -> updatePartitionStatus(
                            name,
                            status.copy(
                                message = "Extracted to $out",
                                statusCode = 1,
                                isCompleted = true,
                                hasFailed = false
                            )
                        )

                        2 -> updatePartitionStatus(
                            name,
                            status.copy(
                                message = "Hash mismatch error!",
                                statusCode = 2,
                                isCompleted = false,
                                hasFailed = true
                            )
                        )
                    }
                })
            result.onFailure {
                updatePartitionStatus(
                    name,
                    status.copy(
                        message = it.message ?: "Unknown error",
                        statusCode = 2,
                        isCompleted = false,
                        hasFailed = true
                    )
                )
            }
        }
    }
}