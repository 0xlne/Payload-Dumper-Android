package com.rajmani7584.payloaddumper.models

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.rajmani7584.payloaddumper.MainActivity
import java.io.File


object Utils {

    fun hasPermission(activity: MainActivity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermission(activity: MainActivity, viewModel: DataViewModel) {
        viewModel.println("Requesting file permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivity(intent)
        } else {
            if (activity.shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                viewModel.println("Error: can't show permission dialog! allow from setting")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            } else {
                activity.requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }
    fun setupOutDir(outDir: String, counter: Int): String {
        val appDirectory = File("$outDir${if (counter == 0) "" else "(${counter})"}")
        if (!appDirectory.exists()) {
            return appDirectory.absolutePath
        } else if (!appDirectory.isDirectory) {
            return setupOutDir(outDir, counter + 1)
        }
        return appDirectory.absolutePath
    }
    fun setupPartitionName(
        outputDirectory: String,
        partitionName: String,
        counter: Int
    ): String {
        val partition =
            File(outputDirectory, "${partitionName}${if (counter == 0) "" else "(${counter})"}.img")
        return if (!partition.exists()) {
            partition.name.removeSuffix(".img")
        } else {
            setupPartitionName(outputDirectory, partitionName, counter + 1)
        }
    }

    fun parseSize(size: Float): String {
        return when (size) {
            in 0f..1000f -> "%.2f KB".format(size)
            in 1000f..1000000f -> "%.2f MB".format(size / 1000f)
            in 1000000f..1000000000f -> "%.2f GB".format(size / 1000000f)
            else -> "$size KB"
        }
    }
}