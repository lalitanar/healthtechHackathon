/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.wear.alwayson

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientModeSupport
import com.example.android.wearable.wear.alwayson.ble.common.BleMainViewModel
import com.example.android.wearable.wear.alwayson.ble.common.BleScanHeater
import com.example.android.wearable.wear.alwayson.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.AndroidEntryPoint
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random
import kotlinx.coroutines.*
import splitties.permissions.PermissionRequestResult
import splitties.permissions.requestPermission
import splitties.views.onClick
import com.example.android.wearable.wear.alwayson.healthservices.MainViewModel


/**
 * IMPORTANT NOTE: Most apps shouldn't use always on ambient mode, as it drains battery life. Unless
 * required, it's much better to allow the system to take over after the user stops interacting
 * with your app.
 *
 * Demonstrates support for *Ambient Mode* by attaching ambient mode support to the activity,
 * and listening for ambient mode updates (onEnterAmbient, onUpdateAmbient, and onExitAmbient) via a
 * named AmbientCallback subclass.
 *
 * Also demonstrates how to update the display more frequently than every 60 seconds, which is
 * the default frequency, using an AlarmManager. The Alarm code is only necessary for the custom
 * refresh frequency; it can be ignored for basic ambient mode support where you can simply rely on
 * calls to onUpdateAmbient() by the system.
 *
 * There are two modes: *ambient* and *active*. To trigger future display updates, we
 * use coroutines for active mode and an Alarm for ambient mode.
 *
 * Why not use just one or the other? Coroutines are generally less battery intensive and can be
 * triggered every second. However, they can not wake up the processor (common in ambient mode).
 *
 * Alarms can wake up the processor (what we need for ambient move), but they are less efficient
 * compared to coroutines when it comes to quick update frequencies.
 *
 * Therefore, we use coroutines for active mode (can trigger every second and are better on the
 * battery), and we use an Alarm for ambient mode (only need to update once every 10 seconds and
 * they can wake up a sleeping processor).
 *
 * The activity waits 10 seconds between doing any processing (getting data, updating display
 * etc.) while in ambient mode to conserving battery life (processor allowed to sleep). If your app
 * can wait 60 seconds for display updates, you can disregard the Alarm code and simply use
 * onUpdateAmbient() to save even more battery life.
 *
 * As always, you will still want to apply the performance guidelines outlined in the Watch Faces
 * documentation to your app.
 *
 * Finally, in ambient mode, this activity follows the same best practices outlined in the Watch
 * Faces API documentation: keeping most pixels black, avoiding large blocks of white pixels, using
 * only black and white, disabling anti-aliasing, etc.
 */

@AndroidEntryPoint
class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider, CoroutineScope by MainScope()  {

    private lateinit var binding: ActivityMainBinding

    //Health Service
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val viewModel: MainViewModel by viewModels()

    //Google Sheet object
    lateinit var service: Sheets
    private val TAG = "SheetDebug"

    /**
     * Ambient mode controller attached to this display. Used by Activity to see if it is in ambient
     * mode.
     */
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    /**
     * Since the coroutine-based update (used in active mode) can't wake up the processor when the
     * device is in ambient mode and undocked, we use an Alarm to cover ambient mode updates when we
     * need them more frequently than every minute. Remember, if getting updates once a minute in
     * ambient mode is enough, you can do away with the Alarm code and just rely on the
     * onUpdateAmbient() callback.
     */
    private lateinit var ambientUpdateAlarmManager: AlarmManager
    private lateinit var ambientUpdatePendingIntent: PendingIntent
    private lateinit var ambientUpdateBroadcastReceiver: BroadcastReceiver

