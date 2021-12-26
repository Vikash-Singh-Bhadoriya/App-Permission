package com.example.apppermission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //Storage + Location Permission
    private val requestExternalStoragePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                onStoragePermissionAlreadyGranted()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their decision.
                onStoragePermissionDenied()
            }
        }
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                when {
                    permissions.getOrElse(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        { false }) -> {
                        // background + foreground location access granted.
                        onLocationPermissionAlreadyGranted()
                    }
                    permissions.getOrElse(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        { false }) -> {
                        // only foreground location access granted.
                        onForegroundLocationPermissionAlreadyGranted()
                    }
                    else -> {
                        // No location access granted.
                        onLocationPermissionDenied()
                    }
                }
            }
            permissions.getOrElse(Manifest.permission.ACCESS_COARSE_LOCATION, { false }) -> {
                onLocationPermissionAlreadyGranted()
            }
            else -> {
                onLocationPermissionDenied()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        save_button.setOnClickListener {
            requestPermission(
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onPermissionAlreadyGranted = ::onStoragePermissionAlreadyGranted,
                reasonForPermission = ::reasonForExternalStoragePermission,
                launchRequestPermission = {
                    requestExternalStoragePermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            )
        }
        local_weather_button.setOnClickListener {
            requestPermission(
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACCESS_BACKGROUND_LOCATION else Manifest.permission.ACCESS_COARSE_LOCATION,
                onPermissionAlreadyGranted = ::onLocationPermissionAlreadyGranted,
                reasonForPermission = ::reasonForLocationPermission,
                launchRequestPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestLocationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        )
                    } else {
                        requestLocationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            )
        }
    }

    private fun requestPermission(
        permission: String,
        onPermissionAlreadyGranted: () -> Unit,
        reasonForPermission: () -> Unit,
        launchRequestPermission: () -> Unit
    ) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    onPermissionAlreadyGranted()
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    //Called when permission is denied
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
                    reasonForPermission()
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    launchRequestPermission()
                }
            }
        } else {
            onPermissionAlreadyGranted()
        }

    private fun onStoragePermissionAlreadyGranted() {
        storage_permission_result_text_view.text = "Storage permission is already granted\nDo Work"
    }

    private fun onLocationPermissionAlreadyGranted() {
        location_permission_result_text_view.text =
            "Location permission is already granted\nDo Work"
    }

    private fun onForegroundLocationPermissionAlreadyGranted() {
        location_permission_result_text_view.text =
            "Only foreground Location permission is already granted\nDo Work"
    }

    private fun onStoragePermissionDenied() {
        storage_permission_result_text_view.text =
            "We can only save the file when storage permission is granted. We will only use this permission for saving file"
    }

    private fun onLocationPermissionDenied() {
        location_permission_result_text_view.text =
            "We can only give local weather, when location permission is granted. We will only use this permission for giving local weather only"
    }

    private fun reasonForExternalStoragePermission() {
        AlertDialog.Builder(this)
            .setTitle("Allow Storage Permission")
            .setMessage("In order to save the file, allow storage permission.\n\nWe will only use this permission for saving file")
            .setPositiveButton("Allow") { _, _ ->
                requestExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Cancel") { _, _ ->
                onStoragePermissionDenied()
            }
            .setIcon(R.drawable.ic_save_black_24dp)
            .show()
    }

    private fun reasonForLocationPermission() {
        AlertDialog.Builder(this)
            .setTitle("Allow Location Permission")
            .setMessage("In order to send local weather, allow location permission.\n\nWe will only use this permission for giving local weather only")
            .setPositiveButton("Allow") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestLocationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    )
                } else {
                    requestLocationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                onLocationPermissionDenied()
            }
            .setIcon(R.drawable.ic_location_on_black_24dp)
            .show()
    }
}