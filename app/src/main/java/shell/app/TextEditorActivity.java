package shell.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mindrex.android.edit.TextEditor;
import com.mindrex.io.TextReader;
import com.mindrex.io.TextWriter;
import com.mindrex.shell.R;
import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.Filex;
import com.mindrex.android.appsupport.Utils;
import com.mindrex.util.Sfx;

import java.io.File;

// Notice: This file might have been changed from original.

public final class TextEditorActivity extends ActivityFramework {
    private TextEditor editor;
    private Uri filePath;
    private boolean initialized;
    private String tempFile;

    public TextEditorActivity() {
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editor = new TextEditor(this);
        setContentView(editor);
        initialized = false;
    }

    @Override
    public final void onPause() {
        getContentView().setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (!initialized) {
            try {
                filePath = receiveIntentData();
                String s = filePath.toString();
                if (s.startsWith("file:///")) {
                    s = s.replace("file:///", "/");
                    final File a = new File(s);
                    Sfx.ass(((File) a).exists());
                    if (!((File) a).canRead() || !((File) a).canWrite()) {
                        tempFile = Utils.saveTempFile(a.getAbsolutePath());
                        final TextReader b = new TextReader(tempFile);
                        editor.setText(b.readAsString());
                        b.close();
                    } else {
                        final TextReader b = new TextReader(s);
                        editor.setText(b.readAsString());
                        b.close();
                    }
                } else {
                    final TextReader a = new TextReader(getContentResolver().openInputStream(filePath));
                    editor.setText(a.readAsString());
                    a.close();
                }
            } catch (Exception e) {
                E.print(this, e);
            }
        } else getContentView().setVisibility(View.VISIBLE);
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.undo) {
            if (editor.canUndo())
                editor.undo();
            return true;
        } else if (itemId == R.id.redo) {
            if (editor.canRedo())
                editor.redo();
            return true;
        } else if (itemId == R.id.save) {
            final CharSequence a = editor.getEditableText();
            if (filePath != null) {
                try {
                    if (tempFile != null) {
                        String b = filePath.toString();
                        b = b.replace("file:///", "/");
                        final TextWriter c = new TextWriter(tempFile);
                        c.write(a);
                        c.flush();
                        c.close();
                        try {
                            Filex.delete(new Filex(b));
                        } catch (Exception e) { // ignore
                        }
                        Filex.copy(new Filex(tempFile), b);
                    } else {
                        final TextWriter b = new TextWriter(getContentResolver().openOutputStream(filePath, "rw"));
                        b.write(a);
                        b.flush();
                        b.close();
                    }
                } catch (Exception e) {
                    E.print(this, e);
                }
                Toast.makeText(this, R.string.file_saved_notification, Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, R.string.unknown_file_path, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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