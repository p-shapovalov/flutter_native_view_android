package io.flutter.plugins.nativeview;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import io.flutter.embedding.engine.FlutterEngine;
import java.util.function.Supplier;

/** Base class for native views rendered below a transparent Flutter view. */
public abstract class NativeView {

  @Nullable private View contentView;
  @Nullable private Context context;
  @Nullable private FlutterEngine flutterEngine;
  @Nullable private Supplier<Lifecycle> lifecycleSupplier;
  @Nullable private String viewKey;
  private boolean isInitialized = false;
  private boolean isVisible = false;

  /** Creates the native view. Called when FlutterEngine is available. */
  @NonNull
  protected abstract View onCreateView();

  /** Called after the view is added to the hierarchy. */
  protected void onViewCreated() {}

  /** Handles touch events from the Activity. */
  public void dispatchTouchEvent(@NonNull MotionEvent event) {
    if (contentView != null && isVisible) {
      contentView.dispatchTouchEvent(event);
    }
  }

  /** Called when this view becomes visible. */
  protected void onShow() {}

  /** Called when this view is hidden. */
  protected void onHide() {}

  protected void onStart() {}

  protected void onResume() {}

  protected void onPause() {}

  protected void onStop() {}

  protected void onSaveInstanceState(@NonNull Bundle outState) {}

  protected void onLowMemory() {}

  /** Called when the view is being destroyed. */
  protected void onDispose() {}

  // Internal methods used by NativeViewFlutterActivity

  final void initialize(
      @NonNull String viewKey,
      @NonNull Context context,
      @NonNull FlutterEngine flutterEngine,
      @NonNull Supplier<Lifecycle> lifecycleSupplier) {
    if (isInitialized) {
      return;
    }
    this.viewKey = viewKey;
    this.context = context;
    this.flutterEngine = flutterEngine;
    this.lifecycleSupplier = lifecycleSupplier;
    contentView = onCreateView();
    isInitialized = true;
  }

  final void notifyViewCreated() {
    onViewCreated();
  }

  @Nullable
  final View getView() {
    return contentView;
  }

  @Nullable
  final String getViewKey() {
    return viewKey;
  }

  final boolean isInitialized() {
    return isInitialized;
  }

  final boolean isVisible() {
    return isVisible;
  }

  final void show() {
    if (!isVisible) {
      isVisible = true;
      if (contentView != null) {
        contentView.setVisibility(View.VISIBLE);
        contentView.bringToFront();
      }
      onShow();
    }
  }

  final void hide() {
    if (isVisible) {
      isVisible = false;
      if (contentView != null) {
        contentView.setVisibility(View.GONE);
      }
      onHide();
    }
  }

  final void dispose() {
    onDispose();
    contentView = null;
    context = null;
    flutterEngine = null;
    lifecycleSupplier = null;
    viewKey = null;
    isInitialized = false;
    isVisible = false;
  }

  @Nullable
  protected final Context getContext() {
    return context;
  }

  @Nullable
  protected final FlutterEngine getFlutterEngine() {
    return flutterEngine;
  }

  @Nullable
  protected final Lifecycle getLifecycle() {
    return lifecycleSupplier != null ? lifecycleSupplier.get() : null;
  }
}
