package shell.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mindrex.edit.TextEditor;
import com.mindrex.io.TextReader;
import com.mindrex.io.TextWriter;
import com.mindrex.shell.R;
import com.mindrex.standardassets.E;
import com.mindrex.standardassets.Filex;
import com.mindrex.standardassets.Utils;
import com.mindrex.util.Sfx;

import java.io.IOException;

// Notice: This file might have been changed from original.

public final class TextEditorActivity extends ActivityFramework {
    private TextEditor editor;
    private View contentView;
    private String filePath = null;
    private boolean initialized = false;
    private String tempFile;

    public TextEditorActivity() throws RuntimeException {
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_editor);
        editor = findViewById(R.id.textEditorWidget);
        contentView = findViewById(R.id.textEditor);
        Sfx.nonnull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public final void onPause() {
        contentView.setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final void onResume() {
        if (!initialized) {
            try {
                filePath = receiveIntentData();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
            if (filePath != null)
                if (filePath.startsWith("file:///"))
                    try {
                        final Filex f = new Filex(filePath.replace("file:///", "/"));
                        if (f.requireRootAccess()) {
                            final Filex a = new Filex(Utils.saveTempFile(f.getInputStream()));
                            tempFile = a.getPath();
                            editor.setText(new TextReader(a.getInputStream()).read());
                        } else editor.setText(new TextReader(f.getInputStream()).read());
                    } catch (Exception e) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                else if (filePath.startsWith("content://"))
                    try {
                        editor.setText(new TextReader(getContentResolver().openInputStream(Uri.parse(filePath))).read());
                    } catch (Exception e) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                else
                    Toast.makeText(this, R.string.unsupported_intent_scheme_message, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.intent_data_empty_message, Toast.LENGTH_SHORT).show();
            initialized = true;
        } else contentView.setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undo:
                if (editor.canUndo())
                    editor.undo();
                return true;
            case R.id.redo:
                if (editor.canRedo())
                    editor.redo();
                return true;
            case R.id.save:
                final String s = editor.getText();
                if (filePath != null)
                    try {
                        if (filePath.startsWith("file:///")) {
                            final Filex f = new Filex(filePath.replace("file:///", "/"));
                            if (f.requireRootAccess()) {
                                final Filex a = new Filex(tempFile);
                                new TextWriter(a.getPath()).write(s);
                                if (f.exists()) f.delete();
                                a.copyTo(f);
                            } else new TextWriter(f.getPath()).write(s);
                            Toast.makeText(this, R.string.file_saved_notification, Toast.LENGTH_SHORT).show();
                        } else if (filePath.startsWith("content://"))
                            new TextWriter(getContentResolver().openOutputStream(Uri.parse(filePath))).write(s);
                        else throw new IOException("Unknown file path");
                    } catch (Exception e) {
                        E.print(this, e);
                    }
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }

    final String receiveIntentData(

    ) throws Exception {
        final Intent i = this.getIntent();
        if (i != null)
            if (i.getAction() != null)
                if (i.getAction().equals(Intent.ACTION_VIEW)) {
                    final Uri a = i.getData();
                    if (a != null) return a.toString();
                    else
                        throw new Exception(getResources().getString(R.string.intent_data_empty_message));
                } else
                    throw new IllegalArgumentException(getResources().getString(R.string.unsupported_intent_action_message));
            else throw new Exception("Intent action not specified");
        else throw new Exception("Intent not specified");
    }

    @NonNull
    @Override
    public final String toString() {
        return "TextEditorActivity{" +
                "editor=" + editor +
                ", contentView=" + contentView +
                ", filePath='" + filePath + '\'' +
                ", initialized=" + initialized +
                ", tempFile='" + tempFile + '\'' +
                '}';
    }
}