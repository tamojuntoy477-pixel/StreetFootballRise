package com.noxaria.atlas;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.*;
import android.text.*;
import java.util.*;

public class MainActivity extends Activity {
    private final String[] CODES = {
        "AF","AL","DZ","AD","AO","AG","AR","AM","AU","AT","AZ","BS","BH","BD","BB","BY","BE","BZ","BJ","BT","BO","BA","BW","BR","BN","BG","BF","BI",
        "CV","KH","CM","CA","CF","TD","CL","CN","CO","KM","CG","CD","CR","CI","HR","CU","CY","CZ","DK","DJ","DM","DO","EC","EG","SV","GQ","ER","EE","SZ","ET",
        "FJ","FI","FR","GA","GM","GE","DE","GH","GR","GD","GT","GN","GW","GY","HT","HN","HU","IS","IN","ID","IR","IQ","IE","IL","IT","JM","JP","JO","KZ","KE",
        "KI","KP","KR","KW","KG","LA","LV","LB","LS","LR","LY","LI","LT","LU","MG","MW","MY","MV","ML","MT","MH","MR","MU","MX","FM","MD","MC","MN","ME","MA",
        "MZ","MM","NA","NR","NP","NL","NZ","NI","NE","NG","MK","NO","OM","PK","PW","PS","PA","PG","PY","PE","PH","PL","PT","QA","RO","RU","RW","KN","LC","VC",
        "WS","SM","ST","SA","SN","RS","SC","SL","SG","SK","SI","SB","SO","ZA","SS","ES","LK","SD","SR","SE","CH","SY","TJ","TZ","TH","TL","TG","TO","TT",
        "TN","TR","TM","TV","UG","UA","AE","GB","US","UY","UZ","VU","VA","VE","VN","YE","ZM","ZW"
    };

    private ArrayAdapter<String> adapter;
    private final ArrayList<String> allCountries = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(12));
        root.setBackgroundColor(Color.rgb(6,17,38));

        root.addView(label("NOXÁRIA ATLAS", 26, Color.WHITE, true));
        TextView sub = label("195 países + Noxária", 14, Color.rgb(24,230,255), true);
        sub.setPadding(0,0,0,dp(12));
        root.addView(sub);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(14),dp(14),dp(14));
        card.setBackgroundColor(Color.rgb(16,40,77));

        card.addView(label("✦ NOXÁRIA", 24, Color.WHITE, true));
        card.addView(label("MENOR PAÍS • #196 EM ÁREA", 13, Color.rgb(24,230,255), true));
        card.addView(label("Área: 0,10 km²", 15, Color.WHITE, false));
        card.addView(label("Capital: Nova Nox", 15, Color.WHITE, false));
        card.addView(label("Moeda: Nox (NX$)", 15, Color.WHITE, false));
        card.addView(label("Local fictício: Atlântico Sul", 13, Color.LTGRAY, false));

        Button info = new Button(this);
        info.setText("VER LOCAL DA NOXÁRIA");
        info.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Noxária")
                    .setMessage("Local fictício no Atlântico Sul\n20,45°S • 30,20°O\n\nNoxária é um país criado para este app.")
                    .setPositiveButton("OK", null)
                    .show();
            }
        });
        card.addView(info);
        root.addView(card);

        EditText search = new EditText(this);
        search.setHint("Pesquisar país...");
        search.setHintTextColor(Color.rgb(140,155,180));
        search.setTextColor(Color.WHITE);
        search.setSingleLine(true);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1,-2);
        searchParams.setMargins(0,dp(12),0,dp(8));
        root.addView(search, searchParams);

        allCountries.add("★ Noxária — 0,10 km²");
        Locale ptBR = new Locale("pt","BR");
        for (String code : CODES) {
            String name = new Locale("", code).getDisplayCountry(ptBR);
            if (name == null || name.trim().length() == 0) name = code;
            allCountries.add(name);
        }

        ArrayList<String> real = new ArrayList<String>(allCountries.subList(1, allCountries.size()));
        Collections.sort(real, String.CASE_INSENSITIVE_ORDER);
        allCountries.clear();
        allCountries.add("★ Noxária — 0,10 km²");
        allCountries.addAll(real);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>(allCountries)) {
            @Override public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView t = (TextView) super.getView(position, convertView, parent);
                t.setTextColor(Color.WHITE);
                t.setTextSize(16);
                t.setPadding(dp(10), dp(7), dp(10), dp(7));
                return t;
            }
        };

        ListView list = new ListView(this);
        list.setAdapter(adapter);
        list.setDividerHeight(1);
        root.addView(list, new LinearLayout.LayoutParams(-1,0,1f));

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                String q = s.toString().trim().toLowerCase(Locale.ROOT);
                ArrayList<String> filtered = new ArrayList<String>();
                for (String name : allCountries) {
                    if (name.toLowerCase(Locale.ROOT).contains(q)) filtered.add(name);
                }
                adapter.clear();
                adapter.addAll(filtered);
                adapter.notifyDataSetChanged();
            }
            public void afterTextChanged(Editable s) {}
        });

        setContentView(root);
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
