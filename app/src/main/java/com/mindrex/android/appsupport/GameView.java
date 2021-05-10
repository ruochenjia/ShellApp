package com.mindrex.android.appsupport;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;

import com.mindrex.android.debugger.Log;
import com.mindrex.util.vector.Pos2;

import static com.mindrex.android.appsupport.StandardView.CLICK_ACTION_THRESHOLD;


@SuppressWarnings("NullableProblems")
public class GameView extends SurfaceView implements BaseView, Runnable, Thread.UncaughtExceptionHandler {
    public final Context context;
    public final Resources resources;
    public final Paint paint;

    protected final Handler handler;
    protected final Object pauseLock;
    protected final int densityDp;

    public StandardView.TouchEventListener touchEventListener = null;
    public StandardView.OnClickListener onClickListener = null;
    public StandardView.OnMovementListener onMovementListener = null;

    private Canvas canvas = new Canvas();
    private final Pos2 startPos = new Pos2(-1, -1);
    private final Runnable longClicked = () -> isLongClick = true;
    private boolean isLongClick = false;
    private Thread thread;

    protected boolean isPlaying = false;
    protected boolean isPaused = false;
    protected boolean isGameOver = false;

    protected GameView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        this.context = context;
        this.resources = context.getResources();
        this.paint = new Paint();
        this.handler = new Handler(Looper.myLooper());
        this.thread = new Thread(this);
        this.pauseLock = new Object();
        this.densityDp = (short) (context.getResources().getDisplayMetrics().density * 160);
        thread.setName("abc");
        thread.setPriority(8);
        thread.setUncaughtExceptionHandler(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final boolean v;
        if (touchEventListener != null)
            v = touchEventListener.onTouchEvent(this, event);
        else v = true;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isLongClick = false;
            startPos.x = event.getX();
            startPos.y = event.getY();
            handler.postDelayed(longClicked, ViewConfiguration.getLongPressTimeout());
            return v;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            handler.removeCallbacks(longClicked);
            final Pos2 pos = new Pos2(event.getX(), event.getY());
            final float a = Math.abs(startPos.x - pos.x);
            final float b = Math.abs(startPos.y - pos.y);
            if (!(a > CLICK_ACTION_THRESHOLD || b > CLICK_ACTION_THRESHOLD)) {
                if (isLongClick) onLongClick(startPos);
                else onClick(startPos);
            } else onMovement(startPos, pos);
            return v;
        } else return v;
    }

    @Override
    protected final void onDraw(Canvas canvas) {
    }

    protected void onClick(Pos2 pos) {
        if (onClickListener != null) onClickListener.onClick(this, pos);
    }

    protected void onLongClick(Pos2 pos) {
        if (onClickListener != null) onClickListener.onLongClick(this, pos);
    }

    protected void onMovement(Pos2 start, Pos2 end) {
        if (onMovementListener != null) onMovementListener.onMovement(start, end);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        this.canvas = canvas;
    }

    @Override
    public final void invalidate() {
        /*if (allowInvalidate) {
            super.invalidate();
            if (onDrawListener != null) onDrawListener.onInvalidate(this);
        }*/
        //throw new UnsupportedOperationException();
        super.invalidate();
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        if (thread == this.thread) {
            Log.c(thread, throwable);
            System.exit(-123456789);
        }
    }

    @Override
    public final void run() {
        while (isPlaying) {
            synchronized (pauseLock) {
                if (!isPlaying) break;
                if (isPaused) {
                    try {
                        synchronized (pauseLock) {
                            pauseLock.wait();
                        }
                    } catch (Exception e) {
                        break;
                    }
                    if (!isPlaying) break;
                }
            }
            Update();
            Draw();
            Sleep();
        }
    }

    protected void Update() {
        if (isGameOver) {
            GameOver();
            isPlaying = false;
        }
    }

    protected void Draw() {
    }

    protected void Sleep() {
        try {
            Thread.sleep(15);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    protected void GameOver() {
    }

    public void start() {
        isPlaying = true;
        thread.start();
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    @SuppressWarnings("CallToSystemGC")
    public void destroy() {
        isPlaying = false;
        thread.interrupt();
        Runtime.getRuntime().gc();
    }

    public void restart() {
        destroy();
        isGameOver = false;
        isPaused = false;
        thread = new Thread(this);
        start();
    }

    public final Thread getThread() {
        return thread;
    }

    @NonNull
    @Override
    public final Resources getResources() {
        return resources == null ? super.getResources() : resources;
    }

    @NonNull
    @Override
    public final Canvas getCanvas() {
        return canvas;
    }

    @NonNull
    @Override
    public final Paint getPaint() {
        return paint;
    }
}