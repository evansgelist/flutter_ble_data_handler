package com.example.ble_data_example.services

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import com.itransition.ble.peripheral.services.Service
import com.welie.blessed.*
import timber.log.Timber
import timber.log.Timber.Forest.plant
import java.util.*

@SuppressLint("MissingPermission")
internal class BluetoothServer(
    deviceName: String,
    context: Context
) {
    lateinit var peripheralManager: BluetoothPeripheralManager
    lateinit var dataTransferService: DataTransferService
    private val serviceImplementations = HashMap<BluetoothGattService, Service>()
    private val peripheralManagerCallback: BluetoothPeripheralManagerCallback =
        object : BluetoothPeripheralManagerCallback() {
            override fun onServiceAdded(status: GattStatus, service: BluetoothGattService) {}
            override fun onCharacteristicRead(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic
            ) {
                val serviceImplementation = serviceImplementations[characteristic.service]
                serviceImplementation?.onCharacteristicRead(central, characteristic)
            }

            override fun onCharacteristicWrite(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ): GattStatus {
                val serviceImplementation = serviceImplementations[characteristic.service]
                return if (serviceImplementation != null) {
                    serviceImplementation.onCharacteristicWrite(central, characteristic, value)!!
                } else GattStatus.REQUEST_NOT_SUPPORTED
            }

            override fun onDescriptorRead(
                central: BluetoothCentral,
                descriptor: BluetoothGattDescriptor
            ) {
                val characteristic = Objects.requireNonNull(
                    descriptor.characteristic,
                    "Descriptor has no Characteristic"
                )
                val service =
                    Objects.requireNonNull(characteristic.service, "Characteristic has no Service")
                val serviceImplementation = serviceImplementations[service]
                serviceImplementation?.onDescriptorRead(central, descriptor)
            }

            override fun onDescriptorWrite(
                central: BluetoothCentral,
                descriptor: BluetoothGattDescriptor,
                value: ByteArray
            ): GattStatus {
                val characteristic = Objects.requireNonNull(
                    descriptor.characteristic,
                    "Descriptor has no Characteristic"
                )
                val service =
                    Objects.requireNonNull(characteristic.service, "Characteristic has no Service")
                val serviceImplementation = serviceImplementations[service]
                return if (serviceImplementation != null) {
                    serviceImplementation.onDescriptorWrite(central, descriptor, value)!!
                } else GattStatus.REQUEST_NOT_SUPPORTED
            }

            override fun onNotifyingEnabled(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic
            ) {
                val serviceImplementation = serviceImplementations[characteristic.service]
                serviceImplementation?.onNotifyingEnabled(central, characteristic)
            }

            override fun onNotifyingDisabled(
                central: BluetoothCentral,
                characteristic: BluetoothGattCharacteristic
            ) {
                val serviceImplementation = serviceImplementations[characteristic.service]
                serviceImplementation?.onNotifyingDisabled(central, characteristic)
            }

            override fun onNotificationSent(
                central: BluetoothCentral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                val serviceImplementation = serviceImplementations[characteristic.service]
                serviceImplementation?.onNotificationSent(central, value, characteristic, status)
            }

            override fun onCentralConnected(central: BluetoothCentral) {
                for (serviceImplementation in serviceImplementations.values) {
                    serviceImplementation.onCentralConnected(central)
                }
            }

            override fun onCentralDisconnected(central: BluetoothCentral) {
                for (serviceImplementation in serviceImplementations.values) {
                    serviceImplementation.onCentralDisconnected(central)
                }
            }

            override fun onAdvertisingStarted(settingsInEffect: AdvertiseSettings) {}
            override fun onAdvertiseFailure(advertiseError: AdvertiseError) {}
            override fun onAdvertisingStopped() {}
        }

    fun startAdvertising(serviceUUID: UUID?) {
        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val advertiseData = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .build()
        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()
        peripheralManager.startAdvertising(advertiseSettings, scanResponse, advertiseData)
    }

    fun stopAdvertising() {
        peripheralManager.stopAdvertising()
        Timber.d("Stop advertising.")
    }

    private fun setupServices() {
        for (service in serviceImplementations.keys) {
            peripheralManager.add(service)
        }
    }

    companion object {
        private var instance: BluetoothServer? = null

        @Synchronized
        fun getInstance(
            deviceName: String,
            context: Context
        ): BluetoothServer? {
            if (instance == null) {
                instance = BluetoothServer(deviceName, context.applicationContext)
            }
            return instance
        }
    }


    init {
        run {
            plant(Timber.DebugTree())
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null || bluetoothManager == null) {
                Timber.e("bluetooth not supported")
                return@run
            }
            if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
                Timber.e("not supporting advertising")
                return@run
            }

            // Set the adapter name as this is used when advertising
            bluetoothAdapter.name = deviceName
            peripheralManager =
                BluetoothPeripheralManager(context, bluetoothManager, peripheralManagerCallback)
            peripheralManager.removeAllServices()

            dataTransferService = DataTransferService(peripheralManager)
            serviceImplementations[dataTransferService.service] = dataTransferService
            setupServices()
            startAdvertising(dataTransferService.service.uuid)
        }

    }
}