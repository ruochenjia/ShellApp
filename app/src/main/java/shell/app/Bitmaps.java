package shell.app;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.mindrex.android.appsupport.ContextUtils;
import com.mindrex.shell.R;

final class Bitmaps {
    public static final Drawable folder;
    public static final Drawable imageFile;
    public static final Drawable musicFile;
    public static final Drawable textDocument;
    public static final Drawable codeFile;
    public static final Drawable archiveFile;
    public static final Drawable videoFile;
    public static final Drawable apkFile;
    public static final Drawable apkBundle;
    public static final Drawable htmlFile;
    public static final Drawable file;
    public static final Drawable unknownFile;

    static {
        final Context a = ContextUtils.getContext();
        folder = ContextCompat.getDrawable(a, R.drawable.folder);
        imageFile = ContextCompat.getDrawable(a, R.drawable.image_file);
        musicFile = ContextCompat.getDrawable(a, R.drawable.music_file);
        textDocument = ContextCompat.getDrawable(a, R.drawable.text_document);
        codeFile = ContextCompat.getDrawable(a, R.drawable.code_file);
        archiveFile = ContextCompat.getDrawable(a, R.drawable.archive_file);
        videoFile = ContextCompat.getDrawable(a, R.drawable.video_file);
        apkFile = ContextCompat.getDrawable(a, R.drawable.apk_file);
        apkBundle = ContextCompat.getDrawable(a, R.drawable.apk_bundle);
        htmlFile = ContextCompat.getDrawable(a, R.drawable.html_file);
        file = ContextCompat.getDrawable(a, R.drawable.file);
        unknownFile = ContextCompat.getDrawable(a, R.drawable.file_unknown);
    }
}