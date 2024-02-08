import "dart:typed_data";
import 'mycamx_platform_interface.dart';

class Mycamx {
  Future<String?> getPlatformVersion() {
    return MycamxPlatform.instance.getPlatformVersion();
  }
  Future<int?> getBatteryLevel() {
    return MycamxPlatform.instance.getBatteryLevel();
  }
  Future<int?> startCamera() {
    return MycamxPlatform.instance.startCamera();
  }
  Future<String?> takePhoto() {
    return MycamxPlatform.instance.takePhoto();
  }
  Future<Uint8List?> takePhoto2() {
    return MycamxPlatform.instance.takePhoto2();
  }
  Future<int?> createTexture() {
    return MycamxPlatform.instance.createTexture();
  }
}
