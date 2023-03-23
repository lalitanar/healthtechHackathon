package com.example.findble.common

enum class BluetoothState {
    On, Off, TurningOn, TurningOff;
}

/*@RequiresPermission(Manifest.permission.BLUETOOTH)
expect fun isBluetoothEnabledFlow(): Flow<Boolean>

@RequiresPermission(Manifest.permission.BLUETOOTH)
expect fun bluetoothStateFlow(): Flow<BluetoothState>*/
