import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// Channel for communicating with native views in NativeViewFlutterActivity.
///
/// This channel allows Dart widgets to register and control native views
/// that are rendered below the transparent Flutter view.
class NativeViewChannel {
  NativeViewChannel._();

  static final NativeViewChannel _instance = NativeViewChannel._();

  /// The singleton instance of [NativeViewChannel].
  static NativeViewChannel get instance => _instance;

  static const MethodChannel _channel = MethodChannel(
    'plugins.flutter.dev/native_view_flutter_activity',
  );

  /// Shows a native view that is already in the hierarchy.
  Future<bool> showView(String viewKey) async {
    try {
      final bool? result = await _channel.invokeMethod<bool>(
        'showView',
        <String, dynamic>{'viewKey': viewKey},
      );
      return result ?? false;
    } catch (e) {
      debugPrint('NativeViewChannel.showView error: $e');
      return false;
    }
  }

  /// Hides a native view that is in the hierarchy.
  Future<bool> hideView(String viewKey) async {
    try {
      final bool? result = await _channel.invokeMethod<bool>(
        'hideView',
        <String, dynamic>{'viewKey': viewKey},
      );
      return result ?? false;
    } catch (e) {
      debugPrint('NativeViewChannel.hideView error: $e');
      return false;
    }
  }

  /// Hides the current view and shows the view with [viewKey].
  Future<bool> switchToView(String viewKey) async {
    try {
      final bool? result = await _channel.invokeMethod<bool>(
        'switchToView',
        <String, dynamic>{'viewKey': viewKey},
      );
      return result ?? false;
    } catch (e) {
      debugPrint('NativeViewChannel.switchToView error: $e');
      return false;
    }
  }

  /// Gets the key of the currently active native view, or null if none.
  Future<String?> getActiveViewKey() async {
    try {
      return await _channel.invokeMethod<String>('getActiveViewKey');
    } catch (e) {
      debugPrint('NativeViewChannel.getActiveViewKey error: $e');
      return null;
    }
  }

  /// Checks if a native view with the given key is in the hierarchy.
  Future<bool> hasView(String viewKey) async {
    try {
      final bool? result = await _channel.invokeMethod<bool>(
        'hasView',
        <String, dynamic>{'viewKey': viewKey},
      );
      return result ?? false;
    } catch (e) {
      debugPrint('NativeViewChannel.hasView error: $e');
      return false;
    }
  }

  /// Creates and adds a native view to the hierarchy.
  Future<bool> addView(String viewKey) async {
    try {
      final bool? result = await _channel.invokeMethod<bool>(
        'addView',
        <String, dynamic>{'viewKey': viewKey},
      );
      return result ?? false;
    } catch (e) {
      debugPrint('NativeViewChannel.addView error: $e');
      return false;
    }
  }

  /// Removes and disposes a native view from the hierarchy.
  Future<bool> removeView(String viewKey) async {
    try {
      final bool? result = await _channel.invokeMethod<bool>(
        'removeView',
        <String, dynamic>{'viewKey': viewKey},
      );
      return result ?? false;
    } catch (e) {
      debugPrint('NativeViewChannel.removeView error: $e');
      return false;
    }
  }
}

/// Base widget for native views rendered below the Flutter layer.
///
/// Handles adding/removing native views on widget lifecycle. Renders a
/// transparent container to allow the native view to show through.
abstract class NativeViewWidget extends StatefulWidget {
  /// Creates a native view widget.
  const NativeViewWidget({super.key});

  /// The unique key identifying the native view to control.
  String get viewKey;

  /// Called when the native view has been shown.
  void onViewShown() {}

  /// Called when the native view has been hidden.
  void onViewHidden() {}

  @override
  State<NativeViewWidget> createState() =>
      NativeViewWidgetState<NativeViewWidget>();
}

/// State for [NativeViewWidget].
///
/// Lifecycle: [initState] → [addNativeView] → [showNativeView],
/// [dispose] → [removeNativeView].
class NativeViewWidgetState<T extends NativeViewWidget> extends State<T> {
  bool _isShown = false;

  /// Whether the native view is currently shown.
  @protected
  bool get isShown => _isShown;

  @override
  void initState() {
    super.initState();
    addNativeView();
  }

  @override
  void dispose() {
    removeNativeView();
    super.dispose();
  }

  @override
  void didUpdateWidget(T oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.viewKey != widget.viewKey) {
      removeNativeViewByKey(oldWidget.viewKey);
      addNativeView();
    }
  }

  /// Adds the native view to the hierarchy and shows it.
  @protected
  Future<void> addNativeView() async {
    final bool success = await NativeViewChannel.instance.addView(
      widget.viewKey,
    );
    if (success && mounted) {
      await showNativeView();
    }
  }

  /// Shows the native view. Called after [addNativeView].
  @protected
  Future<void> showNativeView() async {
    final bool success = await NativeViewChannel.instance.switchToView(
      widget.viewKey,
    );
    if (success && mounted) {
      setState(() {
        _isShown = true;
      });
      widget.onViewShown();
    }
  }

  /// Removes the native view from the hierarchy.
  @protected
  Future<void> removeNativeView() async {
    if (_isShown) {
      _isShown = false;
      widget.onViewHidden();
    }
    await NativeViewChannel.instance.removeView(widget.viewKey);
  }

  /// Removes a native view by its key.
  @protected
  Future<void> removeNativeViewByKey(String viewKey) async {
    await NativeViewChannel.instance.removeView(viewKey);
  }

  @override
  Widget build(BuildContext context) {
    return const ColoredBox(color: Color(0x00000000));
  }
}
