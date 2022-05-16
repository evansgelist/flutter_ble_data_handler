package com.example.ble_data_example.services

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.welie.blessed.BluetoothCentral
import com.welie.blessed.BluetoothPeripheralManager
import com.welie.blessed.GattStatus
import timber.log.Timber
import java.util.*

class DataTransferService(
    peripheralManager: BluetoothPeripheralManager
) :
    BaseService(peripheralManager) {
    override val service =
        BluetoothGattService(DTS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
    private val notifyCharacteristic = BluetoothGattCharacteristic(
        INDICATION_CHARACTERISTIC_UUID,
        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
        BluetoothGattCharacteristic.PERMISSION_READ
    )
    private val writeCharacteristic = BluetoothGattCharacteristic(
        WRITE_CHARACTERISTIC_UUID,
        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
        BluetoothGattCharacteristic.PERMISSION_WRITE,

        )

    fun notifyBytes(bytes: ByteArray) {
        notifyCharacteristicChanged(bytes, notifyCharacteristic)
        Timber.i("bytes notified: $bytes")
    }

    override fun onCharacteristicWrite(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray?
    ): GattStatus {
        return GattStatus.SUCCESS
    }

    //     private val handler = Handler(Looper.getMainLooper())
//    private val notifyRunnable = Runnable { notifyHeartRate() }
//    private var currentHR = 80
    override fun onCentralDisconnected(central: BluetoothCentral) {
//        if (noCentralsConnected()) {
//            stopNotifying()
//        }
    }

    override fun onNotifyingEnabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
//        if (characteristic.uuid == HEARTRATE_MEASUREMENT_CHARACTERISTIC_UUID) {
//            notifyHeartRate()
//        }
    }

    override fun onNotifyingDisabled(
        central: BluetoothCentral,
        characteristic: BluetoothGattCharacteristic
    ) {
//        if (characteristic.uuid == HEARTRATE_MEASUREMENT_CHARACTERISTIC_UUID) {
//            stopNotifying()
//        }
    }

//    private fun notifyHr() {
//        currentHR += (Math.random() * 10 - 5).toInt()
//        if (currentHR > 120) currentHR = 100
//        val value = byteArrayOf(0x00, currentHR.toByte())
//        notifyCharacteristicChanged(value, notifyCharacteristic)
//        handler.postDelayed(notifyRunnable, 1000)
//        Timber.i("new hr: %d", currentHR)
//    }

//    private fun stopNotifying() {
//        handler.removeCallbacks(notifyRunnable)
//    }


    override val serviceName: String
        get() = "Data Transfer Service"

    companion object {
        private val DTS_SERVICE_UUID = UUID.fromString("11111111-0000-0000-0000-000000000000")
        private val INDICATION_CHARACTERISTIC_UUID =
            UUID.fromString("11111111-0000-0000-0000-000000000000")
        private val WRITE_CHARACTERISTIC_UUID =
            UUID.fromString("11111112-0000-0000-0000-000000000000")
    }

    init {
        service.addCharacteristic(writeCharacteristic)
        service.addCharacteristic(notifyCharacteristic)
        notifyCharacteristic.value = byteArrayOf(0x00, 0x00, 0x00)
        notifyCharacteristic.addDescriptor(cccDescriptor)
    }
}