package com.example.barrier_unlocker.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import com.example.barrier_unlocker.REQUEST_CODE_APP_PERMISSION
import pub.devrel.easypermissions.EasyPermissions

object TrackingPerm {
    fun hasUsefulPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    fun requestPermissions(activity : Activity) {
        if (hasUsefulPermissions(activity)) {
            return
        }
        EasyPermissions.requestPermissions(
            activity,
            "You need to accept location permissions to use this app",
            REQUEST_CODE_APP_PERMISSION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        )
    }
}
