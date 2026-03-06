package io.flutter.plugins.nativeview;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Delegate that implements native view management logic shared by both {@link
 * NativeViewFlutterActivity} and {@link NativeViewFlutterFragmentActivity}.
 */
class NativeViewDelegate implements MethodChannel.MethodCallHandler {

  interface Host {
    Activity getActivity();

    Lifecycle getHostLifecycle();

    void updateGestureHandlerTarget(@Nullable NativeView nativeView);
  }

  private static final String CHANNEL_NAME = "plugins.flutter.dev/native_view_flutter_activity";

  private final Host host;
  private final Map<String, Supplier<NativeView>> viewFactories = new HashMap<>();
  private final Map<String, NativeView> nativeViews = new HashMap<>();

  @Nullable private String activeViewKey;
  @Nullable private FlutterEngine cachedFlutterEngine;
  @Nullable private FrameLayout viewWrapper;
  @Nullable private FrameLayout nativeViewContainer;
  @Nullable private MethodChannel methodChannel;
  @Nullable private NativeViewGestureHandler gestureHandler;

  NativeViewDelegate(@NonNull Host host) {
    this.host = host;
  }

  void registerNativeViewFactory(@NonNull String key, @NonNull Supplier<NativeView> factory) {
    viewFactories.put(key, factory);
  }

  @Nullable
  NativeView getNativeView(@NonNull String key) {
    return nativeViews.get(key);
  }

  @Nullable
  NativeView getActiveNativeView() {
    if (activeViewKey != null) {
      return nativeViews.get(activeViewKey);
    }
    return null;
  }

  @Nullable
  String getActiveViewKey() {
    return activeViewKey;
  }

  @Nullable
  NativeViewGestureHandler getGestureHandler() {
    return gestureHandler;
  }

  boolean showView(@NonNull String key) {
    NativeView view = nativeViews.get(key);
    if (view == null) {
      return false;
    }
    view.show();
    activeViewKey = key;
    host.updateGestureHandlerTarget(view);
    return true;
  }

  boolean hideView(@NonNull String key) {
    NativeView view = nativeViews.get(key);
    if (view == null) {
      return false;
    }
    view.hide();
    if (key.equals(activeViewKey)) {
      activateTopmostVisibleView();
    }
    return true;
  }

  private void activateTopmostVisibleView() {
    if (nativeViewContainer == null) {
      activeViewKey = null;
      host.updateGestureHandlerTarget(null);
      return;
    }

    for (int i = nativeViewContainer.getChildCount() - 1; i >= 0; i--) {
      View child = nativeViewContainer.getChildAt(i);
      if (child.getVisibility() == View.VISIBLE) {
        for (Map.Entry<String, NativeView> entry : nativeViews.entrySet()) {
          if (entry.getValue().getView() == child) {
            activeViewKey = entry.getKey();
            host.updateGestureHandlerTarget(entry.getValue());
            return;
          }
        }
      }
    }

    activeViewKey = null;
    host.updateGestureHandlerTarget(null);
  }

  void defaultUpdateGestureHandlerTarget(@Nullable NativeView nativeView) {
    if (gestureHandler != null) {
      gestureHandler.setTargetView(nativeView != null ? nativeView.getView() : null);
    }
  }

  boolean hasView(@NonNull String key) {
    return nativeViews.containsKey(key);
  }

  boolean hasViewFactory(@NonNull String key) {
    return viewFactories.containsKey(key);
  }

