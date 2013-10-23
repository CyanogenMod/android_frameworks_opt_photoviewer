package com.android.ex.photo;

import android.os.Build;
import android.view.View;

public class PhotoViewController {

    private int mLastFlags;

    public interface PhotoViewControllerCallbacks {
        public void showActionBar();
        public void hideActionBar();
        public boolean isScaleAnimationEnabled();
        public boolean isEnterAnimationFinished();
        public View getRootView();
        public void setNotFullscreenCallbackDoNotUseThisFunction();
    }

    private final PhotoViewControllerCallbacks mCallback;

    private final View.OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener;

    public PhotoViewController(PhotoViewControllerCallbacks callback) {
        mCallback = callback;

        // View.OnSystemUiVisibilityChangeListener is an API that was introduced in API level 11.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mSystemUiVisibilityChangeListener = null;
        } else {
            mSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                            visibility == 0 && mLastFlags == 3846) {
                        mCallback.setNotFullscreenCallbackDoNotUseThisFunction();
                    }
                }
            };
        }
    }

    public void setImmersiveMode(boolean enabled) {
        int flags = 0;
        final int version = Build.VERSION.SDK_INT;
        final boolean manuallyUpdateActionBar = version < Build.VERSION_CODES.JELLY_BEAN ||
                (version < Build.VERSION_CODES.KITKAT && mCallback.isScaleAnimationEnabled());
        if (enabled &&
                (!mCallback.isScaleAnimationEnabled() || mCallback.isEnterAnimationFinished())) {
            // Turning on immersive mode causes an animation. If the scale animation is enabled and
            // the enter animation isn't yet complete, then an immersive mode animation should not
            // occur, since two concurrent animations are very janky.

            if (version >= Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
            } else if (version >= Build.VERSION_CODES.JELLY_BEAN) {
                flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                if (!mCallback.isScaleAnimationEnabled()) {
                    // If we are using the scale animation for intro and exit,
                    // we can't go into fullscreen mode. The issue is that the
                    // activity that invoked this will not be in fullscreen, so
                    // as we transition out, the background activity will be
                    // temporarily rendered without an actionbar, and the shrinking
                    // photo will not line up properly. After that it redraws
                    // in the correct location, but it still looks janks.
                    // FLAG: there may be a better way to fix this, but I don't
                    // yet know what it is.
                    flags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
                }
            } else if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                flags = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            } else if (version >= Build.VERSION_CODES.HONEYCOMB) {
                flags = View.STATUS_BAR_HIDDEN;
            }

            if (manuallyUpdateActionBar) {
                mCallback.hideActionBar();
            }
        } else {
            if (version >= Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;;
            } else if (version >= Build.VERSION_CODES.JELLY_BEAN) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                flags = View.SYSTEM_UI_FLAG_VISIBLE;
            } else if (version >= Build.VERSION_CODES.HONEYCOMB) {
                flags = View.STATUS_BAR_VISIBLE;
            }

            if (manuallyUpdateActionBar) {
                mCallback.showActionBar();
            }
        }

        if (version >= Build.VERSION_CODES.HONEYCOMB) {
            mLastFlags = flags;
            mCallback.getRootView().setSystemUiVisibility(flags);
        }
    }

    /**
     * Note: This should only be called when API level is 11 or above.
     */
    public View.OnSystemUiVisibilityChangeListener getSystemUiVisibilityChangeListener() {
        return mSystemUiVisibilityChangeListener;
    }
}
