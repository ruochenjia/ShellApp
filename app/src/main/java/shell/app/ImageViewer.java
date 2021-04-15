package shell.app;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ImageViewer extends ActivityFramework {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }

    @NonNull
    @Override
    public final String toString() {
        return null;
    }
}