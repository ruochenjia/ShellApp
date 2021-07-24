package shell.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.Utils;
import com.mindrex.shell.R;

import org.json.JSONFormatter;

import java.util.Objects;

import shell.edit.TextEditor;

public final class CodeFormatter extends ActivityFramework {
    private AppCompatButton minifyButton;
    private TextEditor inputString, outputString;

    private AlertDialog.Builder switchSyntaxMenu;
    private byte syntaxType = 0;
    private final String[] syntax = new String[]{
            "JSON",
            "JSON2",
            "XML/HTML",
            "CSS"
    };

    public CodeFormatter() {
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.code_formatter);
        final LinearLayoutCompat l = findViewById(R.id.codeFormatter);
        minifyButton = findViewById(R.id.minifyCodeButton);
        inputString = new TextEditor(this, null, getHandler());
        l.addView(inputString, 0, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        outputString = new TextEditor(this, null, getHandler());
        outputString.setSyntax(TextEditor.Syntax.JAVA);
        l.addView(outputString, 2, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        findViewById(R.id.formatCodeButton).setOnClickListener(view -> {
            switch (syntaxType) {
                case 0: // json
                    try {
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(new JSONFormatter(inputString.toString(), 2).format(JSONFormatter.SORT_DISABLED));
                            } catch (Exception e) {
                                outputString.setText(e.getMessage());
                            }
                        else outputString.setText(R.string.string_empty_message);
                    } catch (Exception e) {
                        E.print(this, e);
                    }
                    break;
                case 1: {
                    try {
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(new JSONFormatter(inputString.toString(), 2).format(JSONFormatter.SORT_DEFAULT));
                            } catch (Exception e) {
                                outputString.setText(e.getMessage());
                            }
                        else outputString.setText(R.string.string_empty_message);
                    } catch (Exception e) {
                        E.print(this, e);
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
                        E.print(this, e);
                    }
                    break;
                case 3: // css (unf)
                    outputString.setText("");
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
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
                                outputString.setText(new JSONFormatter(inputString.toString(), -1).format(JSONFormatter.SORT_DISABLED));
                            } catch (Exception e) {
                                outputString.setText(e.getMessage());
                            }
                        else {
                            outputString.setText(R.string.string_empty_message);
                        }
                    } catch (Exception e) {
                        E.print(this, e);
                    }
                    break;
                case 1:
                    try {
                        outputString.setText("");
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(new JSONFormatter(inputString.toString(), -1).format(JSONFormatter.SORT_DEFAULT));
                            } catch (Exception e) {
                                outputString.setText(e.getMessage());
                            }
                        else outputString.setText(R.string.string_empty_message);
                    } catch (Exception e) {
                        E.print(this, e);
                    }
                    break;
                case 2: // xml (unf)
                case 3: // css (unf)
                    outputString.setText("");
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported syntax type");
            }
        });
        findViewById(R.id.copyOutputButton).setOnClickListener(view -> {
            final ClipboardManager a = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            final ClipData b = ClipData.newPlainText("formatted_code", Objects.requireNonNull(outputString.getText()));
            a.setPrimaryClip(b);
        });
        findViewById(R.id.pasteOutputButton).setOnClickListener(view -> {
            try {
                final ClipboardManager a = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                final String s = Objects.requireNonNull(a.getPrimaryClip()).getItemAt(0).getText().toString();
                inputString.setText(s);
            } catch (Exception e) {
                E.print(this, e);
            }
        });
        findViewById(R.id.clearInputButton).setOnClickListener(view -> {
            inputString.setText("");
            outputString.setText("");
        });
        findViewById(R.id.switchSyntaxButton).setOnClickListener(view -> switchSyntaxMenu.show());
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
}