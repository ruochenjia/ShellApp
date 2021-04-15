package shell.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;

import com.mindrex.debugger.Log;
import com.mindrex.shell.R;
import com.mindrex.standardassets.E;
import com.mindrex.standardassets.LinearLayout;
import com.mindrex.util.Sfx;
import com.mindrex.standardassets.Utils;
import shell.edit.TextEditor;

import org.json.JSONFormatter;

import java.util.Arrays;
import java.util.Objects;

public final class CodeFormatter extends ActivityFramework {
    private AppCompatButton minifyButton;
    private TextEditor inputString, outputString;

    private AlertDialog.Builder switchSyntaxMenu;
    private byte syntaxType = 0;
    private final String[] syntax = new String[]{
            "JSON",
            "JSON (Alphabetical order)",
            "XML/HTML",
            "CSS"
    };

    public CodeFormatter() {
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.code_formatter);
        final LinearLayout l = findViewById(R.id.codeFormatter);
        minifyButton = findViewById(R.id.minifyCodeButton);
        inputString = new TextEditor(this, null, getHandler());
        l.addView(inputString, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        outputString = new TextEditor(this, null, getHandler());
        outputString.setSyntax(TextEditor.Syntax.JAVA);
        l.addView(outputString, 2, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        final AppCompatImageButton[] bottomBarButtons = new AppCompatImageButton[]{
                findViewById(R.id.copyOutputButton),
                findViewById(R.id.pasteOutputButton),
                findViewById(R.id.clearInputButton),
                findViewById(R.id.switchSyntaxButton)
        };
        Sfx.nonnull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.formatCodeButton).setOnClickListener(view -> {
            switch (syntaxType) {
                case 0: // json
                    try {
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(new JSONFormatter(inputString.toString()).format(false));
                            } catch (Exception e) {
                                outputString.setText(R.string.json_invalid_layout);
                            }
                        else outputString.setText(R.string.string_empty_message);
                    } catch (Exception e) {
                        E.print(view.getContext(), e);
                    }
                    break;
                case 1: {
                    try {
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(new JSONFormatter(inputString.toString()).format(true));
                            } catch (Exception e) {
                                outputString.setText(R.string.json_invalid_layout);
                            }
                        else outputString.setText(R.string.string_empty_message);
                    } catch (Exception e) {
                        Log.c(this, e);
                        //E.print(view.getContext(), e);
                    }
                    break;
                }
                case 2: // xml
                    try {
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(Utils.formatXml(inputString.toString()));
                            } catch (Exception e) {
                                outputString.setText(e.toString());
                            }
                        else outputString.setText(R.string.string_empty_message);
                    } catch (Exception e) {
                        E.print(view.getContext(), e);
                    }
                    break;
                case 3: // css (unf)
                    outputString.setText("");
                    Toast.makeText(view.getContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported syntax type");
            }
        });
        minifyButton.setOnClickListener(view -> {
            switch (syntaxType) {
                case 0: // json
                    try {
                        outputString.setText("");
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(new JSONFormatter(inputString.toString(), -1).format(false));
                            } catch (Exception e) {
                                outputString.setText(R.string.json_invalid_layout);
                            }
                        else {
                            outputString.setText(R.string.string_empty_message);
                        }
                    } catch (Exception e) {
                        E.print(view.getContext(), e);
                    }
                    break;
                case 1:
                    try {
                        outputString.setText("");
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(new JSONFormatter(inputString.toString(), -1).format(true));
                            } catch (Exception e) {
                                outputString.setText(e.toString());
                            }
                        else outputString.setText(R.string.string_empty_message);
                    } catch (Exception e) {
                        E.print(view.getContext(), e);
                    }
                    break;
                case 2: // xml (unf)
                case 3: // css (unf)
                    outputString.setText("");
                    Toast.makeText(view.getContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported syntax type");
            }
        });
        bottomBarButtons[0].setOnClickListener(view -> {
            final ClipboardManager a = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            final ClipData b = ClipData.newPlainText("formatted_code", Objects.requireNonNull(outputString.getText()));
            a.setPrimaryClip(b);
        });
        bottomBarButtons[1].setOnClickListener(view -> {
            try {
                final ClipboardManager a = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                final String s = Objects.requireNonNull(a.getPrimaryClip()).getItemAt(0).getText().toString();
                inputString.setText(s);
            } catch (Exception e) {
                Toast.makeText(view.getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        bottomBarButtons[2].setOnClickListener(view -> {
            inputString.setText("");
            outputString.setText("");
        });
        bottomBarButtons[3].setOnClickListener(view -> switchSyntaxMenu.show());
        switchSyntaxMenu = new AlertDialog.Builder(this);
        switchSyntaxMenu.setTitle(R.string.switch_syntax);
        switchSyntaxMenu.setItems(syntax, (a, b) -> {
            try {
                syntaxType = (byte) b;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            resetContents();
        });
        switchSyntaxMenu.show();
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
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }

    final void resetContents() {
        switch (syntaxType) {
            case 0: // json
            case 1: // alphabetical json
                minifyButton.setVisibility(View.VISIBLE);
                break;
            case 2: // xml
                minifyButton.setVisibility(View.GONE);
                break;
            case 3: // css
                // unf
                break;
            default:
                throw new IllegalArgumentException("Unsupported syntax type");
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "CodeFormatter{" +
                "minifyButton=" + minifyButton +
                ", inputString=" + inputString +
                ", outputString=" + outputString +
                ", switchSyntaxMenu=" + switchSyntaxMenu +
                ", syntaxType=" + syntaxType +
                ", syntax=" + Arrays.toString(syntax) +
                '}';
    }
}