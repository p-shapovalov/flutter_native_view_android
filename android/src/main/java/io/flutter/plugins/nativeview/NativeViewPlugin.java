package io.flutter.plugins.nativeview;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;

/**
 * Plugin class for flutter_native_view_android.
 *
 * <p>This plugin provides base classes for hosting native Android views below a transparent Flutter
 * view. The actual functionality is provided by extending NativeView and NativeViewFlutterActivity
 * in your own code.
 */
public class NativeViewPlugin implements FlutterPlugin {
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    // No-op: This plugin provides base classes, not runtime functionality.
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    // No-op
  }
}
