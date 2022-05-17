import UIKit
import Flutter
import CoreBluetooth

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate, CBPeripheralManagerDelegate {
    
    private var peripheralManager : CBPeripheralManager!
    private var isPeripheral = true //mode
    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        GeneratedPluginRegistrant.register(with: self)
        if (isPeripheral) {
            peripheralManager = CBPeripheralManager(delegate: self, queue: nil)
            addServices()
        }
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    func addServices() {
        
        // 1. Create instance of CBMutableCharcateristic
        let g = UUID(uuidString:"49535343-8841-43F4-A8D4-ECBE34729BB3") ?? UUID()
        let myCharacteristic1 = CBMutableCharacteristic(type: CBUUID(nsuuid: g),
                                                        properties: [.read, .write, .notify],
                                                        value: nil,
                                                        permissions: [.readable, .writeable])
        
        let g2 = UUID(uuidString:"49535343-1E4D-4BD9-BA61-23C647249616") ?? UUID()
        let myCharacteristic2 = CBMutableCharacteristic(type: CBUUID(nsuuid: g2),
                                                        properties: [.notify, .write, .read],
                                                        value: nil,
                                                        permissions: [.readable, .writeable])
        
        // 2. Create instance of CBMutableService
        let service = CBUUID(nsuuid: UUID(uuidString:"49535343-FE7D-4AE5-8FA9-9FAFD205E455") ?? UUID())
        let myService = CBMutableService(type: service, primary: true)
        
        // 3. Add characteristics to the service
        myService.characteristics = [myCharacteristic1]
        
        // 4. Add service to peripheralManager
        peripheralManager.add(myService)
        
        // 5. Start advertising
        peripheralManager.startAdvertising([CBAdvertisementDataLocalNameKey : "FileSharing",
                                         CBAdvertisementDataServiceUUIDsKey : [service]])
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveRead request: CBATTRequest) {
        print("=============== begin")
        print("Data getting Read!!!")
        print("=============== end")
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]) {
        print("=============== begin")
        print("Writing Data!!!")
        if let value = requests.first?.value {
            print(value.hexEncodedString())
        }
        peripheral.respond(to: requests.first!, withResult: .success)
        print("=============== end")
        
    }
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .unknown:
            print("Bluetooth Device is UNKNOWN")
        case .unsupported:
            print("Bluetooth Device is UNSUPPORTED")
        case .unauthorized:
            print("Bluetooth Device is UNAUTHORIZED")
        case .resetting:
            print("Bluetooth Device is RESETTING")
        case .poweredOff:
            print("Bluetooth Device is POWERED OFF")
        case .poweredOn:
            print("Bluetooth Device is POWERED ON")
            addServices()
        @unknown default:
            fatalError()
        }
    }
}


extension Data {
    struct HexEncodingOptions: OptionSet {
        let rawValue: Int
        static let upperCase = HexEncodingOptions(rawValue: 1 << 0)
    }
    
    func hexEncodedString(options: HexEncodingOptions = []) -> String {
        let format = options.contains(.upperCase) ? "%02hhX" : "%02hhx"
        return map { String(format: format, $0) }.joined()
    }
}
