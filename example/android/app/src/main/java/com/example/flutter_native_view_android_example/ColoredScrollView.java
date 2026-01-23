package com.example.flutter_native_view_android_example;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import io.flutter.plugins.nativeview.NativeView;

/**
 * A simple scrollable native view with colored item cards.
 * Demonstrates touch handling in a native Android view below Flutter.
 */
public class ColoredScrollView extends NativeView {

    private final int baseColor;

    public ColoredScrollView(int color) {
        this.baseColor = color;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(lightenColor(baseColor, 0.85f));

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(16), dp(120), dp(16), dp(200)); // Extra padding for Flutter UI

        // Header
        TextView header = new TextView(context);
        header.setText("Native Android ScrollView");
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextColor(darkenColor(baseColor, 0.3f));
        header.setPadding(0, dp(16), 0, dp(8));
        container.addView(header);

        TextView subtitle = new TextView(context);
        subtitle.setText("Scroll to see native view handling touch events");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        subtitle.setTextColor(0xFF666666);
        subtitle.setPadding(0, 0, 0, dp(16));
        container.addView(subtitle);

        // Add scrollable items
        for (int i = 1; i <= 20; i++) {
            container.addView(createItemCard(context, i));
        }

        scrollView.addView(container);
        return scrollView;
    }

    private View createItemCard(Context context, int index) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(params);
        card.setElevation(dp(2));

        // Color indicator
        View colorIndicator = new View(context);
        colorIndicator.setBackgroundColor(baseColor);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(4)
        );
        indicatorParams.setMargins(0, 0, 0, dp(12));
        colorIndicator.setLayoutParams(indicatorParams);
        card.addView(colorIndicator);

        // Title
        TextView title = new TextView(context);
        title.setText("Native Item #" + index);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF333333);
        card.addView(title);

        // Description
        TextView description = new TextView(context);
        description.setText("This card is rendered by native Android code. " +
                "Touch events are forwarded from Flutter to this ScrollView.");
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        description.setTextColor(0xFF666666);
        description.setPadding(0, dp(8), 0, 0);
        card.addView(description);

        // Make card clickable with visual feedback
        card.setClickable(true);
        card.setFocusable(true);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        card.setForeground(context.getDrawable(outValue.resourceId));

        card.setOnClickListener(v -> {
            // Visual feedback only - the ripple effect shows the touch was received
        });

        return card;
    }

    private int dp(int value) {
        Context context = getContext();
        if (context == null) return value;
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()
        );
    }

    private int lightenColor(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) + 255 * factor));
        int green = (int) ((Color.green(color) * (1 - factor) + 255 * factor));
        int blue = (int) ((Color.blue(color) * (1 - factor) + 255 * factor));
        return Color.argb(255, red, green, blue);
    }

    private int darkenColor(int color, float factor) {
        int red = (int) (Color.red(color) * (1 - factor));
        int green = (int) (Color.green(color) * (1 - factor));
        int blue = (int) (Color.blue(color) * (1 - factor));
        return Color.argb(255, red, green, blue);
    }
}
