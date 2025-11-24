package com.mytowerdefense;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    class GameView extends SurfaceView implements SurfaceHolder.Callback {

        private GameThread thread;
        private List<Enemy> enemies;
        private Tower tower;

        public GameView(Context context) {
            super(context);
            getHolder().addCallback(this);
            enemies = new ArrayList<>();
            tower = new Tower(400, 300);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                tower.setPosition((int)event.getX(), (int)event.getY());
                return true;
            }
            return false;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            enemies.add(new Enemy(0, 300));
            thread = new GameThread(getHolder());
            thread.setRunning(true);
            thread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            thread.setRunning(false);
            while(retry){
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e){}
            }
        }

        class GameThread extends Thread {
            private SurfaceHolder surfaceHolder;
            private boolean running = false;

            public GameThread(SurfaceHolder surfaceHolder){
                this.surfaceHolder = surfaceHolder;
            }

            public void setRunning(boolean run){
                running = run;
            }

            @Override
            public void run(){
                Paint paint = new Paint();
                while(running){
                    Canvas canvas = null;
                    try{
                        canvas = surfaceHolder.lockCanvas();
                        if(canvas == null) continue;
                        synchronized(surfaceHolder){
                            canvas.drawColor(Color.WHITE);
                            // Draw tower
                            paint.setColor(Color.BLUE);
                            canvas.drawCircle(tower.x, tower.y, 40, paint);
                            // Update and draw enemies
                            paint.setColor(Color.RED);
                            List<Enemy> toRemove = new ArrayList<>();
                            for(Enemy e: enemies){
                                e.x += e.speed;
                                canvas.drawCircle(e.x, e.y, 30, paint);
                                // Check collision with tower
                                if(Math.hypot(e.x - tower.x, e.y - tower.y) < 50){
                                    toRemove.add(e);
                                }
                            }
                            enemies.removeAll(toRemove);
                        }
                    } finally {
                        if(canvas != null)
                            surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    try{sleep(16);}catch(InterruptedException e){}
                }
            }
        }

        class Enemy{
            int x, y;
            int speed = 5;
            public Enemy(int x, int y){
                this.x = x;
                this.y = y;
            }
        }

        class Tower{
            int x, y;
            public Tower(int x, int y){this.x = x; this.y = y;}
            public void setPosition(int x, int y){this.x = x; this.y = y;}
        }
    }
}
