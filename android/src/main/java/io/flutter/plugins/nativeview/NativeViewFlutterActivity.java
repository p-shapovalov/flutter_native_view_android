package io.flutter.plugins.nativeview;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.android.TransparencyMode;
import io.flutter.embedding.engine.FlutterEngine;
import java.util.function.Supplier;

/**
 * FlutterActivity that hosts native views below a transparent Flutter view.
 *
 * <p>Views are created on demand via method channel "plugins.flutter.dev/native_view_flutter_activity".
 */
public abstract class NativeViewFlutterActivity extends FlutterActivity
    implements NativeViewDelegate.Host {

  private final NativeViewDelegate delegate = new NativeViewDelegate(this);

  @Override
  public Activity getActivity() {
    return this;
  }

  @Override
  public Lifecycle getHostLifecycle() {
    return getLifecycle();
  }

  /** Register view factories. Called during onCreate. */
  protected abstract void onRegisterNativeViews();

  /** Registers a factory that creates NativeView instances on demand. */
  protected final void registerNativeViewFactory(
      @NonNull String key, @NonNull Supplier<NativeView> factory) {
    delegate.registerNativeViewFactory(key, factory);
  }

  @Nullable
  protected final NativeView getNativeView(@NonNull String key) {
    return delegate.getNativeView(key);
  }

  @Nullable
  protected final NativeView getActiveNativeView() {
    return delegate.getActiveNativeView();
  }

  @Nullable
  protected final String getActiveViewKey() {
    return delegate.getActiveViewKey();
  }

  public boolean showView(@NonNull String key) {
    return delegate.showView(key);
  }

  public boolean hideView(@NonNull String key) {
    return delegate.hideView(key);
  }

  /**
   * Updates the gesture handler's target view.
   *
   * <p>Subclasses can override this to customize which view receives touch events. By default, uses
   * the native view's content view.
   */
  @Override
  public void updateGestureHandlerTarget(@Nullable NativeView nativeView) {
    delegate.defaultUpdateGestureHandlerTarget(nativeView);
  }

  public boolean hasView(@NonNull String key) {
    return delegate.hasView(key);
  }

  public boolean hasViewFactory(@NonNull String key) {
    return delegate.hasViewFactory(key);
  }

  public boolean addView(@NonNull String key) {
    return delegate.addView(key);
  }

  public boolean removeView(@NonNull String key) {
    return delegate.removeView(key);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    onRegisterNativeViews();
    delegate.onCreate();
  }

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
    delegate.configureFlutterEngine(flutterEngine);
  }

  @Nullable
  public NativeViewGestureHandler getGestureHandler() {
    return delegate.getGestureHandler();
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    delegate.dispatchTouchEvent(event);
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
    delegate.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    delegate.onResume();
  }

  @Override
  protected void onPause() {
    delegate.onPause();
    super.onPause();
  }

  @Override
  protected void onStop() {
    delegate.onStop();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    delegate.onDestroy();
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    delegate.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    delegate.onLowMemory();
  }
}
