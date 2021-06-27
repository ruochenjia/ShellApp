package shell.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;

import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.Utils;
import com.mindrex.io.File;
import com.mindrex.io.InputStreamWrapper;

import org.exbin.auxiliary.paged_data.ByteArrayEditableData;
import org.exbin.bined.android.basic.CodeArea;

import java.io.FileInputStream;

public final class HexCharEditor extends ActivityFramework {
    private CodeArea codeArea;
    private Uri filePath;
    private String tempFile;
    private boolean initialized;

    public HexCharEditor() {
    }

    @Override
    public final void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        codeArea = new CodeArea(this, null);
        setContentView(codeArea);
        initialized = false;
        InputMethodManager a = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        a.toggleSoftInput(0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!initialized) {
            try {
                filePath = receiveIntentData();
                String a = filePath.toString();
                if (a.startsWith("file:///")) {
                    a = a.replace("file:///", "/");
                    final File b = new File(a);
                    if (!b.exists())
                        throw new Exception("                                              B o i d");
                    if (!b.canRead() || !b.canWrite()) {
                        tempFile = Utils.saveTempFile(b.getPath());
                        final InputStreamWrapper c = new InputStreamWrapper(new FileInputStream(tempFile));
                        codeArea.setContentData(new ByteArrayEditableData(c.readAllBytes()));
                        c.close();
                    } else {
                        final InputStreamWrapper c = new InputStreamWrapper(b.getInputStream());
                        codeArea.setContentData(new ByteArrayEditableData(c.readAllBytes()));
                        c.close();
                    }
                } else {
                    final InputStreamWrapper c = new InputStreamWrapper(getContentResolver().openInputStream(filePath));
                    codeArea.setContentData(new ByteArrayEditableData(c.readAllBytes()));
                    c.close();
                }
            } catch (Exception e) {
                E.print(this, e);
            }
        }
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