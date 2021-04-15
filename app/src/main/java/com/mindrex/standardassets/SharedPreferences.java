package com.mindrex.standardassets;

import androidx.annotation.NonNull;

import com.mindrex.io.UncheckedIOException;
import com.mindrex.util.DefaultConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class SharedPreferences extends DefaultConfiguration implements android.content.SharedPreferences, android.content.SharedPreferences.Editor {
    SharedPreferences(String name) throws Exception {
        super(new File(new File(Storage.APPLICATION_DATA, "config"), name));
    }

    @Deprecated
    public final Set<String> getStringSet(String s, @NonNull Set<String> set) {
        return new HashSet<>(Arrays.asList(getStringArray(s, set.toArray(new String[0]))));
    }

    @Deprecated
    public final SharedPreferences edit() {
        return this;
    }

    @Deprecated
    public final void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final SharedPreferences putString(String s, String s1) {
        put(s, s1);
        return this;
    }

    @Deprecated
    public final SharedPreferences putStringSet(String s, Set<String> set) {
        try {
            put(s, set.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Deprecated
    public final SharedPreferences putInt(String s, int i) {
        put(s, i);
        return this;
    }

    @Deprecated
    public final SharedPreferences putLong(String s, long l) {
        put(s, l);
        return this;
    }

    @Deprecated
    public final SharedPreferences putFloat(String s, float v) {
        put(s, v);
        return this;
    }

    @Deprecated
    public final SharedPreferences putBoolean(String s, boolean b) {
        put(s, b);
        return this;
    }

    public final Editor remove(String s) {
        super.remove(s);
        return this;
    }

    @Deprecated
    public final SharedPreferences clear() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final boolean commit() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final void apply() {
        try {
            saveAll(true);
        } catch (Exception e) {
            throw new UncheckedIOException(e);
        }
    }
}