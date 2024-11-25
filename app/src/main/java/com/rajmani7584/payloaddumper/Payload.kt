package com.rajmani7584.payloaddumper

import java.io.File

class Payload {

    private external fun getPartitionList(path: String): String
    private external fun extractPartition(
        path: String,
        partition: String,
        outputPath: String,
        onProgress: RustCallback
    ): String


    init {
        System.loadLibrary("payload-dumper-rust")
    }

    fun getPartitions(path: String): Result<List<Pair<String, Float>>> {
        val partitionList = getPartitionList(path)
        if (!File(path).exists()) return Result.failure(Exception("File not found or maybe deleted"))
        return if (partitionList.startsWith("Partitions:")) {
            Result.success(partitionList.removePrefix("Partitions:").trim().split(",").mapNotNull { partition ->
                val parts = partition.split("|")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val size = parts[1].toFloatOrNull()?.div(1000) ?: 0f
                    name to size
                } else null
            })
        } else if (partitionList.startsWith("Err:")) {
            Result.failure(Exception(partitionList.removePrefix("Err:")))
        } else {
            Result.failure(Exception("Can't get list, error: Unknown"))
        }
    }

    fun extract(path: String, partition: String, outputPath: String, onProgress: (Long) -> Unit): Result<String> {
        File(outputPath).parentFile?.apply { if (!exists()) mkdirs() }
        val result = extractPartition(path, partition, outputPath, OnRustCallback(onProgress))
        return if (result.startsWith("Done")) {
            Result.success("Done")
        } else if (result.startsWith("Err:")) {
            Result.failure(Exception(result.removePrefix("Err:")))
        } else {
            Result.failure(Exception("Can't extract, error: Unknown"))
        }
    }
}
interface RustCallback {
    fun onProgressCallback(progress: Long)
}

class OnRustCallback(private val onProgress: (Long) -> Unit): RustCallback {
    override fun onProgressCallback(progress: Long) {
        onProgress(progress)
    }
}