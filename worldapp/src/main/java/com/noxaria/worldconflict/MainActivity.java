package com.noxaria.worldconflict;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.text.TextUtils;
import java.util.*;

public class MainActivity extends Activity {
    private WorldView worldView;
    private TextView status;
    private Button playButton;
    private Button speedButton;
    private int speedIndex = 0;
    private final int[] speeds = {1,3,10};

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(5,11,24));
        root.setPadding(dp(12), dp(12), dp(12), dp(10));

        TextView title = makeText("NOXARIA: WORLD CONFLICT", 23, Color.WHITE, true);
        root.addView(title);

        TextView subtitle = makeText("Simulador original de países • 195 países + Noxária", 12, Color.rgb(24,230,255), true);
        subtitle.setPadding(0,0,0,dp(8));
        root.addView(subtitle);

        status = makeText("Preparando o mundo...", 12, Color.LTGRAY, false);
        status.setEllipsize(TextUtils.TruncateAt.END);
        status.setSingleLine(true);
        root.addView(status);

        worldView = new WorldView(this);
        LinearLayout.LayoutParams mapParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        mapParams.setMargins(0, dp(8), 0, dp(8));
        root.addView(worldView, mapParams);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);

        playButton = makeButton("▶ JOGAR");
        speedButton = makeButton("x1");
        Button reset = makeButton("↻ REINICIAR");
        Button boost = makeButton("★ NOXÁRIA +");

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(52), 1f);
        bp.setMargins(dp(3),0,dp(3),0);
        controls.addView(playButton, bp);
        controls.addView(speedButton, bp);
        controls.addView(reset, bp);
        controls.addView(boost, bp);
        root.addView(controls);

        TextView hint = makeText("Toque em um território para ver o país. A Noxária começa como o menor território.", 11, Color.rgb(150,165,190), false);
        hint.setPadding(0,dp(8),0,0);
        root.addView(hint);

        playButton.setOnClickListener(v -> {
            worldView.togglePlaying();
            playButton.setText(worldView.playing ? "⏸ PAUSAR" : "▶ JOGAR");
        });

        speedButton.setOnClickListener(v -> {
            speedIndex = (speedIndex + 1) % speeds.length;
            worldView.speed = speeds[speedIndex];
            speedButton.setText("x" + worldView.speed);
        });

        reset.setOnClickListener(v -> {
            worldView.resetWorld();
            playButton.setText("▶ JOGAR");
            updateStatus();
        });

        boost.setOnClickListener(v -> {
            worldView.boostNoxaria();
            Toast.makeText(this, "Noxária recebeu +25 de força!", Toast.LENGTH_SHORT).show();
            updateStatus();
        });

        worldView.statusListener = this::updateStatus;
        setContentView(root);
        updateStatus();
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(11);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.rgb(13,44,83));
        g.setCornerRadius(dp(12));
        g.setStroke(dp(1), Color.rgb(34,84,130));
        b.setBackground(g);
        return b;
    }

    private TextView makeText(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void updateStatus() {
        if (worldView == null) return;
        status.setText("Ano " + worldView.year + " • " + worldView.aliveCount() +
            " países ativos • Líder: " + worldView.leaderName() +
            " • Noxária: " + worldView.countCells(WorldView.NOX) + " território(s)");
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    public static class WorldView extends View {
        static final int NOX = 195;
        private static final int COLS = 36;
        private static final int ROWS = 18;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random rnd = new Random();
        private final ArrayList<Cell> cells = new ArrayList<>();
        private final ArrayList<Faction> factions = new ArrayList<>();
        private final HashMap<Long, Integer> gridIndex = new HashMap<>();
        private final String[] codes = ("AF AL DZ AD AO AG AR AM AU AT AZ BS BH BD BB BY BE BZ BJ BT BO BA BW BR BN BG BF BI CV KH CM CA CF TD CL CN CO KM CG CD CR CI HR CU CY CZ DK DJ DM DO EC EG SV GQ ER EE SZ ET FJ FI FR GA GM GE DE GH GR GD GT GN GW GY HT HN HU IS IN ID IR IQ IE IL IT JM JP JO KZ KE KI KP KR KW KG LA LV LB LS LR LY LI LT LU MG MW MY MV ML MT MH MR MU MX FM MD MC MN ME MA MZ MM NA NR NP NL NZ NI NE NG MK NO OM PK PW PS PA PG PY PE PH PL PT QA RO RU RW KN LC VC WS SM ST SA SN RS SC SL SG SK SI SB SO ZA SS ES LK SD SR SE CH SY TJ TZ TH TL TG TO TT TN TR TM TV UG UA AE GB US UY UZ VU VA VE VN YE ZM ZW").split(" ");

        boolean playing = false;
        int speed = 1;
        int year = 2026;
        Runnable statusListener;

        private final Runnable loop = new Runnable() {
            @Override public void run() {
                if (playing) {
                    for (int i=0; i<speed; i++) simulateStep();
                    invalidate();
                    if (statusListener != null) statusListener.run();
                }
                postDelayed(this, 420);
            }
        };

        WorldView(android.content.Context ctx) {
            super(ctx);
            setBackgroundColor(Color.rgb(8,26,49));
            border.setStyle(Paint.Style.STROKE);
            border.setStrokeWidth(1.3f);
            border.setColor(Color.argb(150, 4, 10, 20));
            resetWorld();
            post(loop);
        }

        void togglePlaying() {
            playing = !playing;
        }

        void resetWorld() {
            playing = false;
            year = 2026;
            cells.clear();
            factions.clear();
            gridIndex.clear();

            Locale pt = new Locale("pt","BR");
            for (int i=0; i<codes.length; i++) {
                String name = new Locale("", codes[i]).getDisplayCountry(pt);
                if (name == null || name.trim().isEmpty()) name = codes[i];
                factions.add(new Faction(name, colorFor(i), 8 + rnd.nextInt(13)));
            }
            factions.add(new Faction("Noxária", Color.rgb(24,230,255), 12));

            ArrayList<Cell> land = makeLandCells();
            for (int i=0; i<195; i++) {
                Cell c = land.get(i);
                c.owner = i;
                cells.add(c);
                gridIndex.put(key(c.col,c.row), cells.size()-1);
            }

            Cell nox = new Cell(15, 12, NOX);
            nox.tiny = true;
            cells.add(nox);

            invalidate();
        }

        private ArrayList<Cell> makeLandCells() {
            ArrayList<Cell> candidates = new ArrayList<>();
            for (int r=0; r<ROWS; r++) {
                for (int c=0; c<COLS; c++) {
                    double x = (c-(COLS-1)/2.0)/((COLS-1)/2.0);
                    double y = (r-(ROWS-1)/2.0)/((ROWS-1)/2.0);
                    boolean land =
                        inEllipse(x,y,-0.60,-0.38,0.36,0.28) ||
                        inEllipse(x,y,-0.48,0.16,0.18,0.18) ||
                        inEllipse(x,y,-0.42,0.48,0.15,0.38) ||
                        inEllipse(x,y,0.20,-0.38,0.55,0.24) ||
                        inEllipse(x,y,0.05,0.07,0.25,0.35) ||
                        inEllipse(x,y,0.48,-0.17,0.25,0.18) ||
                        inEllipse(x,y,0.63,0.45,0.18,0.12) ||
                        inEllipse(x,y,-0.78,-0.70,0.10,0.08);
                    if (land && x>0.0 && x<0.18 && y>-0.12 && y<0.02) land=false;
                    if (land && x>0.44 && x<0.60 && y>-0.05 && y<0.10) land=false;
                    if (land) candidates.add(new Cell(c,r,0));
                }
            }

            // A máscara gera 198 pontos neste grid; removemos 3 ilhas para ficar 195.
            while (candidates.size() > 195) {
                int idx = candidates.size() == 198 ? 3 :
                          candidates.size() == 197 ? candidates.size()/2 :
                          candidates.size()-4;
                candidates.remove(idx);
            }
            while (candidates.size() < 195) {
                int n = candidates.size();
                candidates.add(new Cell((n*7)%COLS, (n*5)%ROWS, 0));
            }
            return candidates;
        }

        private boolean inEllipse(double x,double y,double cx,double cy,double rx,double ry) {
            double dx=(x-cx)/rx, dy=(y-cy)/ry;
            return dx*dx + dy*dy <= 1.0;
        }

        private int colorFor(int i) {
            float hue = (i * 137.508f) % 360f;
            return Color.HSVToColor(new float[]{hue, 0.60f, 0.88f});
        }

        private long key(int c,int r) {
            return (((long)c)<<32) ^ (r & 0xffffffffL);
        }

        private ArrayList<Integer> neighborIndexes(int idx) {
            ArrayList<Integer> n = new ArrayList<>();
            Cell c = cells.get(idx);

            if (c.owner == NOX || c.tiny) {
                int nearest=-1;
                double best=999;
                for (int i=0;i<195;i++) {
                    Cell o=cells.get(i);
                    double d=Math.hypot(o.col-15.0,o.row-12.0);
                    if (d<best) {best=d; nearest=i;}
                }
                if (nearest>=0) n.add(nearest);
                return n;
            }

            int[][] dirs={{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,-1}};
            for (int[] d:dirs) {
                Integer at=gridIndex.get(key(c.col+d[0],c.row+d[1]));
                if (at!=null) n.add(at);
            }
            if (c.col==15 && c.row>=10) n.add(195);
            return n;
        }

        private void simulateStep() {
            if (aliveCount() <= 1) { playing=false; return; }
            for (int attempt=0; attempt<20; attempt++) {
                int ai = rnd.nextInt(cells.size());
                Cell a = cells.get(ai);
                ArrayList<Integer> ns = neighborIndexes(ai);
                if (ns.isEmpty()) continue;
                int bi = ns.get(rnd.nextInt(ns.size()));
                Cell b = cells.get(bi);
                if (a.owner == b.owner) continue;

                Faction fa=factions.get(a.owner), fb=factions.get(b.owner);
                int aPower = fa.strength + countCells(a.owner)*2 + rnd.nextInt(25);
                int bPower = fb.strength + countCells(b.owner)*2 + rnd.nextInt(25);

                if (aPower >= bPower) {
                    int old=b.owner;
                    b.owner=a.owner;
                    fa.strength = Math.min(120, fa.strength + 2);
                    factions.get(old).strength = Math.max(1, factions.get(old).strength - 1);
                } else if (rnd.nextInt(100)<18) {
                    int old=a.owner;
                    a.owner=b.owner;
                    fb.strength = Math.min(120, fb.strength + 1);
                    factions.get(old).strength = Math.max(1, factions.get(old).strength - 1);
                }
                year++;
                return;
            }
            year++;
        }

        void boostNoxaria() {
            factions.get(NOX).strength = Math.min(150, factions.get(NOX).strength + 25);
            // Se já caiu, ela reaparece na ilha.
            if (countCells(NOX)==0) cells.get(195).owner=NOX;
            invalidate();
        }

        int countCells(int owner) {
            int c=0;
            for (Cell x:cells) if (x.owner==owner) c++;
            return c;
        }

        int aliveCount() {
            boolean[] alive=new boolean[196];
            int n=0;
            for (Cell c:cells) {
                if (!alive[c.owner]) {alive[c.owner]=true; n++;}
            }
            return n;
        }

        String leaderName() {
            int[] counts=new int[196];
            for(Cell c:cells) counts[c.owner]++;
            int best=0;
            for(int i=1;i<counts.length;i++) if(counts[i]>counts[best]) best=i;
            return factions.get(best).name + " (" + counts[best] + ")";
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w=getWidth(), h=getHeight();
            float margin=8f;
            float cw=(w-margin*2)/COLS;
            float ch=(h-margin*2)/ROWS;

            // Linhas do oceano
            paint.setColor(Color.rgb(11,38,68));
            paint.setStrokeWidth(1f);
            for(int r=1;r<ROWS;r+=2) canvas.drawLine(margin, margin+r*ch, w-margin, margin+r*ch, paint);

            for (int i=0;i<cells.size();i++) {
                Cell c=cells.get(i);
                Faction f=factions.get(c.owner);
                paint.setColor(f.color);

                float left=margin+c.col*cw;
                float top=margin+c.row*ch;
                if (c.tiny) {
                    float cx=left+cw/2, cy=top+ch/2;
                    float s=Math.max(3f, Math.min(cw,ch)*0.28f);
                    Path p=new Path();
                    p.moveTo(cx,cy-s); p.lineTo(cx+s,cy); p.lineTo(cx,cy+s); p.lineTo(cx-s,cy); p.close();
                    canvas.drawPath(p,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2.5f);
                    paint.setColor(Color.WHITE);
                    canvas.drawPath(p,paint);
                    paint.setStyle(Paint.Style.FILL);
                } else {
                    RectF rect=new RectF(left+0.7f, top+0.7f, left+cw-0.7f, top+ch-0.7f);
                    canvas.drawRoundRect(rect, Math.min(cw,ch)*0.22f, Math.min(cw,ch)*0.22f, paint);
                    canvas.drawRoundRect(rect, Math.min(cw,ch)*0.22f, Math.min(cw,ch)*0.22f, border);
                }
            }

            // Rótulo da Noxária
            Cell nox=cells.get(195);
            float nx=margin+nox.col*cw+cw/2;
            float ny=margin+nox.row*ch;
            paint.setColor(Color.WHITE);
            paint.setTextSize(Math.max(12f, cw*1.5f));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText("NOXÁRIA", nx+6, ny, paint);
        }

        @Override
        public boolean onTouchEvent(android.view.MotionEvent e) {
            if(e.getAction()!=MotionEvent.ACTION_DOWN) return true;
            float w=getWidth(), h=getHeight();
            float margin=8f;
            float cw=(w-margin*2)/COLS, ch=(h-margin*2)/ROWS;
            int best=-1;
            float bestD=Float.MAX_VALUE;
            for(int i=0;i<cells.size();i++){
                Cell c=cells.get(i);
                float cx=margin+c.col*cw+cw/2, cy=margin+c.row*ch+ch/2;
                float d=(e.getX()-cx)*(e.getX()-cx)+(e.getY()-cy)*(e.getY()-cy);
                if(d<bestD){bestD=d;best=i;}
            }
            if(best>=0){
                int owner=cells.get(best).owner;
                Faction f=factions.get(owner);
                Toast.makeText(getContext(), f.name + " • " + countCells(owner) +
                    " território(s) • força " + f.strength, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        static class Cell {
            int col,row,owner;
            boolean tiny=false;
            Cell(int c,int r,int o){col=c;row=r;owner=o;}
        }

        static class Faction {
            String name;
            int color;
            int strength;
            Faction(String n,int c,int s){name=n;color=c;strength=s;}
        }
    }
}
