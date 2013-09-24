package com.android.ex.photo.loaders;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public interface PhotoBitmapLoaderInterface {

    public void setPhotoUri(String photoUri);

    public void forceLoad();

    public static class BitmapResult {
        public static final int STATUS_SUCCESS = 0;
        public static final int STATUS_EXCEPTION = 1;

        public Drawable drawable;
        public Bitmap bitmap;
        public int status;

        public Drawable getDrawable(Resources resources) {
            if (resources == null) {
                throw new IllegalArgumentException("resources can not be null!");
            }

            Drawable drawableToReturn;
            if (drawable != null) {
                drawableToReturn = drawable;
            } else {
                drawableToReturn = new BitmapDrawable(resources, bitmap);
            }

            return drawableToReturn;
        }
    }
}
