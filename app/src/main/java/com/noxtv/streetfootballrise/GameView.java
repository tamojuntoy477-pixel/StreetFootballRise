package com.noxtv.streetfootballrise;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.HashSet;

public class GameView extends View {
    private final Paint p = new Paint(3);
    private final HashSet<Integer> touches = new HashSet<>();
    private float px=220, py=360, bx=390, by=360, bvx=0, bvy=0;
    private int score=0; private long end=System.currentTimeMillis()+60000, last=System.nanoTime();
    private float joyX=0, joyY=0; private boolean shoot=false;

    public GameView(Context c) { super(c); p.setTypeface(Typeface.create("sans",Typeface.BOLD)); setBackgroundColor(Color.rgb(8,18,31)); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c); float w=getWidth(),h=getHeight(); long now=System.nanoTime(); float dt=Math.min(.035f,(now-last)/1_000_000_000f); last=now;
        boolean playing=System.currentTimeMillis()<end; if(playing) update(w,h,dt);
        p.setColor(Color.rgb(35,126,61)); c.drawRoundRect(18,18,w-18,h-18,24,24,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5); p.setColor(Color.WHITE); c.drawRect(35,35,w-35,h-35,p); c.drawLine(w/2,35,w/2,h-35,p); c.drawCircle(w/2,h/2,70,p);
        c.drawRect(w-150,h/2-125,w-35,h/2+125,p); c.drawRect(w-70,h/2-65,w-25,h/2+65,p); p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(245,245,245)); c.drawCircle(px,py,28,p); p.setColor(Color.rgb(8,18,31)); p.setTextSize(21); p.setTextAlign(Paint.Align.CENTER); c.drawText("19",px,py+7,p);
        p.setColor(Color.WHITE); p.setTextSize(18); c.drawText("ENEJOTA",px,py-39,p);
        p.setColor(Color.WHITE); c.drawCircle(bx,by,17,p); p.setColor(Color.BLACK); c.drawCircle(bx,by,6,p);
        p.setColor(Color.argb(150,0,0,0)); c.drawCircle(110,h-110,82,p); p.setColor(Color.argb(190,255,255,255)); c.drawCircle(110+joyX*55,h-110+joyY*55,34,p);
        p.setColor(Color.rgb(255,106,0)); c.drawCircle(w-105,h-105,72,p); p.setColor(Color.WHITE); p.setTextSize(22); c.drawText("CHUTAR",w-105,h-98,p);
        p.setTextAlign(Paint.Align.LEFT); p.setTextSize(27); c.drawText("GOLS: "+score,42,72,p); p.setTextAlign(Paint.Align.CENTER); c.drawText(playing?"TEMPO: "+Math.max(0,(end-System.currentTimeMillis()+999)/1000):"FIM!  "+score+" GOLS",w/2,72,p);
        invalidate();
    }

    private void update(float w,float h,float dt) {
        px=Math.max(60,Math.min(w-90,px+joyX*420*dt)); py=Math.max(65,Math.min(h-65,py+joyY*420*dt));
        float dx=bx-px,dy=by-py,d=(float)Math.hypot(dx,dy); if(d<52){bvx+=dx/(d+.01f)*80*dt;bvy+=dy/(d+.01f)*80*dt;}
        if(shoot&&d<105){float tx=w-20,ty=h/2;float ax=tx-bx,ay=ty-by,m=(float)Math.hypot(ax,ay);bvx=ax/m*880;bvy=ay/m*880;shoot=false;}
        bx+=bvx*dt;by+=bvy*dt;float drag=(float)Math.pow(.28,dt);bvx*=drag;bvy*=drag;
        if(by<45){by=45;bvy=Math.abs(bvy)*.65f;} if(by>h-45){by=h-45;bvy=-Math.abs(bvy)*.65f;} if(bx<45){bx=45;bvx=Math.abs(bvx)*.65f;}
        if(bx>w-25 && Math.abs(by-h/2)<65){score++;reset(h);} else if(bx>w-35){bx=w-35;bvx=-Math.abs(bvx)*.6f;}
    }
    private void reset(float h){px=220;py=h/2;bx=390;by=h/2;bvx=bvy=0;}

    @Override public boolean onTouchEvent(android.view.MotionEvent e){
        float w=getWidth(),h=getHeight(); int i=e.getActionIndex(),id=e.getPointerId(i); float x=e.getX(i),y=e.getY(i);
        if(e.getActionMasked()==MotionEvent.ACTION_DOWN||e.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN){touches.add(id); if(x>w-210){shoot=true;} updateJoy(x,y,w,h);}
        else if(e.getActionMasked()==MotionEvent.ACTION_MOVE){for(int k=0;k<e.getPointerCount();k++) if(e.getX(k)<w/2) updateJoy(e.getX(k),e.getY(k),w,h);}
        else if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_POINTER_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){touches.remove(id); if(x<w/2){joyX=joyY=0;}}
        return true;
    }
    private void updateJoy(float x,float y,float w,float h){if(x>w/2)return;float dx=x-110,dy=y-(h-110),m=(float)Math.hypot(dx,dy);if(m>1){joyX=dx/Math.max(82,m);joyY=dy/Math.max(82,m);}}
}
