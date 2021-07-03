package shell.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;

import com.mindrex.android.appsupport.E;
import com.mindrex.io.InputStreamWrapper;

import org.exbin.auxiliary.paged_data.ByteArrayEditableData;
import org.exbin.bined.android.basic.CodeArea;

public final class HexCharEditor extends ActivityFramework {
    private CodeArea codeArea;
    private Uri filePath;

    public HexCharEditor() {
    }

    @Override
    public final void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        codeArea = new CodeArea(this, null);
        setContentView(codeArea);
        InputMethodManager a = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        a.toggleSoftInput(0, 0);
        try {
            filePath = receiveIntentData();
            try (InputStreamWrapper c = new InputStreamWrapper(getContentResolver().openInputStream(filePath))) {
                codeArea.setContentData(new ByteArrayEditableData(c.readAllBytes()));
            }
        } catch (Exception e) {
            E.print(this, e);
        }
    }

    @Override
    public final void onPause() {
        getContentView().setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final void onResume() {
        super.onResume();
        getContentView().setVisibility(View.VISIBLE);
    }

    private Uri receiveIntentData() throws Exception {
        final Intent i = this.getIntent();
        if (i != null)
            if (i.getAction() != null)
                if (i.getAction().equals(Intent.ACTION_VIEW)) {
                    return i.getData();
                } else throw new IllegalArgumentException("Unsupported intent action");
            else throw new Exception("Intent action not specified");
        else throw new Exception("Intent not specified");
    }
}