package io.flutter.plugins.nativeview;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.android.TransparencyMode;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * FlutterActivity that hosts native views below a transparent Flutter view.
 *
 * <p>Views are created on demand via method channel "plugins.flutter.dev/native_view_flutter_activity".
 */
public abstract class NativeViewFlutterActivity extends FlutterActivity
    implements MethodChannel.MethodCallHandler {

  private static final String CHANNEL_NAME = "plugins.flutter.dev/native_view_flutter_activity";

  private final Map<String, Supplier<NativeView>> viewFactories = new HashMap<>();
  private final Map<String, NativeView> nativeViews = new HashMap<>();

  @Nullable private String activeViewKey;
  @Nullable private FlutterEngine cachedFlutterEngine;
  @Nullable private FrameLayout viewWrapper;
  @Nullable private FrameLayout nativeViewContainer;
  @Nullable private MethodChannel methodChannel;
  @Nullable private NativeViewGestureHandler gestureHandler;

  /** Register view factories. Called during onCreate. */
  protected abstract void onRegisterNativeViews();

  /** Registers a factory that creates NativeView instances on demand. */
  protected final void registerNativeViewFactory(
      @NonNull String key, @NonNull Supplier<NativeView> factory) {
    viewFactories.put(key, factory);
  }

  @Nullable
  protected final NativeView getNativeView(@NonNull String key) {
    return nativeViews.get(key);
  }

  @Nullable
  protected final NativeView getActiveNativeView() {
    if (activeViewKey != null) {
      return nativeViews.get(activeViewKey);
    }
    return null;
  }

  @Nullable
  protected final String getActiveViewKey() {
    return activeViewKey;
  }

  public boolean showView(@NonNull String key) {
    NativeView view = nativeViews.get(key);
    if (view == null) {
      return false;
    }
    view.show();
    activeViewKey = key;
    updateGestureHandlerTarget(view);
    return true;
  }

  public boolean hideView(@NonNull String key) {
    NativeView view = nativeViews.get(key);
    if (view == null) {
      return false;
    }
    view.hide();
    if (key.equals(activeViewKey)) {
      activeViewKey = null;
      updateGestureHandlerTarget(null);
    }
    return true;
  }

  /**
   * Updates the gesture handler's target view.
   *
   * <p>Subclasses can override this to customize which view receives touch events. By default, uses
   * the native view's content view.
   *
   * @param nativeView The native view that should receive touch events, or null to clear the
   *     target.
   */
  protected void updateGestureHandlerTarget(@Nullable NativeView nativeView) {
    if (gestureHandler != null) {
      gestureHandler.setTargetView(nativeView != null ? nativeView.getView() : null);
    }
  }

  public boolean hasView(@NonNull String key) {
    return nativeViews.containsKey(key);
  }

  public boolean hasViewFactory(@NonNull String key) {
    return viewFactories.containsKey(key);
  }

  /** Creates and adds a native view to the hierarchy (initially hidden). */
  public boolean addView(@NonNull String key) {
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

    nativeView.initialize(key, this, cachedFlutterEngine, this::getLifecycle);

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

  /** Removes and disposes a native view from the hierarchy. */
  public boolean removeView(@NonNull String key) {
    NativeView nativeView = nativeViews.remove(key);
    if (nativeView == null) {
      return false;
    }

    if (key.equals(activeViewKey)) {
      activeViewKey = null;
    }

    View view = nativeView.getView();
    if (view != null && nativeViewContainer != null) {
      nativeViewContainer.removeView(view);
    }

    nativeView.dispose();
    return true;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    onRegisterNativeViews();

    View contentView = findViewById(android.R.id.content);
    if (contentView instanceof ViewGroup contentParent) {
      if (contentParent.getChildCount() > 0) {
        View flutterView = contentParent.getChildAt(0);
        contentParent.removeView(flutterView);

        viewWrapper = new FrameLayout(this);
        viewWrapper.setLayoutParams(
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        nativeViewContainer = new FrameLayout(this);
        nativeViewContainer.setLayoutParams(
            new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        viewWrapper.addView(nativeViewContainer);
        viewWrapper.addView(flutterView);
        contentParent.addView(viewWrapper);
      }
    }
  }

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
    cachedFlutterEngine = flutterEngine;
    methodChannel =
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL_NAME);
    methodChannel.setMethodCallHandler(this);
    // Initialize gesture handler early so it's available before any native views are created.
    // This allows Flutter widgets to call claimPointer() etc. at any time.
    gestureHandler =
        new NativeViewGestureHandler(flutterEngine.getDartExecutor().getBinaryMessenger());
  }

  /**
   * Gets the gesture handler for this activity.
   *
   * <p>Native views can use this to register themselves as the target for touch event forwarding.
   *
   * @return The gesture handler, or null if not yet initialized.
   */
  @Nullable
  public NativeViewGestureHandler getGestureHandler() {
    return gestureHandler;
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

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    // Use the gesture handler to dispatch touch events to the active native view.
    // The gesture handler handles pointer claiming and enables/disables gesture forwarding.
    if (gestureHandler != null) {
      gestureHandler.dispatchTouchEvent(event);
    }
    return super.dispatchTouchEvent(event);
  }

  @NonNull
  @Override
  public TransparencyMode getTransparencyMode() {
    return TransparencyMode.transparent;
  }

  @Override
  protected void onStart() {
    super.onStart();
    for (NativeView view : nativeViews.values()) {
      view.onStart();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    for (NativeView view : nativeViews.values()) {
      view.onResume();
    }
  }

  @Override
  protected void onPause() {
    for (NativeView view : nativeViews.values()) {
      view.onPause();
    }
    super.onPause();
  }

  @Override
  protected void onStop() {
    for (NativeView view : nativeViews.values()) {
      view.onStop();
    }
    super.onStop();
  }

  @Override
  protected void onDestroy() {
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
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    for (NativeView view : nativeViews.values()) {
      view.onSaveInstanceState(outState);
    }
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    for (NativeView view : nativeViews.values()) {
      view.onLowMemory();
    }
  }
}
