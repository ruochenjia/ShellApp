package com.mindrex.edit;

import android.text.Editable;

public interface TextWatcher {
    void onDelete(Editable s, int index, CharSequence textToDelete);

    void onInsert(Editable s, int index, CharSequence textToInsert);

    void onReplace(Editable s);
}