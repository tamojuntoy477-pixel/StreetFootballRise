package com.nox.fakebattery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText valueInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Bateria Fake");

        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.rgb(15, 15, 18));

        TextView title = new TextView(this);
        title.setText("Bateria Fake");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-2, -2));

        TextView sub = new TextView(this);
        sub.setText("Escolha um valor de 0% a 9999%. É só visual e não altera a carga real do celular.");
        sub.setTextColor(Color.LTGRAY);
        sub.setTextSize(16);
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.setMargins(0, dp(12), 0, dp(24));
        root.addView(sub, subLp);

        valueInput = new EditText(this);
        valueInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        valueInput.setText(String.valueOf(getPreferences(MODE_PRIVATE).getInt("last_value", 9999)));
        valueInput.setTextColor(Color.WHITE);
        valueInput.setHintTextColor(Color.GRAY);
        valueInput.setHint("9999");
        valueInput.setTextSize(28);
        valueInput.setGravity(Gravity.CENTER);
        valueInput.setSingleLine(true);
        root.addView(valueInput, new LinearLayout.LayoutParams(-1, dp(64)));

        Button show = new Button(this);
        show.setText("MOSTRAR NO TOPO");
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, dp(56));
        btnLp.setMargins(0, dp(20), 0, 0);
        root.addView(show, btnLp);

        Button hide = new Button(this);
        hide.setText("ESCONDER");
        LinearLayout.LayoutParams hideLp = new LinearLayout.LayoutParams(-1, dp(56));
        hideLp.setMargins(0, dp(10), 0, 0);
        root.addView(hide, hideLp);

        Button permission = new Button(this);
        permission.setText("PERMISSÃO DE SOBREPOSIÇÃO");
        LinearLayout.LayoutParams permLp = new LinearLayout.LayoutParams(-1, dp(56));
        permLp.setMargins(0, dp(10), 0, 0);
        root.addView(permission, permLp);

        TextView tip = new TextView(this);
        tip.setText("Dica: depois de aparecer, arraste a bateria falsa para encaixar onde você quiser.");
        tip.setTextColor(Color.GRAY);
        tip.setTextSize(14);
        tip.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tipLp = new LinearLayout.LayoutParams(-1, -2);
        tipLp.setMargins(0, dp(22), 0, 0);
        root.addView(tip, tipLp);

        setContentView(root);

        show.setOnClickListener(v -> showOverlay());
        hide.setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            Toast.makeText(this, "Bateria falsa escondida", Toast.LENGTH_SHORT).show();
        });
        permission.setOnClickListener(v -> openOverlayPermission());
    }

    private void showOverlay() {
        String raw = valueInput.getText().toString().trim();
        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (Exception e) {
            value = 9999;
        }
        value = Math.max(0, Math.min(9999, value));
        valueInput.setText(String.valueOf(value));
        getPreferences(MODE_PRIVATE).edit().putInt("last_value", value).apply();

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Libere 'aparecer sobre outros apps' e volte aqui", Toast.LENGTH_LONG).show();
            openOverlayPermission();
            return;
        }

        Intent intent = new Intent(this, OverlayService.class);
        intent.putExtra("value", value);
        startService(intent);
        Toast.makeText(this, "Mostrando " + value + "% (visual)", Toast.LENGTH_SHORT).show();
    }

    private void openOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
