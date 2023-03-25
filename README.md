# Intro Idea Hackathon Challenge by THG x MedINT

## Special Session 5:  Android & Lab-Kit (Find BLE)


## BleGattCoroutines[1]
**The content below is referred from the reference below [1]**
Functional Bluetooth GATT This library allows easy and safer usage of BluetoothGatt in Android. Instead of having callbacks to manage, you just need to call functions. It has also been tested successfully on Wear OS, with the sample included in this repository. It should work similarly on other Android variants such as Android TV.

It does so by taking advantage of the excellent coroutines feature in the Kotlin programming language that allows to write asynchronous code in a sequential/synchronous style, which means, without the callback hell, and without blocking any thread (which would waste memory and decrease performances).

This library makes it possible to have readable and debuggable code that interacts with Bluetooth Low Energy GATT (General Attribute), that is, the connection part of the Bluetooth Low Energy standard.


## References
[1] [BleGattCoroutines Repository](https://github.com/Beepiz/BleGattCoroutines)
