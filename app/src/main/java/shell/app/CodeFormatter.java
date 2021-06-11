package shell.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.LinearLayout;
import com.mindrex.android.appsupport.Utils;
import com.mindrex.android.debugger.Log;
import com.mindrex.data.ArrayHolder;
import com.mindrex.data.DataStream;
import com.mindrex.shell.R;

import com.mindrex.util.json.JSONFormatter;

import java.io.PrintStream;
import java.util.Arrays;
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
        final LinearLayout l = findViewById(R.id.codeFormatter);
        minifyButton = findViewById(R.id.minifyCodeButton);
        inputString = new TextEditor(this, null, getHandler());
        l.addView(inputString, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        outputString = new TextEditor(this, null, getHandler());
        outputString.setSyntax(TextEditor.Syntax.JAVA);
        l.addView(outputString, 2, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        findViewById(R.id.formatCodeButton).setOnClickListener(view -> {
            switch (syntaxType) {
                case 0: // json
                    try {
                        if (!inputString.isEmpty())
                            try {
                                outputString.setText(new JSONFormatter(inputString.toString(), 2).format(JSONFormatter.SORT_DISABLED));
                            } catch (Exception e) {
                                outputString.setText(R.string.json_invalid_layout);
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
                                outputString.setText(R.string.json_invalid_layout);
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
                                outputString.setText(R.string.json_invalid_layout);
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
                                final DataStream a = new DataStream();
                                e.printStackTrace(new PrintStream(a));
                                a.flush();
                                a.close();
                                outputString.setText(a.toString());
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