@file:Suppress("unused")

package com.rajmani7584.payloaddumper

import java.io.File

object PayloadDumper {

    private external fun getPartitionList(path: String): String
    private external fun extractPartition(
        path: String,
        partition: String,
        outputPath: String,
        onCallback: RustCallback
    ): String

    init {
        System.loadLibrary("payload-dumper-rust")
    }

    fun getPartitions(path: String): Result<Payload> {
        if (!File(path).exists()) return Result.failure(Exception("Error: File not found or maybe deleted"))
        val result = getPartitionList(path)
        return if (result.startsWith("data:")) {
            val data = result.removePrefix("data:").split(":")
            val manifest = data[0].split("|")
            val partitions = data[1].split(",").map { partition ->
                val parts = partition.split("|")
                Partition(parts[0], parts[1].toFloatOrNull()?.div(1000) ?: 0f, parts[2])
            }
            val payload = Payload(path.split("/").last(), manifest[0], manifest[1], manifest[2], manifest[3].let { if (it == "") "Unknown" else it }, partitions)
            Result.success(payload)
        } else if (result.startsWith("Error:")) {
            Result.failure(Exception(result))
        } else {
            Result.failure(Exception("Error: Can't get list, error: Unknown"))
        }
    }

    fun extract(path: String, partition: String, outputPath: String, onProgress: (Long) -> Unit, onVerify: (Int) -> Unit): Result<String> {
        File(outputPath).parentFile?.apply { if (!exists()) mkdirs() }
        val result = extractPartition(path, partition, outputPath, OnRustCallback(onProgress, onVerify))
        return if (result.startsWith("Done")) {
            Result.success("Done")
        } else if (result.startsWith("Error:")) {
            Result.failure(Exception(result))
        } else {
            Result.failure(Exception("Error: Can't extract, error: Unknown"))
        }
    }
}

interface RustCallback {
    fun onProgressCallback(progress: Long)
    fun onVerifyCallback(status: Int)
}

class OnRustCallback(private val onProgress: (Long) -> Unit, private val onVerify: (Int) -> Unit): RustCallback {
    override fun onProgressCallback(progress: Long) {
        onProgress(progress)
    }

    override fun onVerifyCallback(status: Int) {
        onVerify(status)
    }
}

data class Payload (val name: String, val version: String, val manifestLength: String, val signatureLength: String, val securityPatch: String, val partitions: List<Partition>)
data class Partition (val name: String, val size: Float, val hash: String)
data class PartitionStatus (val name: String, val progress: Int, val message: String, val output: String?, val statusCode: Int?, val isSelected: Boolean = false)