    private val dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)


    //Initialize data
    @Volatile
    private var drawCount = 0
    private var heartRate = "60"
    private var batteryLevel = 100
    private var sendDate_Time = ""

    /**
     * The [Job] associated with the updates performed while in active mode.
     */
    private var activeUpdateJob: Job = Job().apply { complete() }

    //BLE View Model
    private val bleviewModel: BleMainViewModel by viewModels()
    private var bleDeviceName = "No Device"

    //Android Device ID (Unique ID)
    private var watchId = ""

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        //Binding to the Layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Request the permission for body sensor permission
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                when (result) {
                    true -> {
                        Log.i(TAG, "Body sensors permission granted")
                        // Only measure while the activity is at least in STARTED state.
                        // MeasureClient provides frequent updates, which requires increasing the
                        // sampling rate of device sensors, so we must be careful not to remain
                        // registered any longer than necessary.
                        lifecycleScope.launchWhenStarted {
                            viewModel.measureHeartRate()
                        }
                    }
                    false -> Log.i(TAG, "Body sensors permission not granted")
                }
            }

        //Show the heart rate status
        /*lifecycleScope.launchWhenStarted {
            viewModel.heartRateAvailable.collect {
                binding.statusText.text = getString(R.string.measure_status, it)
            }
        }*/

        //Show the heart rate value
        lifecycleScope.launchWhenStarted {
            viewModel.heartRateBpm.collect {
               heartRate = String.format("%.1f", it)
            }
        }

        //Request for Google Sheet API in permission
        requestSignIn()


        lifecycleScope.launch {
            //Request the BLE permission
            ensureBlePermissionOrFinishActivity()
            val result = requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            if (result is PermissionRequestResult.Denied) {
                finish(); return@launch
            }

            @Suppress("MissingPermission")
            binding.sendButton.onClick {

                //Get Android ID
                watchId = getDeviceID(this@MainActivity)

                //Get the BLE device name
                bleDeviceName = bleviewModel.logNameAndAppearance()
                Log.d("BLE Device", bleDeviceName)
                if(bleDeviceName.isNotEmpty()){
                    binding.bleName.text = "BLE Device: " + bleDeviceName
                }


                //Append data instance to Google Sheet
                appendToSpreadsheet(service)
            }

            @Suppress("MissingPermission")
            launch { BleScanHeater.heatUpWhileStarted(lifecycle) }
        }

        ambientController = AmbientModeSupport.attach(this)
        ambientUpdateAlarmManager = getSystemService()!!


        /*
         * Create a PendingIntent which we'll give to the AlarmManager to send ambient mode updates
         * on an interval which we've define.
         */
        val ambientUpdateIntent = Intent(AMBIENT_UPDATE_ACTION)

        /*
         * Retrieves a PendingIntent that will perform a broadcast. You could also use getActivity()
         * to retrieve a PendingIntent that will start a new activity, but be aware that actually
         * triggers onNewIntent() which causes lifecycle changes (onPause() and onResume()) which
         * might trigger code to be re-executed more often than you want.
         *
         * If you do end up using getActivity(), also make sure you have set activity launchMode to
         * singleInstance in the manifest.
         *
         * Otherwise, it is easy for the AlarmManager launch Intent to open a new activity
         * every time the Alarm is triggered rather than reusing this Activity.
         */
        ambientUpdatePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            ambientUpdateIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /*
         * An anonymous broadcast receiver which will receive ambient update requests and trigger
         * display refresh.
         */
        ambientUpdateBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                refreshDisplayAndSetNextUpdate()
            }


        }


    }

    // Get Android Device ID
    private fun getDeviceID(context: Context) : String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    //Request permission for Bluetooth BLE scanner
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


    //For Google Sheet
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener { account ->
                        val scopes = listOf(SheetsScopes.SPREADSHEETS)
                        val credential = GoogleAccountCredential.usingOAuth2(this.applicationContext, scopes)
                        credential.selectedAccount = account.account
                        val jsonFactory = JacksonFactory.getDefaultInstance()
                        // GoogleNetHttpTransport.newTrustedTransport()
                        val httpTransport = NetHttpTransport()
                        service = Sheets.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(getString(R.string.app_name))
                            .build()
//                        appendToSpreadsheet(service)
                    }
                    .addOnFailureListener { e ->
                        Log.e("error",e.toString())
                    }
            }
        }
    }

    //Request permission to access Google Sheet
    private fun requestSignIn() {
       GoogleSignIn.getLastSignedInAccount(this)?.also { account ->
            Log.d(TAG ,"account=${account.displayName}")
        }
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
             //.requestEmail()
            // .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .build()
        val client = GoogleSignIn.getClient(this, signInOptions)
        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }

    //Append data instance to Google Sheet
    private fun appendToSpreadsheet(service: Sheets) {

        //Prepare Google Sheet attributes
        val currentInstant = Instant.now(clock)
        //currentInstant.toEpochMilli()

        val spreadsheetId = "1BIp...QFpc"
        val sheetName = "Sheet1"
        val rows = listOf(listOf(watchId, "test@test.com", bleDeviceName, sendDate_Time, heartRate, batteryLevel, "1", "0"))
        val valueInputOption = "RAW"
        //val body = ValueRange().setValues(rows)
        val range = "'$sheetName'!A1"


        launch(Dispatchers.Default) {
            var result: AppendValuesResponse? = null
            val body = ValueRange().setValues(rows)
            result = service.spreadsheets().values().append(spreadsheetId, range, body)
                .setValueInputOption(valueInputOption)
                .execute()
            // Prints the spreadsheet with appended values.
            System.out.printf("%d cells appended.", result.updates.updatedCells)
            Log.d("TAG:", "Number of cells: "+result.updates.updatedCells.toString())
        }
    }

    //For Google Sheet
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    //For health service
    override fun onStart() {
        super.onStart()
        permissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
    }

    public override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        val filter = IntentFilter(AMBIENT_UPDATE_ACTION)
        registerReceiver(ambientUpdateBroadcastReceiver, filter)
        refreshDisplayAndSetNextUpdate()
    }

    public override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        unregisterReceiver(ambientUpdateBroadcastReceiver)
        activeUpdateJob.cancel()
        ambientUpdateAlarmManager.cancel(ambientUpdatePendingIntent)
    }

    /**
     * Loads data/updates screen (via method), but most importantly, sets up the next refresh
     * (active mode = coroutines and ambient mode = Alarm).
     */
    private fun refreshDisplayAndSetNextUpdate() {
        loadDataAndUpdateScreen()
        val instant = Instant.now(clock)
        if (ambientController.isAmbient) {
            val triggerTime = instant.getNextInstantWithInterval(AMBIENT_INTERVAL)
            ambientUpdateAlarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime.toEpochMilli(),
                ambientUpdatePendingIntent
            )
        } else {
            val delay = instant.getDelayToNextInstantWithInterval(ACTIVE_INTERVAL)
            activeUpdateJob.cancel()
            activeUpdateJob = lifecycleScope.launch {
                withContext(activeDispatcher) {
                    // Delay on the active dispatcher for testability
                    delay(delay.toMillis())
                }

                refreshDisplayAndSetNextUpdate()
            }
        }
    }

    /**
     * Returns the delay from this [Instant] to the next one that is aligned with the given
     * [interval].
     */
    private fun Instant.getDelayToNextInstantWithInterval(interval: Duration): Duration =
        Duration.ofMillis(interval.toMillis() - toEpochMilli() % interval.toMillis())

    /**
     * Returns the next [Instant] that is aligned with the given [interval].
     */
    private fun Instant.getNextInstantWithInterval(interval: Duration): Instant =
        plus(getDelayToNextInstantWithInterval(interval))

    /**
     * Updates display based on Ambient state. If you need to pull data, you should do it here.
     */
    private fun loadDataAndUpdateScreen() {
        drawCount += 1

        val currentInstant = Instant.now(clock)

        Log.d(
            TAG,
            "loadDataAndUpdateScreen(): " +
                "${currentInstant.toEpochMilli()} (${ambientController.isAmbient})"
        )

        val currentTime = LocalTime.now(clock)
            sendDate_Time = LocalDateTime.now(clock).toString()


        //Get Battery Level
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this.registerReceiver(null, ifilter)
        }
        val batteryPct: Float? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
        batteryLevel = batteryPct!!.toInt()


        binding.time.text = dateFormat.format(currentTime)
        binding.timeStamp.text = getString(R.string.timestamp_label, currentInstant.toEpochMilli())
        binding.state.text = getString(
            if (ambientController.isAmbient) {
                R.string.mode_ambient_label
            } else {
                R.string.mode_active_label
            }
        )

        /*binding.updateRate.text = getString(
            R.string.update_rate_label,
            if (ambientController.isAmbient) {
                AMBIENT_INTERVAL.seconds
            } else {
                ACTIVE_INTERVAL.seconds
            }
        )*/


        binding.drawCount.text = getString(R.string.draw_count_label, drawCount)
        binding.heartRate.text = "Heart rate: " + heartRate
        binding.batteryLevel.text = "Battery level: "+batteryLevel.toString()

    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {

        /**
         * If the display is low-bit in ambient mode. i.e. it requires anti-aliased fonts.
         */
        private var isLowBitAmbient = false

        /**
         * If the display requires burn-in protection in ambient mode, rendered pixels need to be
         * intermittently offset to avoid screen burn-in.
         */
        private var doBurnInProtection = false

        /**
         * Prepares the UI for ambient mode.
         */
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
            isLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false)
            doBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false)

            // Cancel any active updates
            activeUpdateJob.cancel()

            /*
             * Following best practices outlined in WatchFaces API (keeping most pixels black,
             * avoiding large blocks of white pixels, using only black and white, and disabling
             * anti-aliasing, etc.)
             */
            binding.state.setTextColor(Color.WHITE)
            binding.bleName.setTextColor(Color.WHITE)
            binding.drawCount.setTextColor(Color.WHITE)
            binding.heartRate.setTextColor(Color.WHITE)
            binding.batteryLevel.setTextColor(Color.WHITE)
            if (isLowBitAmbient) {
                binding.time.paint.isAntiAlias = false
                binding.timeStamp.paint.isAntiAlias = false
                binding.state.paint.isAntiAlias = false
                binding.bleName.paint.isAntiAlias = false
                binding.drawCount.paint.isAntiAlias = false
                binding.heartRate.paint.isAntiAlias = false
                binding.batteryLevel.paint.isAntiAlias = false
            }
            refreshDisplayAndSetNextUpdate()
        }

        /**
         * Updates the display in ambient mode on the standard interval. Since we're using a custom
         * refresh cycle, this method does NOT update the data in the display. Rather, this method
         * simply updates the positioning of the data in the screen to avoid burn-in, if the display
         * requires it.
         */
        override fun onUpdateAmbient() {
            super.onUpdateAmbient()

            /*
             * If the screen requires burn-in protection, views must be shifted around periodically
             * in ambient mode. To ensure that content isn't shifted off the screen, avoid placing
             * content within 10 pixels of the edge of the screen.
             *
             * Since we're potentially applying negative padding, we have ensured
             * that the containing view is sufficiently padded (see res/layout/activity_main.xml).
             *
             * Activities should also avoid solid white areas to prevent pixel burn-in. Both of
             * these requirements only apply in ambient mode, and only when this property is set
             * to true.
             */
            if (doBurnInProtection) {
                binding.container.translationX =
                    Random.nextInt(-BURN_IN_OFFSET_PX, BURN_IN_OFFSET_PX + 1).toFloat()
                binding.container.translationY =
                    Random.nextInt(-BURN_IN_OFFSET_PX, BURN_IN_OFFSET_PX + 1).toFloat()
            }
        }

        /**
         * Restores the UI to active (non-ambient) mode.
         */
        override fun onExitAmbient() {
            super.onExitAmbient()

            /* Clears out Alarms since they are only used in ambient mode. */
            ambientUpdateAlarmManager.cancel(ambientUpdatePendingIntent)
            binding.state.setTextColor(Color.GREEN)
            binding.bleName.setTextColor(Color.GREEN)
            binding.drawCount.setTextColor(Color.GREEN)
            binding.heartRate.setTextColor(Color.GREEN)
            binding.batteryLevel.setTextColor(Color.GREEN)
            if (isLowBitAmbient) {
                binding.time.paint.isAntiAlias = true
                binding.timeStamp.paint.isAntiAlias = true
                binding.state.paint.isAntiAlias = true
                binding.bleName.paint.isAntiAlias = true
                binding.drawCount.paint.isAntiAlias = true
                binding.heartRate.paint.isAntiAlias = true
                binding.batteryLevel.paint.isAntiAlias = true
            }

            /* Reset any random offset applied for burn-in protection. */
            if (doBurnInProtection) {
                binding.container.translationX = 0f
                binding.container.translationY = 0f
            }
            refreshDisplayAndSetNextUpdate()
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        /**
         * Duration between updates while in active mode.
         */
        private val ACTIVE_INTERVAL = Duration.ofSeconds(1)

        /**
         * Duration between updates while in ambient mode.
         */
        private val AMBIENT_INTERVAL = Duration.ofSeconds(10)

        /**
         * Action for updating the display in ambient mode, per our custom refresh cycle.
         */
        const val AMBIENT_UPDATE_ACTION =
            "com.example.android.wearable.wear.alwayson.action.AMBIENT_UPDATE"

        /**
         * Number of pixels to offset the content rendered in the display to prevent screen burn-in.
         */
        private const val BURN_IN_OFFSET_PX = 10


        private const val REQUEST_SIGN_IN = 1
    }
}

/**
 * The [Clock] driving the time information. Overridable only for testing.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal var clock: Clock = Clock.systemDefaultZone()

/**
 * The dispatcher used for delaying in active mode. Overridable only for testing.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal var activeDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
