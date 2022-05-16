package com.example.ble_data_example

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import com.example.ble_data_example.services.BluetoothServer
import io.flutter.embedding.android.FlutterActivity

class MainActivity : FlutterActivity() {

    private val isPeripheral = false

    private var bluetoothServer: BluetoothServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isPeripheral) {
            val requestCode = 1;
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            startActivityForResult(discoverableIntent, requestCode)
            bluetoothServer = BluetoothServer.getInstance("sharing flutter", context)
        }
    }
}