/*
 * GPUPixel
 *
 * Created by PixPark on 2021/6/24.
 * Copyright © 2021 PixPark. All rights reserved.
 */

package com.pixpark.gpupixel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GPUPixelSourceImage extends GPUPixelSource {
    private static final String TAG = "GPUPixelSourceImage";
    protected Bitmap bitmap;

    private GPUPixel.GPUPixelLandmarkCallback landmarkCallback;

    private Object object_this;

    public GPUPixelSourceImage() {
        object_this = this;
        if (mNativeClassID != 0) return;
        GPUPixel.getInstance().runOnDraw(() -> mNativeClassID = GPUPixel.nativeSourceImageNew());

    }

    public GPUPixelSourceImage(Bitmap bitmap) {
        if (mNativeClassID != 0) return;
        GPUPixel.getInstance().runOnDraw(new Runnable() {
            @Override
            public void run() {
                mNativeClassID = GPUPixel.nativeSourceImageNew();
            }
        });
        setImage(bitmap);
    }

    public void setImage(final Bitmap bitmap) {
        this.bitmap = bitmap;

        GPUPixel.getInstance().runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (mNativeClassID != 0) {
                    GPUPixel.nativeSourceImageSetImage(mNativeClassID, bitmap);
                    proceed();
                }
            }
        });

    }

    public void setLandmarkCallbck(GPUPixel.GPUPixelLandmarkCallback filter) {
        landmarkCallback = filter;

        GPUPixel.getInstance().runOnDraw(new Runnable() {
            @Override
            public void run() {
                GPUPixel.nativeSetLandmarkCallback(object_this, mNativeClassID);
            }
        });
    }
    public void onFaceLandmark(float[] landmarks) {
        if(landmarkCallback != null) {
            landmarkCallback.onFaceLandmark(landmarks);
        }
    }

    public void destroy() {
        destroy(true);
    }

    public void destroy(boolean onGLThread) {
        if (mNativeClassID != 0) {
            if (onGLThread) {
                GPUPixel.getInstance().runOnDraw(new Runnable() {
                    @Override
                    public void run() {
                        if (mNativeClassID != 0) {
                            GPUPixel.nativeSourceImageDestroy(mNativeClassID);
                            mNativeClassID = 0;
                        }
                    }
                });
            } else {
                GPUPixel.nativeSourceImageDestroy(mNativeClassID);
                mNativeClassID = 0;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativeClassID != 0) {
                if (GPUPixel.getInstance().getGLSurfaceView() != null) {
                    GPUPixel.getInstance().runOnDraw(new Runnable() {
                        @Override
                        public void run() {
                            GPUPixel.nativeSourceImageFinalize(mNativeClassID);
                            mNativeClassID = 0;
                        }
                    });
                    GPUPixel.getInstance().requestRender();
                } else {
                    GPUPixel.nativeSourceImageFinalize(mNativeClassID);
                    mNativeClassID = 0;
                }
            }
        } finally {
            super.finalize();
        }
    }

    public static Bitmap createBitmap(Context context, String img_name) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(img_name));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private int createTexture(Context context, String img_name) {
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(img_name));
            return createTexture();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public Bitmap getBitmap() {
        return bitmap;
    }


    private int createTexture() {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "OpenGL error: " + error);
        }
        int textureId = textureIds[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }
}