  boolean addView(@NonNull String key) {
    if (nativeViews.containsKey(key)) {
      return true;
    }

    Supplier<NativeView> factory = viewFactories.get(key);
    if (factory == null) {
      return false;
    }

    if (cachedFlutterEngine == null || nativeViewContainer == null) {
      return false;
    }

    NativeView nativeView = factory.get();
    if (nativeView == null) {
      return false;
    }

    Activity activity = host.getActivity();
    nativeView.initialize(key, activity, cachedFlutterEngine, host::getHostLifecycle);

    View view = nativeView.getView();
    if (view == null) {
      return false;
    }

    view.setLayoutParams(
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

    nativeViewContainer.addView(view);
    nativeViews.put(key, nativeView);
    nativeView.notifyViewCreated();
    nativeView.hide();

    return true;
  }

  boolean removeView(@NonNull String key) {
    NativeView nativeView = nativeViews.remove(key);
    if (nativeView == null) {
      return false;
    }

    boolean wasActive = key.equals(activeViewKey);

    View view = nativeView.getView();
    if (view != null && nativeViewContainer != null) {
      nativeViewContainer.removeView(view);
    }

    nativeView.dispose();

    if (wasActive) {
      activateTopmostVisibleView();
    }

    return true;
  }

  void onCreate() {
    Activity activity = host.getActivity();
    View contentView = activity.findViewById(android.R.id.content);
    if (contentView instanceof ViewGroup contentParent) {
      if (contentParent.getChildCount() > 0) {
        View flutterView = contentParent.getChildAt(0);
        contentParent.removeView(flutterView);

        viewWrapper = new FrameLayout(activity);
        viewWrapper.setLayoutParams(
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        nativeViewContainer = new FrameLayout(activity);
        nativeViewContainer.setLayoutParams(
            new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        viewWrapper.addView(nativeViewContainer);
        viewWrapper.addView(flutterView);
        contentParent.addView(viewWrapper);
      }
    }
  }

  void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    cachedFlutterEngine = flutterEngine;
    methodChannel =
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL_NAME);
    methodChannel.setMethodCallHandler(this);
    gestureHandler =
        new NativeViewGestureHandler(flutterEngine.getDartExecutor().getBinaryMessenger());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    String viewKey;
    switch (call.method) {
      case "addView":
        viewKey = call.argument("viewKey");
        if (viewKey != null) {
          result.success(addView(viewKey));
        } else {
          result.error("INVALID_ARGUMENT", "viewKey is required", null);
        }
        break;
      case "removeView":
        viewKey = call.argument("viewKey");
        if (viewKey != null) {
          result.success(removeView(viewKey));
        } else {
          result.error("INVALID_ARGUMENT", "viewKey is required", null);
        }
        break;
      case "showView":
        viewKey = call.argument("viewKey");
        if (viewKey != null) {
          result.success(showView(viewKey));
        } else {
          result.error("INVALID_ARGUMENT", "viewKey is required", null);
        }
        break;
      case "hideView":
        viewKey = call.argument("viewKey");
        if (viewKey != null) {
          result.success(hideView(viewKey));
        } else {
          result.error("INVALID_ARGUMENT", "viewKey is required", null);
        }
        break;
      case "getActiveViewKey":
        result.success(activeViewKey);
        break;
      case "hasView":
        viewKey = call.argument("viewKey");
        if (viewKey != null) {
          result.success(hasView(viewKey));
        } else {
          result.error("INVALID_ARGUMENT", "viewKey is required", null);
        }
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  void dispatchTouchEvent(MotionEvent event) {
    if (gestureHandler != null) {
      gestureHandler.dispatchTouchEvent(event);
    }
  }

  void onStart() {
    for (NativeView view : nativeViews.values()) {
      view.onStart();
    }
  }

  void onResume() {
    for (NativeView view : nativeViews.values()) {
      view.onResume();
    }
  }

  void onPause() {
    for (NativeView view : nativeViews.values()) {
      view.onPause();
    }
  }

  void onStop() {
    for (NativeView view : nativeViews.values()) {
      view.onStop();
    }
  }

  void onDestroy() {
    if (methodChannel != null) {
      methodChannel.setMethodCallHandler(null);
      methodChannel = null;
    }
    if (gestureHandler != null) {
      gestureHandler.dispose();
      gestureHandler = null;
    }
    for (NativeView view : nativeViews.values()) {
      view.dispose();
    }
    nativeViews.clear();
    activeViewKey = null;
  }

  void onSaveInstanceState(@NonNull Bundle outState) {
    for (NativeView view : nativeViews.values()) {
      view.onSaveInstanceState(outState);
    }
  }

  void onLowMemory() {
    for (NativeView view : nativeViews.values()) {
      view.onLowMemory();
    }
  }
}
