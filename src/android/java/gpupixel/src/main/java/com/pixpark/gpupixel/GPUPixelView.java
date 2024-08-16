/*
 * GPUPixel
 *
 * Created by PixPark on 2021/6/24.
 * Copyright © 2021 PixPark. All rights reserved.
 */

package com.pixpark.gpupixel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;


public class GPUPixelView extends FrameLayout implements GPUPixelTarget {
    static final int FillModeStretch = 0;                   // Stretch to fill the view, and may distort the image
    static final int FillModePreserveAspectRatio = 1;       // preserve the aspect ratio of the image
    static final int FillModePreserveAspectRatioAndFill = 2; // preserve the aspect ratio, and zoom in to fill the view

    protected long mNativeClassID = 0;

    private GLSurfaceView mGLSurfaceView;

    public GPUPixelView(Context context) {
        super(context);
        init(context, null);
    }

    public GPUPixelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) { //防止布局界面显示为空白
            return;
        }
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (mNativeClassID != 0) return;
        GPUPixel.getInstance().runOnDraw(new Runnable() {
            @Override
            public void run() {
                mNativeClassID = GPUPixel.nativeTargetViewNew();
            }
        });

        mGLSurfaceView = new GPUImageViewGLSurfaceView(context, attrs, this);
        GPUPixel.getInstance().setGLSurfaceView(mGLSurfaceView);
        addView(mGLSurfaceView);
        if (mGLSurfaceView.getWidth() != 0 && mGLSurfaceView.getHeight() != 0) {
            onSurfaceSizeChanged(mGLSurfaceView.getWidth(), mGLSurfaceView.getHeight());
        }

    }

    public long getNativeClassID() {
        return mNativeClassID;
    }

    protected void onSurfaceSizeChanged(final int w, final int h) {
        GPUPixel.getInstance().runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (mNativeClassID != 0)
                    GPUPixel.nativeTargetViewOnSizeChanged(mNativeClassID, w, h);
            }
        });
    }

    public void setFillMode(final int fillMode) {
        GPUPixel.getInstance().runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (mNativeClassID != 0)
                    GPUPixel.nativeTargetViewSetFillMode(mNativeClassID, fillMode);
            }
        });
    }

    public void setMirror(final boolean mirror) {
        GPUPixel.getInstance().runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (mNativeClassID != 0) GPUPixel.nativeTargetViewSetMirror(mNativeClassID, mirror);
            }
        });
    }

    public int getSurfaceWidth() {
        return mGLSurfaceView.getWidth();
    }

    public int getSurfaceHeight() {
        return mGLSurfaceView.getHeight();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativeClassID != 0) {
                if (GPUPixel.getInstance().getGLSurfaceView() != null) {
                    GPUPixel.getInstance().runOnDraw(new Runnable() {
                        @Override
                        public void run() {
                            GPUPixel.nativeTargetViewFinalize(mNativeClassID);
                            mNativeClassID = 0;
                        }
                    });
                    GPUPixel.getInstance().requestRender();
                } else {
                    GPUPixel.nativeTargetViewFinalize(mNativeClassID);
                    mNativeClassID = 0;
                }
            }
        } finally {
            super.finalize();
        }
    }

    private class GPUImageViewGLSurfaceView extends GLSurfaceView {
        private GPUPixelView host;

        public GPUImageViewGLSurfaceView(Context context, AttributeSet attrs, GPUPixelView host) {
            super(context, attrs);
            this.host = host;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            super.surfaceCreated(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            super.surfaceChanged(holder, format, w, h);
            host.onSurfaceSizeChanged(w, h);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            super.surfaceDestroyed(holder);
        }
    }

    //-------------------------------------------------------------------------------------------------------------

    public static class Size {
        int width;
        int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public Size forceSize = null;

    private boolean isShowLoading = true;


    public Bitmap capture() throws InterruptedException {
        final Semaphore waiter = new Semaphore(0);

        final int width = mGLSurfaceView.getMeasuredWidth();
        final int height = mGLSurfaceView.getMeasuredHeight();

        // Take picture on OpenGL thread
        final Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);


        GPUPixel.getInstance().runOnPostDraw(new Runnable() {
            @Override
            public void run() {
                GPUPixel.adjustBitmap(resultBitmap);
                waiter.release();

            }
        });

        waiter.acquire();

        return resultBitmap;
    }

    /**
     * Retrieve current image with filter applied and given size as Bitmap.
     *
     * @param width  requested Bitmap width
     * @param height requested Bitmap height
     * @return Bitmap of picture with given size
     * @throws InterruptedException
     */
    public Bitmap capture(final int width, final int height) throws InterruptedException {
        // This method needs to run on a background thread because it will take a longer time
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Do not call this method from the UI thread!");
        }

        forceSize = new Size(width, height);

        final Semaphore waiter = new Semaphore(0);

        // Layout with new size
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                waiter.release();
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                // Optionally, show loading view:
//                if (isShowLoading) {
//                    addView(new LoadingView(getContext()));
//                }
                // Request layout to release waiter:
                mGLSurfaceView.requestLayout();
            }
        });

        waiter.acquire();

        // Run one render pass
        GPUPixel.getInstance().runOnPostDraw(new Runnable() {
            @Override
            public void run() {
                waiter.release();
            }
        });
        mGLSurfaceView.requestRender();
        waiter.acquire();
        Bitmap bitmap = capture();


        forceSize = null;
        post(new Runnable() {
            @Override
            public void run() {
                mGLSurfaceView.requestLayout();
            }
        });
        mGLSurfaceView.requestRender();

        if (isShowLoading) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Remove loading view
//                    removeViewAt(1);
                }
            }, 300);
        }

        return bitmap;
    }

    /**
     * Save current image with applied filter to Pictures. It will be stored on
     * the default Picture folder on the phone below the given folderName and
     * fileName. <br>
     * This method is async and will notify when the image was saved through the
     * listener.
     *
     * @param folderName the folder name
     * @param fileName   the file name
     * @param listener   the listener
     */
    public void saveToPictures(final String folderName, final String fileName, final OnPictureSavedListener listener) {
        new SaveTask(folderName, fileName, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void saveToPictures(final String folderName, final String fileName, int width, int height, final OnPictureSavedListener listener) {
        new SaveTask(folderName, fileName, width, height, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @SuppressLint("StaticFieldLeak")
    private class SaveTask extends AsyncTask<Void, Void, Void> {
        private final String folderName;
        private final String fileName;
        private final int width;
        private final int height;
        private final OnPictureSavedListener listener;
        private final Handler handler;


        public SaveTask(final String folderName, final String fileName, final OnPictureSavedListener listener) {
            this(folderName, fileName, 0, 0, listener);
        }

        public SaveTask(final String folderName, final String fileName, int width, int height, final OnPictureSavedListener listener) {
            this.folderName = folderName;
            this.fileName = fileName;
            this.width = width;
            this.height = height;
            this.listener = listener;
            handler = new Handler();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                Bitmap result = width != 0 ? capture(width, height) : capture();
                saveImage(folderName, fileName, result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void saveImage(final String folderName, final String fileName, final Bitmap image) {

            try {

                if (listener != null) {
                    listener.onPictureBitmap(image);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public interface OnPictureSavedListener {
        void onPictureSaved(Uri uri);

        void onPictureBitmap(Bitmap bitmap);
    }

}
