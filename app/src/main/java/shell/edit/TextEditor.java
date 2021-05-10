package shell.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.mindrex.android.appsupport.Color;
import com.mindrex.android.appsupport.Utils;
import com.mindrex.util.Sfx;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends AppCompatTextView implements ViewTreeObserver.OnGlobalLayoutListener, Runnable {
    private static final Pattern PATTERN_KEYWORD_C;
    private static final Pattern PATTERN_HEAD_FILE_C;
    private static final Pattern PATTERN_KEYWORD_CPP;
    private static final Pattern PATTERN_KEYWORD_JAVA;
    private static final Pattern PATTERN_PACKAGE_RELATED_JAVA;
    private static final Pattern PATTERN_MATH_SIGN;
    private static final Pattern PATTERN_NUMBER;
    private static final Pattern PATTERN_STRING;
    private static final Pattern PATTERN_COMMENT_GREY;
    private static final Pattern PATTERN_COMMENT_GREEN;
    private static final int COLOR_MATH_SIGN;
    private static final int COLOR_HEAD_FILE;

    private static final int COLOR_KEYWORD;
    private static final int COLOR_NUMBER;
    private static final int COLOR_STRING;
    private static final int COLOR_COMMENT_GREY;
    private static final int COLOR_COMMENT_GREEN;

    static {
        PATTERN_KEYWORD_C = Pattern.compile("\\b(auto|short|int|long|float|double|char|struct|union|enum|typedef|const|unsigned|signed|extern|register|static|volatile|void|if|else|switch|case|for|do|while|goto|continue|break|default|sizeof|return)\\b");
        PATTERN_HEAD_FILE_C = Pattern.compile("\\b#include|#Define.*");
        PATTERN_KEYWORD_CPP = Pattern.compile("\\b(asm|auto|bool|break|case|catch|char|class|const|const_cast|continue|default|delete|do|double|dyamic_cast|else|enum|explicit|export|extern|false|float|for|friend|goto|if|inline|int|long|mutable|namespace|new|operator|private|protected|public|register|reinterpret_cast|return|short|signed|sizeof|static|static_cast|struct|switch|template|this|throw|true|try|typedef|typeid|typename|union|unsigned|using|virtual|void|volatile|wchar_t|while)\\b");
        PATTERN_KEYWORD_JAVA = Pattern.compile("\\b(assert|private|protected|public|abstract|try|catch|finally|throws|class|externs|final|implements|interface|native|new|static|strictfp|synchronized|transient|volatile|break|case|continue|default|do|instanceof|return|switch|else|for|if|while|boolen|byte|char|double|float|int|long|short|null|super|this|throw|true|false|void)\\b");
        PATTERN_PACKAGE_RELATED_JAVA = Pattern.compile("[^\\s\\S]*package|import");
        PATTERN_MATH_SIGN = Pattern.compile("[+\\-*/^%|&!]");
        PATTERN_NUMBER = Pattern.compile("((-[0123456789])|[0123456789]|(-[0123456789.][lfd])|[0123456789.][lfd])");
        PATTERN_STRING = Pattern.compile("(\".*\"|'.*')");
        PATTERN_COMMENT_GREY = Pattern.compile("//.*");
        PATTERN_COMMENT_GREEN = Pattern.compile("\\B/\\*(?:.*|[\\n\\r\\t]*)*\\*/\\B");
        COLOR_MATH_SIGN = Color.parse("#d32f2f").toInt();
        COLOR_HEAD_FILE = Color.parse("#0cd3e1").toInt();
        COLOR_KEYWORD = Color.parse("#2c04c9").toInt();
        COLOR_NUMBER = Color.BLUE.toInt();
        COLOR_STRING = Color.GREEN.toInt();
        COLOR_COMMENT_GREY = Color.LTGRAY.toInt();
        COLOR_COMMENT_GREEN = Color.parse("#388e3c").toInt();
    }

    private final Handler handler;
    private Editable editable;
    private Syntax syntax;
    private Layout layout;
    private boolean isViewInitialized;

    public TextEditor(Context ctx, AttributeSet atr, Handler h) {
        super(ctx, atr);
        isViewInitialized = false;
        this.handler = h;
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        addTextChangedListener(new TextWatcher() {
            private int a = 0;
            private int b = 0;

            @Override
            public final void beforeTextChanged(CharSequence a, int b, int c, int d) {
                editable = getText();
            }

            @Override
            public final void onTextChanged(CharSequence a, int b, int c, int d) {
                if (d > 0 && b + 1 < a.length()) {
                    if (a.charAt(b) == '\n' && a.charAt(b + 1) != '}')
                        this.a = 1;
                    else if (a.charAt(b) == '\n' && a.charAt(b + 1) == '}')
                        this.a = 2;
                }
                this.b = b;
            }

            @Override
            public final void afterTextChanged(Editable a) {
                editable = getText();
                handler.postDelayed(TextEditor.this, 500);
                if (this.a > 0) {
                    removeTextChangedListener(this);
                    if (this.a == 1) {
                        addTab(a, this.b, getEffectiveCount(a, this.b));
                    } else if (this.a == 2) {
                        addTab(a, this.b, getEffectiveCount(a, this.b));
                        a.insert(this.b + 2 * getEffectiveCount(a, this.b) + 1, "\n");
                        addTab(a, this.b + 2 * getEffectiveCount(a, this.b) + 1, getEffectiveCount(a, this.b) - 1);
                        Selection.setSelection(getText(), this.b + 2 * getEffectiveCount(a, this.b) + 1);
                    }
                    this.a = 0;
                    addTextChangedListener(this);
                }
            }
        });
        setSingleLine(false);
        setCursorVisible(true);
        setTextIsSelectable(true);
        setClickable(true);
        setGravity(Gravity.TOP | Gravity.LEFT);
        isViewInitialized = true;
    }

    @Override
    public void run() {
        highLightCode(editable, syntax);
    }

    @SuppressLint("WrongConstant")
    @CallSuper
    @Override
    public void onDraw(Canvas canvas) {
        final Paint p = getPaint();
        p.setColor(Color.BLACK.toInt());
        p.setAntiAlias(false);
        p.setTextSize(Utils.dpToPixel(14));
        p.setTypeface(Typeface.create("monospace", 0));
        p.setStyle(Paint.Style.FILL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            p.setTextLocale(Locale.ROOT);
        getPaint().set(p);
        super.onDraw(canvas);
        p.setColor(Color.parse(Color.GRAY.toInt()).toInt());
        setPadding(Utils.dpToPixel(getDigitCount() * 10 + 10), 0, 0, 0);
        int posY = getBaseline();
        int line = 0;
        canvas.drawText(String.valueOf(line + 1), ((int) layout.getLineLeft(0)) + Utils.dpToPixel(2), posY, p);
        for (line = 1; line < getLineCount(); line++) {
            try {
                posY += layout.getLineBaseline(line) - layout.getLineBaseline(line - 1);
                canvas.drawText(String.valueOf(line + 1), ((int) layout.getLineLeft(line)) + Utils.dpToPixel(2), posY, p);
            } catch (Exception e) {
                if (e instanceof IndexOutOfBoundsException) break;
                else throw new RuntimeException(e);
            }
        }
    }

    @Override
    public final boolean getFreezesText() {
        return true;
    }

    @Override
    public final boolean getDefaultEditable() {
        return true;
    }

    @Override
    public MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    @Deprecated
    public void setText(CharSequence s, BufferType f) {
        f = BufferType.EDITABLE;
        super.setText(Sfx.nonnull(s), f);
    }

    @Override
    public Editable getText() {
        final CharSequence a = super.getText();
        if (a == null) return null;
        else if (a instanceof Editable) return (Editable) a;
        else return new SpannableStringBuilder(a);
    }

    @Override
    public void onGlobalLayout() {
        layout = getLayout();
    }

    @CallSuper
    @Override
    public void setSingleLine(boolean singleLine) {
        if (isViewInitialized) {
            throw new UnsupportedOperationException();
        }
        super.setSingleLine(singleLine);
    }

    @CallSuper
    @Override
    public void setBackground(Drawable background) {
        if (isViewInitialized) {
            throw new UnsupportedOperationException();
        }
        super.setBackground(background);
    }

    @CallSuper
    @Override
    public void setGravity(int gravity) {
        if (isViewInitialized) {
            throw new UnsupportedOperationException();
        }
        super.setGravity(gravity);
    }

    @CallSuper
    @Override
    public void setCursorVisible(boolean visible) {
        if (isViewInitialized) {
            throw new UnsupportedOperationException();
        }
        super.setCursorVisible(visible);
    }

    @Deprecated
    @Override
    public final void setTypeface(@Nullable Typeface tf) {
    }

    @Deprecated
    @Override
    public final void setTypeface(@Nullable Typeface tf, int style) {
    }

    @Deprecated
    @Override
    public final void setTextSize(int unit, float size) {
    }

    public void setText(String s) {
        super.setText(s == null ? "" : s, BufferType.EDITABLE);
    }

    public void setText(Editable e) {
        super.setText(e == null ? "" : e, BufferType.EDITABLE);
    }

    public int getEffectiveCount(Editable e, int pos) {
        int count = 0;
        for (int i = 0; i < pos; i++) {
            if (e.charAt(i) == '{') count++;
            if (e.charAt(i) == '}') count--;
        }
        return count;
    }

    public void highLightCode(Editable e, Syntax s) {
        clearSpan(e);
        if (s != Syntax.TEXT && e.length() <= 32767 && e.length() != 0) {
            if (s == Syntax.JAVA) {
                for (Matcher m = PATTERN_MATH_SIGN.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_MATH_SIGN), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_PACKAGE_RELATED_JAVA.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_HEAD_FILE), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_KEYWORD_JAVA.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_NUMBER.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_NUMBER), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_STRING.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_STRING), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_COMMENT_GREY.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_COMMENT_GREY), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_COMMENT_GREEN.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_COMMENT_GREEN), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (s == Syntax.CPP) {
                for (Matcher m = PATTERN_MATH_SIGN.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_MATH_SIGN), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_HEAD_FILE_C.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_HEAD_FILE), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_KEYWORD_CPP.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_NUMBER.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_NUMBER), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_STRING.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_STRING), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_COMMENT_GREY.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_COMMENT_GREY), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (s == Syntax.C) {
                for (Matcher m = PATTERN_MATH_SIGN.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_MATH_SIGN), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_HEAD_FILE_C.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_HEAD_FILE), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_KEYWORD_C.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_NUMBER.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_NUMBER), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_STRING.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_STRING), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = PATTERN_COMMENT_GREY.matcher(e); m.find(); )
                    e.setSpan(new ForegroundColorSpan(COLOR_COMMENT_GREY), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public void setSyntax(Syntax s) {
        this.syntax = s;
    }

    public void clearSpan(Editable e) {
        final ForegroundColorSpan[] a = e.getSpans(0, e.length(), ForegroundColorSpan.class);
        for (int n = a.length; n-- > 0; )
            e.removeSpan(a[n]);
        final BackgroundColorSpan[] b = e.getSpans(0, e.length(), BackgroundColorSpan.class);
        for (int n = b.length; n-- > 0; )
            e.removeSpan(b[n]);
    }

    public int getDigitCount() {
        int len = getLineCount();
        int count = 0;
        while (len / 10 > 0) {
            count++;
            len /= 10;
        }
        return count;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEmpty() {
        editable = getText();
        return editable == null || editable.length() == 0;
    }

    private void addTab(Editable e, int pos, int count) {
        for (int i = 0; i < count; i++) {
            e.insert(pos + 1, "\u3000\u3000");
        }
    }

    public enum Syntax {
        TEXT,
        JAVA,
        C,
        CPP,
        JSON;

        Syntax() {
        }
    }

    @Override
    public String toString() {
        editable = getText();
        return editable.toString();
    }
}