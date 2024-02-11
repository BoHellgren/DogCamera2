import "dart:typed_data";
import 'mycamx_platform_interface.dart';

class Mycamx {
  Future<int?> startCamera() {
    return MycamxPlatform.instance.startCamera();
  }
  Future<String?> takePhoto() {
    return MycamxPlatform.instance.takePhoto();
  }
}
