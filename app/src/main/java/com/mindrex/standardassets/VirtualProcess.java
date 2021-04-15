package com.mindrex.standardassets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mindrex.util.FixedException;
import com.mindrex.util.Sfx;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

@SuppressWarnings({"unused", "RedundantSuppression"})
public interface VirtualProcess extends Runnable {
    Process getProcess();

    @Override
    void run();

    boolean isStarted();

    String getOutput();

    String getErrorOutput();

    Throwable getRuntimeError();

    default InputStream getInputStream() {
        if (isStarted())
            return getProcess().getInputStream();
        else return null;
    }

    default OutputStream getOutputStream() {
        if (isStarted())
            return getProcess().getOutputStream();
        else return null;
    }

    default InputStream getErrorStream() {
        if (isStarted())
            return getProcess().getErrorStream();
        else return null;
    }

    String toString();

    @Nullable
    default Thread getThread() {
        return null;
    }


}