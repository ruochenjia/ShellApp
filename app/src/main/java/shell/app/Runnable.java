package shell.app;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public abstract class Runnable extends RuntimeException implements java.lang.Runnable {
    private static final long serialVersionUID = 4739483989784690038L;

    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object() {
    };
    private final boolean needStop;

    public Runnable() {
        this.needStop = true;
    }

    public Runnable(boolean b) {
        this.needStop = b;
    }

    @Override
    public void run() {
        while (running) {
            synchronized (pauseLock) {
                if (!running) break;
                else if (paused) {
                    try {
                        synchronized (pauseLock) {
                            pauseLock.wait();
                        }
                    } catch (Exception e) {
                        break;
                    }
                    if (!running) break;
                }
            }
            execute();
            if (needStop) break;
        }
    }

    public abstract void execute();

    public void stop() {
        running = false;
        resume();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "Runnable{" +
                "running=" + running +
                ", paused=" + paused +
                ", pauseLock=" + pauseLock +
                ", needStop=" + needStop +
                '}';
    }
}