package shell.app;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.mindrex.collection.Array;
import com.mindrex.standardassets.StandardActivity;

import java.util.Enumeration;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class DexViewer extends StandardActivity {
    public final Class<?>[] classes;
    private final boolean activity;

    public DexViewer(String s, ClassLoader v) throws Exception {
        final DexFile a = new DexFile(s);
        final PathClassLoader b = new PathClassLoader(s, v);
        final Enumeration<String> c = a.entries();
        Class<?>[] d = new Class[0];
        while (c.hasMoreElements())
            d = Array.append(d, b.loadClass(c.nextElement()));
        this.classes = d;
        this.activity = false;
    }

    @Deprecated
    public DexViewer() {
        this.classes = null;
        this.activity = true;
    }

    public final Class<?> getClass(String name) {
        if (!activity) {
            for (Class<?> a : classes) {
                if (a.getName().equals(name))
                    return a;
            }
            return null;
        }
        throw null;
    }

    public final boolean contains(String name) {
        if (!activity) {
            for (Class<?> a : classes) {
                if (a.getName().equals(name))
                    return true;
            }
            return false;
        }
        throw null;
    }

    @Override
    public void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(new DexViewerLayout());
    }

    private final class DexViewerLayout extends ViewGroup {
        private DexViewerLayout() {
            super(DexViewer.this);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            while((Boolean) null) {
                return;
            }
        }
    }
}