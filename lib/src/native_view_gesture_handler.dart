import 'package:flutter/widgets.dart';

import 'native_view_gesture_controller.dart';

/// Global tracker for pointers that landed on the native view area.
///
/// This is used by [NativeViewOverlayApp] and [NativeViewOverlayBody] to
/// coordinate gesture handling across the widget tree.
class _NativeViewPointerTracker {
  _NativeViewPointerTracker._();

  static final _NativeViewPointerTracker instance =
      _NativeViewPointerTracker._();

  /// Set of pointers that landed on the native view (not on overlay widgets).
  final Set<int> _pointersOnNativeView = <int>{};

  /// Called when a pointer lands on the native view widget.
  void onNativeViewPointerDown(PointerDownEvent event) {
    _pointersOnNativeView.add(event.pointer);
  }

  /// Called for all pointer down events at the app level.
  /// Claims pointers that didn't land on the native view.
  void onAppPointerDown(PointerDownEvent event) {
    if (!_pointersOnNativeView.contains(event.pointer)) {
      // This pointer didn't land on the native view, so claim it
      NativeViewGestureController.claimPointer(event.pointer);
    }
    // Clean up - remove from tracking set
    _pointersOnNativeView.remove(event.pointer);
  }
}

/// A widget that wraps your entire app to enable native view overlay gesture handling.
///
/// This widget should wrap your [MaterialApp] or [MaterialApp.router] at the
/// top level. It intercepts all pointer events and claims those that don't
/// land on a [NativeViewOverlayBody] widget.
///
/// Example with MaterialApp:
/// ```dart
/// NativeViewOverlayApp(
///   enabled: isNativeOverlayMode,
///   child: MaterialApp(
///     home: MyHomePage(),
///   ),
/// )
/// ```
///
/// Example with MaterialApp.router:
/// ```dart
/// NativeViewOverlayApp(
///   enabled: isNativeOverlayMode,
///   child: MaterialApp.router(
///     routerConfig: _router,
///   ),
/// )
/// ```
///
/// Then in your pages, wrap the native view area with [NativeViewOverlayBody]:
/// ```dart
/// Scaffold(
///   backgroundColor: Colors.transparent,
///   body: NativeViewOverlayBody(
///     enabled: isNativeOverlayMode,
///     child: GoogleMap(...),
///   ),
/// )
/// ```
class NativeViewOverlayApp extends StatelessWidget {
  /// Creates a native view overlay app wrapper.
  const NativeViewOverlayApp({
    super.key,
    required this.enabled,
    required this.child,
  });

  /// Whether native view gesture handling is enabled.
  final bool enabled;

  /// The child widget, typically a [MaterialApp] or [MaterialApp.router].
  final Widget child;

  @override
  Widget build(BuildContext context) {
    if (!enabled) {
      return child;
    }

    return Listener(
      onPointerDown: _NativeViewPointerTracker.instance.onAppPointerDown,
      behavior: HitTestBehavior.translucent,
      child: child,
    );
  }
}

/// A widget that marks an area as containing the native view.
///
/// Wrap your native view widget (e.g., [GoogleMap]) with this to indicate that
/// pointers landing on this area should be forwarded to the native view instead
/// of being claimed by Flutter.
///
/// This widget must be used in conjunction with [NativeViewOverlayApp] at the
/// app level.
///
/// Example:
/// ```dart
/// Scaffold(
///   backgroundColor: Colors.transparent,
///   appBar: AppBar(title: Text('Map Page')),
///   body: Stack(
///     children: [
///       NativeViewOverlayBody(
///         enabled: isNativeOverlayMode,
///         child: GoogleMap(...),
///       ),
///       // Overlay widgets on top of the native view
///       Positioned(
///         bottom: 16,
///         child: FloatingActionButton(...),
///       ),
///     ],
///   ),
/// )
/// ```
class NativeViewOverlayBody extends StatelessWidget {
  /// Creates a native view overlay body.
  const NativeViewOverlayBody({
    super.key,
    required this.enabled,
    required this.child,
  });

  /// Whether native view gesture handling is enabled.
  final bool enabled;

  /// The child widget, typically a native view like [GoogleMap].
  final Widget child;

  @override
  Widget build(BuildContext context) {
    if (!enabled) {
      return child;
    }

    return Listener(
      onPointerDown: _NativeViewPointerTracker.instance.onNativeViewPointerDown,
      behavior: HitTestBehavior.translucent,
      child: child,
    );
  }
}
