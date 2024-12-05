package com.rajmani7584.payloaddumper

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
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

    val externalStorage = Environment.getExternalStorageDirectory().absolutePath
    private val _outputDirectory =
        mutableStateOf("$externalStorage/PayloadDumperAndroid")
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

    fun initPayload(path: String, navController: NavHostController) {
        _payloadPath.value = null
        _payloadInfo.value = null
        _isSelecting.value = false
        _isExtracting.value = false
        viewModelScope.launch(Dispatchers.IO) {
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
                viewModelScope.launch(Dispatchers.Main) {
                    navController.navigate(Screens.EXTRACT)
                }
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

        viewModelScope.extractOnThread(
            partitionStatus.keys,
            concurrency.value
        ) { name ->
            withContext(Dispatchers.IO) {
                extractSelected(name)
                partitionStatus[name]?.let {
                    if (it.statusCode == 4) {
                        it.output?.let { File(it).delete() }
                    }
                }
            }
        }.invokeOnCompletion {
            _partitionStatus.value.forEach {
                selectCard(it.value, false)
            }
            _isExtracting.value = false
        }
    }

    private fun <K, F> CoroutineScope.extractOnThread(
        list: Set<K>,
        thread: Int,
        function: suspend (K) -> F
    ): Job {
        val semaphore = Semaphore(thread)

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

    fun extractSelected(name: String) {

        payloadPath.value?.let { path ->
            val status = _partitionStatus.value[name] ?: return
            val outputName = Utils.setupPartitionName(outputDirectory.value, name, 0)
            val out = "${outputDirectory.value}/$outputName.img"
            updatePartitionStatus(name, status.copy(output = out, progress = 0, statusCode = null))

            val result = PayloadDumper.extract(
                path,
                name,
                out,
                onProgress = {
                    updatePartitionStatus(
                        name,
                        status.copy(
                            progress = it.toInt(),
                            message = "Extracting $name as $out ($it%)",
                            statusCode = 1
                        )
                    )
                },
                onVerify = {
                    when (it) {
                        0 -> updatePartitionStatus(
                            name,
                            status.copy(message = "Verifying", statusCode = 2, progress = 100)
                        )

                        1 -> updatePartitionStatus(
                            name,
                            status.copy(
                                message = "Extracted to $out",
                                statusCode = 3,
                                progress = 100
                            )
                        )

                        2 -> updatePartitionStatus(
                            name,
                            status.copy(
                                message = "Hash mismatch error!",
                                statusCode = 4,
                                progress = 100
                            )
                        )
                    }
                })
            result.onFailure {
                updatePartitionStatus(
                    name,
                    status.copy(
                        message = it.message ?: "Unknown error",
                        statusCode = 4
                    )
                )
            }
        }
    }
}