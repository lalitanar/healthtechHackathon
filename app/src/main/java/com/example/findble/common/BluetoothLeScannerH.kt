package com.example.findble.common

import android.bluetooth.le.ScanCallback

/*
@RequiresApi(21)
@RequiresPermission(
    allOf = [
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    ]
)*/
/*
expect fun bluetoothLeScanFlow(
    filters: List<ScanFilter>?,
    settings: ScanSettings
): Flow<ScanResult>

@RequiresApi(21)
@RequiresPermission(
    allOf = [
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    ]
)
expect fun BluetoothLeScanner.scanFlow(
    filters: List<ScanFilter>?,
    settings: ScanSettings
): Flow<ScanResult>
*/

/**
 * @property errorCode Possible values: [ScanCallback.SCAN_FAILED_ALREADY_STARTED],
 * [ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED],
 * [ScanCallback.SCAN_FAILED_INTERNAL_ERROR],
 * [ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED],
 * [ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES],
 * [ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY]. There may be some undocumented additional errorCodes.
 */
class ScanFailedException(val errorCode: Int) : Exception("Scan failed. errorCode = $errorCode")
