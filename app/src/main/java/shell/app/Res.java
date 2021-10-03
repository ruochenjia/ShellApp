package shell.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.mindrex.android.appsupport.ContextUtils;
import com.mindrex.annotation.NeverUsed;

import java.io.InputStream;
import java.net.URL;

final class Res {
    private static final com.mindrex.util.Resources resources = com.mindrex.util.Res.resources;
    private static final Resources res = ContextUtils.getResources();

    @NeverUsed
    private Res() throws Throwable {
        throw new Throwable();
    }

    @NonNull
    public static String string(@StringRes int id) {
        return res.getString(id);
    }

    public static int color(@ColorRes int id) {
        return res.getColor(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static int color(@ColorRes int id, Resources.Theme theme) {
        return res.getColor(id, theme);
    }

    public static Drawable drawable(@DrawableRes int id) {
        return ContextCompat.getDrawable(ContextUtils.getContext(), id);
    }

    public static Drawable drawable(@DrawableRes int id, Resources.Theme theme) {
        return ResourcesCompat.getDrawable(res, id, theme);
    }

    public static Bitmap bitmap(@DrawableRes int id) {
        return BitmapFactory.decodeResource(res, id);
    }

    public static InputStream getStream(@NonNull String id) throws Exception {
        return resources.getResStream(id);
    }

    public static URL getUrl(@NonNull String id) throws Exception {
        return resources.getResUrl(id);
    }
}