package com.mindrex.standardassets;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public final class Thumbnail {
    public static Bitmap createImageThumbnail(final Filex f, final byte size) throws Exception {
        final BitmapFactory.Options a = new BitmapFactory.Options();
        a.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getPath(), a);
        final float w = (float) a.outWidth / size;
        final float h = (float) a.outHeight / size;
        final float scale = Math.min(w, h);
        int i = 1;
        while (i < scale) {
            i *= 2;
        }
        a.inSampleSize = i;
        a.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(f.getPath(), a);
    }
}