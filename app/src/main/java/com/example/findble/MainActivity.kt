package com.example.findble

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.findble.common.BleScanHeater
import com.example.findble.common.MainViewModel
import com.example.findble.databinding.ActivityMainBinding
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import splitties.permissions.PermissionRequestResult
import splitties.permissions.requestPermission
import splitties.views.onClick

class MainActivity : FragmentActivity()  {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            ensureBlePermissionOrFinishActivity()
            val result = requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            if (result is PermissionRequestResult.Denied) {
                finish(); return@launch
            }

            @Suppress("MissingPermission")
            binding.button.onClick {
                binding.textView.text = viewModel.logNameAndAppearance()
//                Log.d("TestBLE", "Test BLE Button")
            }

            @Suppress("MissingPermission")
            launch { BleScanHeater.heatUpWhileStarted(lifecycle) }
        }

    }

    private suspend fun ensureBlePermissionOrFinishActivity() = ensureAllPermissions(
        permissionNames = when {
            Build.VERSION.SDK_INT >= 31 -> listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            else -> listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        },
        askDialogTitle = "Location permission required",
        askDialogMessage = "Bluetooth Low Energy can be used for location, " +
                "so the permission is required."
    ) { finish(); awaitCancellation() }
}