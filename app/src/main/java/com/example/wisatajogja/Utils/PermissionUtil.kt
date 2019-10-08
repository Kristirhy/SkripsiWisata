package com.example.wisatajogja.Utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.util.ArrayList

abstract class PermissionUtil {

    val STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val LOCATION = Manifest.permission.ACCESS_FINE_LOCATION

    /* Permission required for application */
    val PERMISSION_ALL = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun goToPermissionSettingScreen(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    fun isAllPermissionGranted(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = PERMISSION_ALL
            if (permission.size == 0) return false
            for (s in permission) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        s
                    ) !== PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    fun getDeniedPermission(act: Activity): Array<String> {
        val permissions = ArrayList<String>()
        for (i in PERMISSION_ALL.indices) {
            val status = act.checkSelfPermission(PERMISSION_ALL[i])
            if (status != PackageManager.PERMISSION_GRANTED) {
                permissions.add(PERMISSION_ALL[i])
            }
        }

        return permissions.toTypedArray()
    }


    fun isGranted(ctx: Context, permission: String): Boolean {
        return if (!Tools.needRequestPermission()) true else ctx.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationGranted(ctx: Context): Boolean {
        return isGranted(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun isStorageGranted(ctx: Context): Boolean {
        return isGranted(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }


    fun showSystemDialogPermission(fragment: Fragment, perm: String) {
        fragment.requestPermissions(arrayOf<String>(perm), 200)
    }

    fun showSystemDialogPermission(act: Activity, perm: String) {
        act.requestPermissions(arrayOf(perm), 200)
    }

    fun showSystemDialogPermission(act: Activity, perm: String, code: Int) {
        act.requestPermissions(arrayOf(perm), code)
    }
}
