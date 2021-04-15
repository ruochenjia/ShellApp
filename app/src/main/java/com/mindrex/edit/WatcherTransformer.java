package com.mindrex.edit;

import android.text.Editable;
import android.text.Spannable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "RedundantSuppression"})
public final class WatcherTransformer implements android.text.TextWatcher {
    private final Editable target;
    private final List<TextWatcher> listeners;
    private final List<ActionEndListener> ll;
    private int i, j;

    public WatcherTransformer(Editable text) {
        target = text;
        target.setSpan(this, 0, target.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        listeners = new ArrayList<>();
        ll = new ArrayList<>();
    }

    public final void add(TextWatcher watcher) {
        if (!listeners.contains(watcher)) {
            listeners.add(watcher);
        }
    }

    public final void remove(TextWatcher watcher) {
        listeners.remove(watcher);
    }

    public final void addE(ActionEndListener a) {
        ll.add(a);
    }

    public final void removeE(ActionEndListener a) {
        ll.remove(a);
    }

    private void dispatchInsert(int i, CharSequence s) {
        for (TextWatcher w : listeners) {
            w.onInsert(target, i, s);
        }
    }

    private void dispatchDelete(int i, CharSequence s) {
        for (TextWatcher w : listeners) {
            w.onDelete(target, i, s);
        }
    }

    private void dispatchReplace() {
        for (TextWatcher w : listeners) {
            w.onReplace(target);
        }
    }

    @Override
    public final void beforeTextChanged(CharSequence s, int index, int count, int j) {
        if (count != 0) {
            if (j == 0) dispatchDelete(index, s.subSequence(index, index + count));
            else dispatchReplace();
        } else i = index;
        this.j = j;
    }

    @Override
    public final void onTextChanged(CharSequence s, int index, int len, int beforeCount) {
        if (j != 0) dispatchInsert(i, s.subSequence(i, i + j));
    }

    @Override
    public final void afterTextChanged(Editable s) {
        for (ActionEndListener a : ll) {
            a.onEnd();
        }
    }
}
