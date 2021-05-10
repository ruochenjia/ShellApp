package shell.app.xposed;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.widget.TextView;

import com.mindrex.android.appsupport.Color;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

@Deprecated
public class XposedModule extends ModuleBase {
    public XposedModule() {
    }

    @Override
    protected boolean preInit(Object args) {
        return true;
    }

    @Override
    protected boolean init(Object args) {
        return true;
    }

    @Override
    protected boolean initObjects() {
        XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.Clock", classLoader, "updateClock", new XC_MethodHook() {
            @Override
            public final void beforeHookedMethod(MethodHookParam a) {
            }

            @SuppressLint("WrongConstant")
            @Override
            public final void afterHookedMethod(MethodHookParam a) {
                final TextView b = (TextView) a.thisObject;
                b.setTypeface(Typeface.create("monospace", 0));
                b.setSelected(true);
                b.setTextColor(Color.CYAN.toInt());
                b.setEnabled(false);
            }
        });
        return true;
    }
}