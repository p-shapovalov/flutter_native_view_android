package io.flutter.plugins.nativeview;

import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import java.util.HashSet;

/** Handles gesture forwarding between Flutter and native views. */
public class NativeViewGestureHandler implements MethodChannel.MethodCallHandler {

  private static final String CHANNEL_NAME =
      "plugins.flutter.dev/native_view_flutter_activity/gestures";

  private final MethodChannel channel;

  /** The native view to forward touch events to. */
  @Nullable private View targetView;

  /** Whether touch events are dispatched to the native view. */
  private boolean gesturesEnabled = true;

  /** Pointer IDs claimed by Flutter (not forwarded to native view). */
  private final HashSet<Integer> claimedPointers = new HashSet<>();

  /** Last event dispatched to native view, used for creating cancel events. */
  @Nullable private MotionEvent lastDispatchedEvent;

  public NativeViewGestureHandler(@NonNull BinaryMessenger binaryMessenger) {
    channel = new MethodChannel(binaryMessenger, CHANNEL_NAME);
    channel.setMethodCallHandler(this);
  }

  /** Sets the native view to forward touch events to. */
  public void setTargetView(@Nullable View view) {
    this.targetView = view;
  }

  @Nullable
  public View getTargetView() {
    return targetView;
  }

  public boolean isGesturesEnabled() {
    return gesturesEnabled;
  }

  public void setGesturesEnabled(boolean enabled) {
    this.gesturesEnabled = enabled;
  }

  /** Dispatches a touch event to the target view if appropriate. */
  public void dispatchTouchEvent(@NonNull MotionEvent event) {
    int action = event.getActionMasked();
    int pointerIndex = event.getActionIndex();
    int pointerId = event.getPointerId(pointerIndex);

    // Clean up when the touch sequence ends
    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
      claimedPointers.clear();
      recycleLastEvent();
    } else if (action == MotionEvent.ACTION_POINTER_UP) {
      claimedPointers.remove(pointerId);
    }

    // Check if any pointer in this event is claimed by Flutter
    boolean hasClaimedPointer = false;
    for (int i = 0; i < event.getPointerCount(); i++) {
      if (claimedPointers.contains(event.getPointerId(i))) {
        hasClaimedPointer = true;
        break;
      }
    }

    if (gesturesEnabled && targetView != null && !hasClaimedPointer) {
      saveLastEvent(event);
      targetView.dispatchTouchEvent(event);
    }
  }

  private void saveLastEvent(@NonNull MotionEvent event) {
    recycleLastEvent();
    lastDispatchedEvent = MotionEvent.obtain(event);
  }

  private void recycleLastEvent() {
    if (lastDispatchedEvent != null) {
      lastDispatchedEvent.recycle();
      lastDispatchedEvent = null;
    }
  }

  /** Claims a pointer for exclusive Flutter handling. */
  public void claimPointer(int pointerId) {
    claimedPointers.add(pointerId);
    cancelGestureOnTargetView();
  }

  /** Releases a previously claimed pointer. */
  public void releasePointer(int pointerId) {
    claimedPointers.remove(pointerId);
  }

  /** Sends a cancel event to the target view to cancel any ongoing gesture. */
  private void cancelGestureOnTargetView() {
    if (targetView != null && lastDispatchedEvent != null) {
      MotionEvent cancelEvent = MotionEvent.obtain(lastDispatchedEvent);
      cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
      targetView.dispatchTouchEvent(cancelEvent);
      cancelEvent.recycle();
      recycleLastEvent();
    }
  }

  /** Releases resources and unregisters the method channel handler. */
  public void dispose() {
    channel.setMethodCallHandler(null);
    claimedPointers.clear();
    recycleLastEvent();
    targetView = null;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    switch (call.method) {
      case "setGesturesEnabled":
        Boolean enabled = call.argument("enabled");
        if (enabled != null) {
          gesturesEnabled = enabled;
        }
        result.success(null);
        break;
      case "isGesturesEnabled":
        result.success(gesturesEnabled);
        break;
      case "claimPointer":
        Integer pointerId = call.argument("pointerId");
        if (pointerId != null) {
          claimPointer(pointerId);
        }
        result.success(null);
        break;
      case "releasePointer":
        Integer releasePointerId = call.argument("pointerId");
        if (releasePointerId != null) {
          releasePointer(releasePointerId);
        }
        result.success(null);
        break;
      default:
        result.notImplemented();
        break;
    }
  }
}
