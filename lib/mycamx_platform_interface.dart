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

  static set instance(MycamxPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<int?> startCamera() {
    throw UnimplementedError('startCamera() has not been implemented.');
  }
  Future<String?> takePhoto() {
    throw UnimplementedError('takePhoto() has not been implemented.');
  }
}
