import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_ble_data_handler/handler.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:provider/provider.dart';

import 'ble_device_data.dart';
import 'ble_handling.dart';
import 'widgets.dart';

void main() {
  runApp(FlutterBlueApp());
}

class FlutterBlueApp extends StatefulWidget {
  @override
  _FlutterBlueAppState createState() => _FlutterBlueAppState();
}

class _FlutterBlueAppState extends State<FlutterBlueApp> {
  @override
  Widget build(BuildContext context) {
    //permissions();

    FlutterBluePlus.instance.setLogLevel(LogLevel.error);

    return MultiProvider(
      providers: [
        ChangeNotifierProvider.value(
          value: BleDeviceProvider(),
        ),
        Provider<BleHandling>(
          create: (context) => BleHandling(),
        ),
      ],
      child: MaterialApp(
        color: Colors.lightBlue,
        home: Scaffold(
          appBar: AppBar(),
          body: Stack(
            children: <Widget>[
              Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: <Widget>[
                  Flexible(
                    flex: 3,
                    child: SearchExpansionTile(
                      onSearchPressed: onSearchPressed,
                    ),
                  ),
                  Divider(),
                  Flexible(
                    flex: 1,
                    child: Center(
                      child: ShowData(),
                    ),
                  )
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  onSearchPressed(BuildContext context) {
    print("Just a callback function for search press handling");
  }

// void permissions() async {
//   PermissionStatus permission = await PermissionHandler()
//       .checkPermissionStatus(PermissionGroup.storage);
//   if (permission != PermissionStatus.granted) {
//     print("Storage permission is not granted.");
//     Map<PermissionGroup, PermissionStatus> permissionsMap =
//         await PermissionHandler()
//             .requestPermissions([PermissionGroup.storage]);
//     if (permissionsMap[PermissionGroup.storage] != PermissionStatus.granted) {
//       print("Unable to grant permission: ${PermissionGroup.storage}");
//     }
//   }
// }
}

class ShowData extends StatefulWidget {
  @override
  _ShowDataState createState() => _ShowDataState();
}

class _ShowDataState extends State<ShowData> {
  @override
  Widget build(BuildContext context) {
    final bleHandling = Provider.of<BleHandling>(context);
    return _buildStreamText(context, bleHandling.allDataOnStream);
  }

  Widget _buildStreamText(BuildContext context, Stream<String> dataStream) {
    return Container(
      child: Column(
        children: <Widget>[
          Container(
            child: StreamBuilder(
              stream: UpdateHandler.instance.isSending,
              initialData: false,
              builder: (c1, snapshot1) {
                if (!snapshot1.hasData) {
                  return Container();
                } else {
                  if (snapshot1.data) {
                    return StreamBuilder<int>(
                      stream: UpdateHandler.instance.chunkCount,
                      initialData: 0,
                      builder: (c2, snapshot2) {
                        if (!snapshot2.hasData) {
                          return Container();
                        } else {
                          var totalChunks =
                              UpdateHandler.instance.totalChunkCount;
                          var chunks = snapshot2.data;

                          if (totalChunks < 1) {
                            return Container();
                          } else {
                            return Text(
                                "current chunk: $chunks of total cunks: $totalChunks");
                          }
                        }
                      },
                    );
                  } else {
                    return Container();
                  }
                }
              },
            ),
          ),
          Container(
            child: StreamBuilder(
              stream: UpdateHandler.instance.dumpedValue,
              initialData: "",
              builder: (c, snapshot) {
                if (snapshot.hasData) {
                  return Text("${snapshot.data}");
                } else {
                  return Container();
                }
              },
            ),
          ),
        ],
      ),
    );
  }
}
