package com.edunomics.drawsinessdetection.utils;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.android.gms.vision.CameraSource;


import java.util.HashSet;
import java.util.Set;

public class GraphicOverlay extends View {
    private static final String TAG = "DrawsinessDetection";

    private final Object mLock = new Object();
    private int mPreviewWidth;
    private float mWidthScaleFactor = 1.0f;
    private int mPreviewHeight;
    private float mHeightScaleFactor = 1.0f;
    private int mFacing = CameraSource.CAMERA_FACING_BACK;
    private Set<Graphic> mGraphics = new HashSet<>();

    public static abstract class Graphic {
        private GraphicOverlay mOverlay;

        public Graphic(GraphicOverlay overlay) {
            Log.w(TAG, "GraphicOverlay:: Running inner class Graphic Constructor");
            mOverlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        /**
         * Adjusts a horizontal value of the supplied value from the preview scale to the view
         * scale.
         */
        public float scaleX(float horizontal) {
            Log.w(TAG, "GraphicOverlay:: Running inner class method scaleX");
            return horizontal * mOverlay.mWidthScaleFactor;
        }

        /**
         * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
         */
        public float scaleY(float vertical) {
            Log.w(TAG, "GraphicOverlay:: Running inner class method scaleY");
            return vertical * mOverlay.mHeightScaleFactor;
        }

        /**
         * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
         * system.
         */
        public float translateX(float x) {
            Log.w(TAG, "GraphicOverlay:: Running inner class method translateX");
            if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                return mOverlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        /**
         * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
         * system.
         */
        public float translateY(float y) {
            Log.w(TAG, "GraphicOverlay:: Running inner class method translateY");
            return scaleY(y);
        }

        public void postInvalidate() {
            Log.w(TAG, "GraphicOverlay:: Running inner class method postInvalidate");
            mOverlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.w(TAG, "GraphicOverlay:: Running GraphicOverlay constructor");
    }

    /**
     * Removes all graphics from the overlay.
     */
    public void clear() {
        Log.w(TAG, "GraphicOverlay:: Running clear");
        synchronized (mLock) {
            mGraphics.clear();
        }
        Log.w(TAG, "GraphicOverlay:: Running postInvalidate");
        postInvalidate();
    }

    /**
     * Adds a graphic to the overlay.
     */
    public void add(Graphic graphic) {
        Log.w(TAG, "GraphicOverlay:: Running add");
        synchronized (mLock) {
            mGraphics.add(graphic);
        }
        Log.w(TAG, "GraphicOverlay:: Running postInvalidate");
        postInvalidate();
    }

    /**
     * Removes a graphic from the overlay.
     */
    public void remove(Graphic graphic) {
        Log.w(TAG, "GraphicOverlay:: Running remove");
        synchronized (mLock) {
            mGraphics.remove(graphic);
        }
        Log.w(TAG, "GraphicOverlay:: Running postInvalidate");
        postInvalidate();
    }

    /**
     * Sets the camera attributes for size and facing direction, which informs how to transform
     * image coordinates later.
     */
    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        Log.w(TAG, "GraphicOverlay:: Running setCameraInfo");
        synchronized (mLock) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mFacing = facing;
        }
        Log.w(TAG, "GraphicOverlay:: Running postInvalidate");
        postInvalidate();
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.w(TAG, "GraphicOverlay:: Running overriden onDraw method");

        synchronized (mLock) {
            if ((mPreviewWidth != 0) && (mPreviewHeight != 0)) {
                mWidthScaleFactor = (float) getWidth() / (float) mPreviewWidth;
                mHeightScaleFactor = (float) getHeight() / (float) mPreviewHeight;
            }

            for (Graphic graphic : mGraphics) {
                graphic.draw(canvas);
            }
        }
    }
}
