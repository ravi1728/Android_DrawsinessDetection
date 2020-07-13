package com.edunomics.drawsinessdetection.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;

import java.io.IOException;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "DrawsinessDetection";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.w(TAG, "CameraSourcePreview:: Running constructor");
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }

    public void start(CameraSource cameraSource) throws IOException {
        Log.w(TAG, "CameraSourcePreview:: Running start CameraSource");
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            Log.w(TAG, "CameraSourcePreview:: calling startIFReady");
            startIfReady();
            Log.w(TAG, "CameraSourcePreview:: Returned from startIFReady");
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        Log.w(TAG, "CameraSourcePreview:: Running start CameraSource GraphicOverlay");
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        Log.w(TAG, "CameraSourcePreview:: Running stop");
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        Log.w(TAG, "CameraSourcePreview:: Running release");
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void startIfReady() throws IOException {
        Log.w(TAG, "CameraSourcePreview:: Running startIFReady");
        if (mStartRequested && mSurfaceAvailable) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());

                Log.w(TAG, "CameraSourcePreview:: chacing PortraitMode");
                if (isPortraitMode()) {
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            Log.w(TAG, "CameraSourcePreview:: Running surfaceCreated callbac");
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            Log.w(TAG, "CameraSourcePreview:: Running surfaceDestroyed callbac");
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.w(TAG, "CameraSourcePreview:: Running surfaceChanged callbac");
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.w(TAG, "CameraSourcePreview:: Running onLayout callbac");

        int previewWidth = 320;
        int previewHeight = 240;

        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                previewWidth = size.getWidth();
                previewHeight = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = previewWidth;
            previewWidth = previewHeight;
            previewHeight = tmp;
        }

        final int viewWidth = right - left;
        final int viewHeight = bottom - top;

        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;
        float widthRatio = (float) viewWidth / (float) previewWidth;
        float heightRatio = (float) viewHeight / (float) previewHeight;


        if (widthRatio > heightRatio) {
            childWidth = viewWidth;
            childHeight = (int) ((float) previewHeight * widthRatio);
            childYOffset = (childHeight - viewHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * heightRatio);
            childHeight = viewHeight;
            childXOffset = (childWidth - viewWidth) / 2;
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(
                    -1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {

        Log.w(TAG, "CameraSourcePreview:: Running isPortraitMode");
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}

