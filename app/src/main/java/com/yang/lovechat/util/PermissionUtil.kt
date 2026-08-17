package com.yang.lovechat.util

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri


fun Context.hasNotificationPermission(): Boolean {

    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    return notificationManager.areNotificationsEnabled()
}


fun Context.openNoticePermissionDetail() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }

    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${packageName}".toUri()
        }
    }
    startActivity(intent)
}