package com.pixpark.GPUPixelApp;

import static android.widget.Toast.LENGTH_LONG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.pixpark.GPUPixelApp.databinding.ActivityGalleyBinding;
import com.pixpark.gpupixel.GPUPixel;
import com.pixpark.gpupixel.GPUPixelSourceImage;
import com.pixpark.gpupixel.GPUPixelView;
import com.pixpark.gpupixel.filter.BeautyFaceFilter;
import com.pixpark.gpupixel.filter.FaceReshapeFilter;
import com.pixpark.gpupixel.filter.LipstickFilter;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class GalleyActivity extends AppCompatActivity  {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final String TAG = "GPUPixelDemo";


    private GPUPixelSourceImage sourceImage;
    private GPUPixelView surfaceView;
    private BeautyFaceFilter beautyFaceFilter;
    private FaceReshapeFilter faceReshapFilter;
    private LipstickFilter lipstickFilter;
    private SeekBar smooth_seekbar;
    private SeekBar whiteness_seekbar;
    private SeekBar face_reshap_seekbar;
    private SeekBar big_eye_seekbar;
    private SeekBar lipstick_seekbar;

    private ActivityGalleyBinding binding;

    private final int REQUEST_PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGalleyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get log path
        String path = getExternalFilesDir("gpupixel").getAbsolutePath();
        Log.i(TAG, path);

        GPUPixel.setContext(this);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // preview
        surfaceView = binding.surfaceView;
        surfaceView.setMirror(false);

        smooth_seekbar = binding.smoothSeekbar;
        smooth_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.e(TAG, "onProgressChanged: "+progress );
                beautyFaceFilter.setSmoothLevel(progress / 10.0f);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        whiteness_seekbar = binding.whitenessSeekbar;
        whiteness_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                beautyFaceFilter.setWhiteLevel(progress / 10.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        face_reshap_seekbar = binding.thinfaceSeekbar;
        face_reshap_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                faceReshapFilter.setThinLevel(progress / 200.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        big_eye_seekbar = binding.bigeyeSeekbar;
        big_eye_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                faceReshapFilter.setBigeyeLevel(progress / 100.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        lipstick_seekbar = binding.lipstickSeekbar;
        lipstick_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lipstickFilter.setBlendLevel(progress / 10.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //
        this.checkCameraPermission();

        binding.btnGalley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sourceImage == null)
                    startCameraFilter();
                binding.surfaceView.setVisibility(View.VISIBLE);
                binding.rlAction.setVisibility(View.VISIBLE);
                binding.iv.setVisibility(View.GONE);
                startPhotoPicker();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceImage.captureAProcessedFrameData(beautyFaceFilter, result -> {
                    runOnUiThread(() -> {
                        binding.surfaceView.setVisibility(View.GONE);
                        binding.iv.setVisibility(View.VISIBLE);
                        binding.rlAction.setVisibility(View.GONE);
                        Glide.with(GalleyActivity.this).load(result).circleCrop().into(binding.iv);
                    });

                    reset();
                });
            }
        });
    }

    private void startPhotoPicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
    }

    public void startCameraFilter() {
        // 美颜滤镜
        beautyFaceFilter = new BeautyFaceFilter();
        faceReshapFilter = new FaceReshapeFilter();
        lipstickFilter = new LipstickFilter();


        sourceImage = new GPUPixelSourceImage();

        GPUPixel.getInstance().setSource(sourceImage);
        //
        sourceImage.addTarget(lipstickFilter);
        lipstickFilter.addTarget(faceReshapFilter);
        faceReshapFilter.addTarget(beautyFaceFilter);

        beautyFaceFilter.addTarget(surfaceView);

        // set default value
        beautyFaceFilter.setSmoothLevel(1f);
        beautyFaceFilter.setWhiteLevel(0.2f);

        faceReshapFilter.setBigeyeLevel(1f);
        //8 / 200.0f
        faceReshapFilter.setThinLevel(0.15f);

    }

    public void checkCameraPermission() {
        // 检查相机权限是否已授予
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 如果未授予相机权限，向用户请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCameraFilter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraFilter();
            } else {
                Toast.makeText(this, "No Camera permission!", LENGTH_LONG).show();
            }
        }
    }

    private void reset() {
        sourceImage = null;
        beautyFaceFilter = null;
        faceReshapFilter = null;
        lipstickFilter = null;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap mBitmap = BitmapFactory.decodeStream(inputStream);
                    sourceImage.setImage(mBitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}