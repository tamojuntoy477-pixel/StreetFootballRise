package com.noxtv.streetfootballrise;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        try {
            setContentView(new GameView(this));
        } catch (Throwable error) {
            TextView fallback = new TextView(this);
            fallback.setText("Street Football Rise\nToque para tentar novamente");
            fallback.setTextSize(24);
            fallback.setTextColor(Color.WHITE);
            fallback.setBackgroundColor(Color.rgb(8,18,31));
            fallback.setGravity(android.view.Gravity.CENTER);
            fallback.setOnClickListener(v -> setContentView(new GameView(this)));
            setContentView(fallback);
        }
    }
}
