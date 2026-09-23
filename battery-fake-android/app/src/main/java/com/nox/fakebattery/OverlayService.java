package com.nox.fakebattery;

import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private LinearLayout overlay;
    private TextView percentText;
    private BatteryGlyph batteryGlyph;
    private WindowManager.LayoutParams params;

    private float downRawX, downRawY;
    private int startX, startY;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.HORIZONTAL);
        overlay.setGravity(Gravity.CENTER_VERTICAL);
        overlay.setPadding(dp(4), dp(2), dp(4), dp(2));

        batteryGlyph = new BatteryGlyph();
        overlay.addView(batteryGlyph, new LinearLayout.LayoutParams(dp(24), dp(13)));

        percentText = new TextView(this);
        percentText.setTextColor(Color.WHITE);
        percentText.setTextSize(12);
        percentText.setShadowLayer(2.5f, 0f, 0f, Color.BLACK);
        percentText.setIncludeFontPadding(false);
        percentText.setPadding(dp(4), 0, 0, 0);
        overlay.addView(percentText, new LinearLayout.LayoutParams(-2, -2));

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        int width = getResources().getDisplayMetrics().widthPixels;
        params.x = Math.max(dp(8), width - dp(92));
        params.y = dp(4);

        overlay.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downRawX = event.getRawX();
                    downRawY = event.getRawY();
                    startX = params.x;
                    startY = params.y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = startX + Math.round(event.getRawX() - downRawX);
                    params.y = startY + Math.round(event.getRawY() - downRawY);
                    if (windowManager != null && overlay != null) {
                        windowManager.updateViewLayout(overlay, params);
                    }
                    return true;
                default:
                    return true;
            }
        });

        windowManager.addView(overlay, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("value")) {
            int value = Math.max(0, Math.min(9999, intent.getIntExtra("value", 9999)));
            if (percentText != null) percentText.setText(value + "%");
            if (batteryGlyph != null) batteryGlyph.setFakeValue(value);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (windowManager != null && overlay != null) {
            try {
                windowManager.removeView(overlay);
            } catch (Exception ignored) {}
        }
        overlay = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private class BatteryGlyph extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int fakeValue = 100;

        BatteryGlyph() {
            super(OverlayService.this);
        }

        void setFakeValue(int value) {
            fakeValue = value;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float stroke = Math.max(1.5f, dp(1));
            float nub = Math.max(2f, w * 0.10f);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(Color.WHITE);
            RectF body = new RectF(stroke, stroke, w - nub - stroke, h - stroke);
            canvas.drawRoundRect(body, dp(2), dp(2), paint);

            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(new RectF(w - nub, h * 0.30f, w, h * 0.70f), dp(1), dp(1), paint);

            float fraction = Math.min(fakeValue, 100) / 100f;
            float innerLeft = body.left + dp(2);
            float innerRight = body.right - dp(2);
            float innerTop = body.top + dp(2);
            float innerBottom = body.bottom - dp(2);
            float fillRight = innerLeft + (innerRight - innerLeft) * fraction;
            if (fillRight > innerLeft) {
                canvas.drawRoundRect(new RectF(innerLeft, innerTop, fillRight, innerBottom), dp(1), dp(1), paint);
            }
        }
    }
}
