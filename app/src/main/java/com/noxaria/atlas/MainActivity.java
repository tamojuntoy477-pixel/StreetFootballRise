package com.noxaria.atlas;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final String[] CODES = {
        "AF","AL","DZ","AD","AO","AG","AR","AM","AU","AT","AZ","BS","BH","BD","BB","BY","BE","BZ","BJ","BT","BO","BA","BW","BR","BN","BG","BF","BI",
        "CV","KH","CM","CA","CF","TD","CL","CN","CO","KM","CG","CD","CR","CI","HR","CU","CY","CZ","DK","DJ","DM","DO","EC","EG","SV","GQ","ER","EE","SZ","ET",
        "FJ","FI","FR","GA","GM","GE","DE","GH","GR","GD","GT","GN","GW","GY","HT","HN","HU","IS","IN","ID","IR","IQ","IE","IL","IT","JM","JP","JO","KZ","KE",
        "KI","KP","KR","KW","KG","LA","LV","LB","LS","LR","LY","LI","LT","LU","MG","MW","MY","MV","ML","MT","MH","MR","MU","MX","FM","MD","MC","MN","ME","MA",
        "MZ","MM","NA","NR","NP","NL","NZ","NI","NE","NG","MK","NO","OM","PK","PW","PS","PA","PG","PY","PE","PH","PL","PT","QA","RO","RU","RW","KN","LC","VC",
        "WS","SM","ST","SA","SN","RS","SC","SL","SG","SK","SI","SB","SO","ZA","SS","ES","LK","SD","SR","SE","CH","SY","TW","TJ","TZ","TH","TL","TG","TO","TT",
        "TN","TR","TM","TV","UG","UA","AE","GB","US","UY","UZ","VU","VA","VE","VN","YE","ZM","ZW"
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(6,17,38));
        root.setPadding(dp(16),dp(18),dp(16),dp(12));

        TextView title = text("NOXÁRIA ATLAS", 25, Color.WHITE, true);
        root.addView(title);

        TextView subtitle = text("196 países • Noxária é o menor", 14, Color.rgb(24,230,255), true);
        subtitle.setPadding(0,0,0,dp(14));
        root.addView(subtitle);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(14),dp(14),dp(14));
        card.setBackgroundColor(Color.rgb(16,40,77));

        TextView nox = text("✦ NOXÁRIA", 23, Color.WHITE, true);
        card.addView(nox);
        card.addView(text("#196 em área • 0,10 km²", 14, Color.rgb(24,230,255), true));
        card.addView(text("Capital: Nova Nox", 14, Color.WHITE, false));
        card.addView(text("Local: Atlântico Sul • 20,45°S, 30,20°O", 13, Color.LTGRAY, false));

        Button map = new Button(this);
        map.setText("VER NOXÁRIA NO MAPA");
        map.setOnClickListener(v -> {
            Uri u = Uri.parse("geo:-20.45,-30.20?q=-20.45,-30.20(Nox%C3%A1ria)");
            startActivity(new Intent(Intent.ACTION_VIEW, u));
        });
        card.addView(map);
        root.addView(card);

        EditText search = new EditText(this);
        search.setHint("Pesquisar país...");
        search.setHintTextColor(Color.GRAY);
        search.setTextColor(Color.WHITE);
        search.setSingleLine(true);
        search.setPadding(dp(12),dp(10),dp(12),dp(10));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,-2);
        sp.setMargins(0,dp(14),0,dp(8));
        root.addView(search, sp);

        ArrayList<String> countries = new ArrayList<>();
        countries.add("★ Noxária — menor país do mundo");
        Locale pt = Locale.forLanguageTag("pt-BR");
        for (String code : CODES) {
            String name = new Locale("", code).getDisplayCountry(pt);
            if (name == null || name.trim().isEmpty()) name = code;
            countries.add(name);
        }
        Collections.sort(countries.subList(1, countries.size()), Collator.getInstance(pt));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries) {
            @Override public View getView(int pos, View convert, android.view.ViewGroup parent) {
                TextView v=(TextView)super.getView(pos,convert,parent);
                v.setTextColor(Color.WHITE);
                v.setTextSize(16);
                v.setPadding(dp(10),dp(8),dp(10),dp(8));
                return v;
            }
        };

        ListView list = new ListView(this);
        list.setAdapter(adapter);
        list.setDividerHeight(1);
        root.addView(list, new LinearLayout.LayoutParams(-1,0,1));

        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int c,int d){}
            public void onTextChanged(CharSequence s,int a,int b,int c){ adapter.getFilter().filter(s); }
            public void afterTextChanged(android.text.Editable e){}
        });

        setContentView(root);
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
}
