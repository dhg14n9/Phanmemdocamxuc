package com.example.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

@SuppressLint("MissingPermission")
class BleConnectionManager(private val context: Context, private val viewModel: AuraViewModel) {

    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null

    // Target MAC address of Xiaomi Band 8 (user should configure this)
    private var targetDeviceMacAddress: String? = null

    private val _connectionState = MutableStateFlow("Disconnected")
    val connectionState: StateFlow<String> = _connectionState

    // Standard Heart Rate Service & Characteristic UUIDs (as reference, though Xiaomi uses custom protocols)
    private val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
    private val HEART_RATE_MEASUREMENT_CHAR_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                // Example filter by name or MAC
                val deviceName = device.name ?: ""
                if (deviceName.contains("Xiaomi", ignoreCase = true) || deviceName.contains("Band", ignoreCase = true) || device.address == targetDeviceMacAddress) {
                    Log.d("BleConnectionManager", "Found device: ${device.name} [${device.address}]")
                    bleScanner?.stopScan(this)
                    connectToDevice(device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleConnectionManager", "Scan failed with error: $errorCode")
            _connectionState.value = "Scan Failed"
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = "Connected"
                Log.d("BleConnectionManager", "Connected to GATT server.")
                // Attempt to discover services
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = "Disconnected"
                Log.d("BleConnectionManager", "Disconnected from GATT server.")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleConnectionManager", "Services discovered")
                // Xiaomi Band 8 requires an authentication handshake here using Zepp protocol.
                // Assuming auth is handled or we are reading standard HRS:
                val service = gatt.getService(HEART_RATE_SERVICE_UUID)
                val characteristic = service?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
                
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    // Write to Client Characteristic Configuration descriptor (CCCD) to enable notifications
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                } else {
                    Log.d("BleConnectionManager", "Heart Rate Characteristic not found. Note: Xiaomi Band uses encrypted auth.")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID) {
                val flag = characteristic.properties
                val format = if ((flag and 0x01) != 0) BluetoothGattCharacteristic.FORMAT_UINT16 else BluetoothGattCharacteristic.FORMAT_UINT8
                val heartRate = characteristic.getIntValue(format, 1)
                
                Log.d("BleConnectionManager", "Heart Rate Broadcast: $heartRate")
                viewModel.updateBpm(heartRate)
                viewModel.addHealthData("Nhịp tim (BLE)", heartRate.toFloat(), "bpm")
            }
        }
    }

    fun startScan(targetMac: String? = null) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = "Bluetooth Disabled"
            return
        }
        targetDeviceMacAddress = targetMac
        _connectionState.value = "Scanning..."
        bleScanner?.startScan(scanCallback)
    }

    fun stopScan() {
        bleScanner?.stopScan(scanCallback)
        _connectionState.value = "Scan Stopped"
    }

    fun connectToDevice(device: BluetoothDevice) {
        _connectionState.value = "Connecting..."
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = "Disconnected"
    }
}
