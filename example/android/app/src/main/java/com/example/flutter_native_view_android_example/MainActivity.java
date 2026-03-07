package com.example.flutter_native_view_android_example;

import io.flutter.plugins.nativeview.NativeViewFlutterFragmentActivity;

/**
 * Main activity that hosts native views below a transparent Flutter view.
 */
public class MainActivity extends NativeViewFlutterFragmentActivity {

    @Override
    protected void onRegisterNativeViews() {
        // Register native views: red for main page, green for second page
        registerNativeViewFactory("red_view", () -> new ColoredScrollView(0xFFE53935));   // Red
        registerNativeViewFactory("green_view", () -> new ColoredScrollView(0xFF43A047)); // Green
    }
}
