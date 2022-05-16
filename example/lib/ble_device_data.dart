import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:meta/meta.dart';

/// Device data class
class BleDeviceData {
  BluetoothDevice device;
  BleDeviceData({
    this.device,
  });
}

class BleDeviceProvider with ChangeNotifier {
  BleDeviceData _bleDeviceData = BleDeviceData();
  

  BleDeviceData get bleDeviceData => _bleDeviceData;

  // Register new bluetooth device
  void registerNewBleDevice({
      @required BluetoothDevice bleDevice,
    }) 
  {
    if (_bleDeviceData.device == null) {
      _bleDeviceData = BleDeviceData(device: bleDevice);

      notifyListeners();
    }
   
  }

  // Unregister the bluetooth device
  void unregisterNewBleDevice() {
    _bleDeviceData.device = null;
    notifyListeners();
  }


}
