import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import "dart:typed_data";

import 'mycamx_platform_interface.dart';

/// An implementation of [MycamxPlatform] that uses method channels.
class MethodChannelMycamx extends MycamxPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('mycamx');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
  @override
  Future<int?> getBatteryLevel() async {
    final level = await methodChannel.invokeMethod<int>('getBatteryLevel');
    return level;
  }
  @override
  Future<int?> startCamera() async {
    final level = await methodChannel.invokeMethod<int>('startCamera');
    return level;
  }
  @override
  Future<String?> takePhoto() async {
    final photoName = await methodChannel.invokeMethod<String>('takePhoto');
    return photoName;
  }
  @override
  Future<Uint8List?> takePhoto2() async {
    final level = await methodChannel.invokeMethod<Uint8List>('takePhoto2');
    return level;
  }
  @override
  Future<int?> createTexture() async {
    final level = await methodChannel.invokeMethod<int>('createTexture');
    return level;
  }
}
