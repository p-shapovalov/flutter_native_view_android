package com.example.flutter_native_view_android_example;

import io.flutter.plugins.nativeview.NativeViewFlutterActivity;

/**
 * Main activity that hosts native views below a transparent Flutter view.
 */
public class MainActivity extends NativeViewFlutterActivity {

    @Override
    protected void onRegisterNativeViews() {
        // Register factories for each native view type
        registerNativeViewFactory("red_view", () -> new ColoredScrollView(0xFFE53935));   // Red
        registerNativeViewFactory("green_view", () -> new ColoredScrollView(0xFF43A047)); // Green
        registerNativeViewFactory("blue_view", () -> new ColoredScrollView(0xFF1E88E5));  // Blue
    }
}
