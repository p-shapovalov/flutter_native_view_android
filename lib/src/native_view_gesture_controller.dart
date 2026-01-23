import 'package:flutter/services.dart';

/// Controller for managing native view gesture behavior.
class NativeViewGestureController {
  NativeViewGestureController._();

  static const MethodChannel _channel = MethodChannel(
    'plugins.flutter.dev/native_view_flutter_activity/gestures',
  );

  /// Enables or disables touch event dispatching to the native view.
  static Future<void> setGesturesEnabled(bool enabled) {
    return _channel.invokeMethod<void>(
      'setGesturesEnabled',
      <String, dynamic>{'enabled': enabled},
    );
  }

  /// Returns whether gestures are currently enabled.
  static Future<bool> isGesturesEnabled() async {
    final bool? result = await _channel.invokeMethod<bool>('isGesturesEnabled');
    return result ?? true;
  }

  /// Claims a pointer for exclusive Flutter handling.
  ///
  /// Automatically released when the touch sequence ends.
  static Future<void> claimPointer(int pointerId) {
    return _channel.invokeMethod<void>(
      'claimPointer',
      <String, dynamic>{'pointerId': pointerId},
    );
  }

  /// Releases a previously claimed pointer.
  ///
  /// After releasing, touch events for this pointer will be forwarded
  /// to the native view again.
  ///
  /// Note: Pointers are automatically released when the touch sequence ends,
  /// so calling this is only necessary if you want to release a pointer
  /// mid-gesture.
  static Future<void> releasePointer(int pointerId) {
    return _channel.invokeMethod<void>(
      'releasePointer',
      <String, dynamic>{'pointerId': pointerId},
    );
  }
}
