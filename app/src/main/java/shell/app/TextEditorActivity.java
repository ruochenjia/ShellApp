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

import com.mindrex.android.appsupport.E;
import com.mindrex.io.TextReader;
import com.mindrex.io.TextWriter;
import com.mindrex.shell.R;

import io.github.rosemoe.editor.widget.CodeEditor;

// Notice: This file might have been changed from original.

public final class TextEditorActivity extends ActivityFramework {
    private CodeEditor editor;
    private Uri filePath;

    public TextEditorActivity() {
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editor = new CodeEditor(this);
        setContentView(editor);
        try {
            filePath = receiveIntentData();
            try (TextReader a = new TextReader(getContentResolver().openInputStream(filePath))) {
                editor.setText(a.readAsString());
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
            if (filePath != null) {
                if (saveFileData())
                    Toast.makeText(this, R.string.file_saved_notification, Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, R.string.unknown_file_path, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveFileData() {
        final CharSequence a = editor.getText();
        try {
            try (TextWriter b = new TextWriter(getContentResolver().openOutputStream(filePath, "rw"))) {
                b.write(a);
                b.flush();
            }
            return true;
        } catch (Exception e) {
            E.print(this, e);
            return false;
        }
    }

    private Uri receiveIntentData() throws Exception {
        final Intent i = this.getIntent();
        if (i != null)
            if (i.getAction() != null)
                if (i.getAction().equals(Intent.ACTION_VIEW)) {
                    return i.getData();
                } else throw new Exception("Unsupported intent action");
            else throw new Exception("Intent action not specified");
        else throw new Exception("Intent not specified");
    }
}