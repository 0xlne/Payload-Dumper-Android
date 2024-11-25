package com.rajmani7584.payloaddumper.ui.theme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.rajmani7584.payloaddumper.MainActivity
import java.io.File

class Utils(val activity: MainActivity) {

    fun setupOutDir(outDir: String, counter: Int): String {
        val appDirectory = File("$outDir${if (counter == 0) "" else "(${counter})"}")
        if (!appDirectory.exists()) {
            appDirectory.mkdirs()
        } else {
            if (!appDirectory.isDirectory) {
                return setupOutDir(outDir, counter + 1)
            }
        }
        return appDirectory.absolutePath
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivity(intent)
        } else {
            if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            } else activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }
}