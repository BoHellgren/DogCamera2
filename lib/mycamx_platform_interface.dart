import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import "dart:typed_data";

import 'mycamx_method_channel.dart';

abstract class MycamxPlatform extends PlatformInterface {
  /// Constructs a MycamxPlatform.
  MycamxPlatform() : super(token: _token);

  static final Object _token = Object();

  static MycamxPlatform _instance = MethodChannelMycamx();

  /// The default instance of [MycamxPlatform] to use.
  ///
  /// Defaults to [MethodChannelMycamx].
  static MycamxPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [MycamxPlatform] when
  /// they register themselves.
  static set instance(MycamxPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
  Future<int?> getBatteryLevel() {
    throw UnimplementedError('batteryLevel() has not been implemented.');
  }
  Future<int?> startCamera() {
    throw UnimplementedError('startCamera() has not been implemented.');
  }
  Future<String?> takePhoto() {
    throw UnimplementedError('takePhoto() has not been implemented.');
  }
  Future<Uint8List?> takePhoto2() {
    throw UnimplementedError('takePhoto2() has not been implemented.');
  }
  Future<int?> createTexture() {
    throw UnimplementedError('createTexture() has not been implemented.');
  }
}
