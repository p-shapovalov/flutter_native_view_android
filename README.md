# flutter_native_view_android

Flutter plugin for hosting native Android views below a transparent Flutter view.

This approach renders native views *underneath* a transparent FlutterView rather than embedding them as platform views. This avoids platform view performance issues while enabling Flutter UI overlays on top of native content.

## Features

- Render native Android views below Flutter's transparent layer
- Automatic view lifecycle management tied to Flutter widget lifecycle
- Pointer claiming mechanism for gesture coordination between Flutter and native views
- Support for multiple native views with seamless switching

## Setup

### 1. Create your native view (Android)

Extend `NativeView` and implement `onCreateView()`:

```java
public class MyNativeView extends NativeView {

    @NonNull
    @Override
    protected View onCreateView() {
        Context context = getContext();
        // Create and return your native Android view
        TextView textView = new TextView(context);
        textView.setText("Hello from native!");
        return textView;
    }

    // Optional lifecycle callbacks
    @Override
    protected void onShow() {
        // Called when view becomes visible
    }

    @Override
    protected void onHide() {
        // Called when view is hidden
    }

    @Override
    protected void onDispose() {
        // Clean up resources
    }
}
```

### 2. Configure your MainActivity (Android)

Extend `NativeViewFlutterActivity` and register your view factories:

```java
public class MainActivity extends NativeViewFlutterActivity {

    @Override
    protected void onRegisterNativeViews() {
        registerNativeViewFactory("my_view", () -> new MyNativeView());
        registerNativeViewFactory("another_view", () -> new AnotherNativeView());
    }
}
```

### 3. Create Flutter widgets

Subclass `NativeViewWidget` to create a Flutter widget that manages the native view lifecycle:

```dart
class MyNativeViewWidget extends NativeViewWidget {
  const MyNativeViewWidget({super.key});

  @override
  String get viewKey => 'my_view';
}
```

### 4. Set up gesture handling (Dart)

Wrap your app with `NativeViewOverlayApp` and the native view area with `NativeViewOverlayBody`:

```dart
void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return NativeViewOverlayApp(
      enabled: true,
      child: MaterialApp(
        home: const HomePage(),
      ),
    );
  }
}

class HomePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent, // Important!
      body: Stack(
        children: [
          // Native view area
          Positioned.fill(
            child: NativeViewOverlayBody(
              enabled: true,
              child: MyNativeViewWidget(),
            ),
          ),
          // Flutter UI overlay
          Positioned(
            bottom: 16,
            right: 16,
            child: FloatingActionButton(
              onPressed: () {},
              child: Icon(Icons.add),
            ),
          ),
        ],
      ),
    );
  }
}
```

## Architecture

### Layer Structure

```
┌─────────────────────────────┐
│     Flutter UI (top)        │  ← Transparent, receives all touch events first
├─────────────────────────────┤
│   Native View Container     │  ← Native views render here
└─────────────────────────────┘
```

### Gesture Handling

Touch events flow through the Activity to both Flutter and native views. The pointer claiming mechanism allows Flutter widgets to "claim" specific pointers, preventing those events from being forwarded to the native view.

- `NativeViewOverlayApp` - Wraps your app to intercept all pointer events
- `NativeViewOverlayBody` - Marks areas where touches should forward to native views
- Touches on Flutter UI (buttons, cards, etc.) are automatically claimed
- Unclaimed touches are forwarded to the active native view

### Method Channels

| Channel | Purpose |
|---------|---------|
| `plugins.flutter.dev/native_view_flutter_activity` | View management: addView, removeView, showView, hideView, switchToView |
| `plugins.flutter.dev/native_view_flutter_activity/gestures` | Gesture control: setGesturesEnabled, claimPointer, releasePointer |

## API Reference

### Dart

#### NativeViewWidget

Base widget for native views. Automatically manages view lifecycle (add on init, remove on dispose).

```dart
abstract class NativeViewWidget extends StatefulWidget {
  String get viewKey;        // Unique identifier matching registered factory
  void onViewShown() {}      // Called when view becomes visible
  void onViewHidden() {}     // Called when view is hidden
}
```

#### NativeViewChannel

Low-level API for manual view control:

```dart
NativeViewChannel.instance.addView('my_view');
NativeViewChannel.instance.showView('my_view');
NativeViewChannel.instance.hideView('my_view');
NativeViewChannel.instance.switchToView('another_view');
NativeViewChannel.instance.removeView('my_view');
```

#### NativeViewGestureController

Manual gesture control:

```dart
NativeViewGestureController.setGesturesEnabled(true);
NativeViewGestureController.claimPointer(pointerId);
NativeViewGestureController.releasePointer(pointerId);
```

### Java

#### NativeView

Base class for native views:

```java
public abstract class NativeView {
    protected abstract View onCreateView();
    protected void onViewCreated() {}
    protected void onShow() {}
    protected void onHide() {}
    protected void onDispose() {}

    // Lifecycle callbacks
    protected void onStart() {}
    protected void onResume() {}
    protected void onPause() {}
    protected void onStop() {}

    // Utilities
    protected Context getContext();
    protected FlutterEngine getFlutterEngine();
    protected Lifecycle getLifecycle();
}
```

#### NativeViewFlutterActivity

Base activity class:

```java
public abstract class NativeViewFlutterActivity extends FlutterActivity {
    protected abstract void onRegisterNativeViews();
    protected void registerNativeViewFactory(String key, Supplier<NativeView> factory);

    // View management
    public boolean addView(String key);
    public boolean removeView(String key);
    public boolean showView(String key);
    public boolean hideView(String key);
    public boolean switchToView(String key);
}
```

## Example

See the [example](example/) directory for a complete working example with multiple native views and gesture handling.

## Requirements

- Flutter 3.35.0+
- Dart SDK 3.9.0+
- Android API 21+

## License

MIT
