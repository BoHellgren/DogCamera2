import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mycamx/mycamx.dart';
import 'package:permission_handler/permission_handler.dart';
import 'rectpainter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});
  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _mycamxPlugin = Mycamx();
  int? textureId;
  EventChannel eventChannel = EventChannel('dogChannel');
  // Uint8List? _savedPngBytes;
  final _messangerKey = GlobalKey<ScaffoldMessengerState>();

  initState() {
    super.initState();
    initCamera();
  }

  void initCamera() async {
    Permission cameraPermission = Permission.camera;
    if (cameraPermission.status != PermissionStatus.granted) await cameraPermission.request();
    int id = 0;
    try {
      id = await _mycamxPlugin.startCamera() ?? 0;
    } catch (e) {
      print(' _mycamxPlugin.startCamera exception: $e');
    }
    setState(() {
      textureId = id;
    });
  }

  @override
  Widget build(BuildContext context) {
    double width = MediaQuery.of(context).size.width;
    return MaterialApp(
      scaffoldMessengerKey: _messangerKey,
      home: Scaffold(
        appBar: AppBar(
          title: Center(child: const Text('Dog Camera 2')),
        ),
        body: OverflowBox(
          maxWidth: double.infinity,
          child: AspectRatio(
              aspectRatio: 3/4,
            child: Stack(children: [
              if (textureId != null) Texture(textureId: textureId!),

              /*   _savedPngBytes == null
                  ? Texture(textureId: textureId!)
                  : Image.memory(_savedPngBytes!), */

              StreamBuilder(
                  stream: eventChannel.receiveBroadcastStream(),
                  builder: (BuildContext context, AsyncSnapshot<dynamic> snapshot) {
                    if (snapshot.hasData) {
                      return CustomPaint(
                        painter: RectPainter(snapshot.data),
                        child: Container(),
                      );
                    } else {
                      return CircularProgressIndicator();
                    }
                  }),
            ]),
          ),
        ),
        floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
        floatingActionButton: Padding(
          padding: const EdgeInsets.all(8.0),
          child: FloatingActionButton(
            backgroundColor: Colors.white,
            splashColor: Colors.grey,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(30)),
            onPressed: () async {
              try {
                /* takePhoto2
                var result = await _mycamxPlugin.takePhoto2();
                ByteBuffer byteBuffer = result!.buffer;
                imageLib.Image img = imageLib.Image.fromBytes(
                    width: 480,
                    height: 640,
                    bytes: byteBuffer,
                    numChannels: 4,
                    bytesOffset: 7,
                    rowStride: 1920,
                    order: imageLib.ChannelOrder.rgba);
                _savedPngBytes = imageLib.encodePng(img);
                setState(() {});
                */
                String? photoName = await _mycamxPlugin.takePhoto();
                SnackBar snackBar = SnackBar(content: Text('Saved photo: $photoName'));
                _messangerKey.currentState!.showSnackBar(snackBar);
              } catch (e) {
                print('_mycamxPlugin.takePhoto() exception: $e');
              }
            },
          ),
        ),
      ),
    );
  }
}
