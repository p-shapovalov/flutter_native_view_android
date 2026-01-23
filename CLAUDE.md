# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Flutter plugin for hosting native Android views below a transparent Flutter view. This enables rendering native views (like maps) behind Flutter's UI layer while handling gesture coordination between the two layers.

## Build Commands

```bash
# Analyze Dart code
flutter analyze

# Format Dart code
dart format lib/

# Run Android lint (from android/ directory)
cd android && ./gradlew lint
```

## Architecture

### Layer Structure

The plugin renders native Android views *below* a transparent FlutterView, not as platform views embedded within Flutter. This approach avoids platform view performance issues but requires explicit gesture coordination.

### Dart Side (lib/)

- **NativeViewChannel** - Method channel wrapper for view lifecycle (add/remove/show/hide/switch)
- **NativeViewWidget** - Base StatefulWidget that manages native view lifecycle automatically
- **NativeViewGestureController** - Static methods to enable/disable gesture forwarding and claim/release pointers
- **NativeViewOverlayApp / NativeViewOverlayBody** - Widget pair for automatic pointer claiming (wrap app + wrap native view area)

### Java Side (android/)

- **NativeView** - Abstract base class for native views; subclass and implement `onCreateView()`
- **NativeViewFlutterActivity** - Extends FlutterActivity with transparent mode; manages view hierarchy and method channel
- **NativeViewGestureHandler** - Handles touch event dispatching with pointer claiming mechanism

### Method Channels

| Channel | Purpose |
|---------|---------|
| `plugins.flutter.dev/native_view_flutter_activity` | View management: addView, removeView, showView, hideView, switchToView |
| `plugins.flutter.dev/native_view_flutter_activity/gestures` | Gesture control: setGesturesEnabled, claimPointer, releasePointer |

### Gesture Handling

Touch events flow through the Activity to both Flutter and native views. The pointer claiming mechanism allows Flutter widgets to "claim" specific pointers, preventing those events from being forwarded to the native view. This enables Flutter UI overlays to capture gestures while the native view handles unclaimed touches.
