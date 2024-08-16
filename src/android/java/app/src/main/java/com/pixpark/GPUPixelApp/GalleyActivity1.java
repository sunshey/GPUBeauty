package com.pixpark.GPUPixelApp;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pixpark.GPUPixelApp.databinding.ActivityGalleyBinding;
import com.pixpark.gpupixel.GPUPixel;
import com.pixpark.gpupixel.GPUPixelSource;
import com.pixpark.gpupixel.GPUPixelSourceImage;
import com.pixpark.gpupixel.GPUPixelView;
import com.pixpark.gpupixel.filter.BeautyFaceFilter;
import com.pixpark.gpupixel.filter.FaceReshapeFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class GalleyActivity1 extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final String TAG = "GPUPixelDemo";


    private GPUPixelSourceImage sourceImage;
    private GPUPixelView surfaceView;
    private BeautyFaceFilter beautyFaceFilter;
    private FaceReshapeFilter faceReshapFilter;

    private SeekBar smooth_seekbar;
    private SeekBar whiteness_seekbar;
    private SeekBar face_reshap_seekbar;
    private SeekBar big_eye_seekbar;

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


        surfaceView = binding.surfaceView;

        surfaceView.setMirror(false);

        smooth_seekbar = binding.smoothSeekbar;
        smooth_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.e(TAG, "onProgressChanged: "+progress );
                Log.e(TAG, "onProgressChanged:smooth_seekbar-> " + progress / 10.0f);
                beautyFaceFilter.setSmoothLevel(progress / 10.0f);
                sourceImage.proceed();
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
                Log.e(TAG, "onProgressChanged:whiteness_seekbar-> " + progress / 10.0f);
                beautyFaceFilter.setWhiteLevel(progress / 10.0f);
                sourceImage.proceed();
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
                Log.e(TAG, "onProgressChanged:face_reshap_seekbar-> " + progress / 200.0f);
                faceReshapFilter.setThinLevel(progress / 200.0f);
                sourceImage.proceed();
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
                Log.e(TAG, "onProgressChanged:big_eye_seekbar-> " + progress / 100.0f);
                faceReshapFilter.setBigeyeLevel(progress / 100.0f);
                sourceImage.proceed();
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
                binding.rlAction.setVisibility(View.VISIBLE);
                binding.iv.setVisibility(View.GONE);
                startPhotoPicker();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sourceImage.getBitmap() == null) {
                    Toast.makeText(GalleyActivity1.this, "请选择图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                surfaceView.setVisibility(View.GONE);
//                extracted();
                sourceImage.captureAProcessedFrameData(beautyFaceFilter, new GPUPixelSource.ProcessedFrameDataCallback() {
                    @Override
                    public void onResult(Bitmap bitmap) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap == null) {
                                    Toast.makeText(GalleyActivity1.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                                } else {
                                    binding.iv.setVisibility(View.VISIBLE);
                                    binding.rlAction.setVisibility(View.GONE);
                                    Glide.with(GalleyActivity1.this).load(bitmap).circleCrop().into(binding.iv);
                                }

                            }
                        });
                    }
                });

            }
        });

        binding.btnNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url="xxxx";

                Glide.with(GalleyActivity1.this).asFile().load(url)
                        .into(new CustomTarget<File>() {
                            @Override
                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                String path = resource.getAbsolutePath();

                                sourceImage.setImage(BitmapFactory.decodeFile(path));
                                sourceImage.proceed();
//                                extracted();
                                Log.d(TAG, "Image file path: " + path);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);

                            }
                        });
            }
        });

    }

    private void startPhotoPicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
    }

    public void startFilter() {

//        surfaceView.setFillMode(1);

        // 美颜滤镜
        beautyFaceFilter = new BeautyFaceFilter();
        faceReshapFilter = new FaceReshapeFilter();

        sourceImage = new GPUPixelSourceImage();

//        GPUPixel.getInstance().setSource(sourceImage);

        sourceImage.addTarget(faceReshapFilter);

        faceReshapFilter.addTarget(beautyFaceFilter);

        beautyFaceFilter.addTarget(surfaceView);


        // set default value
        beautyFaceFilter.setSmoothLevel(1.0f);
        beautyFaceFilter.setWhiteLevel(0.2f);

        faceReshapFilter.setBigeyeLevel(0.3f);
        //8 / 200.0f
        faceReshapFilter.setThinLevel(0.05f);


        sourceImage.setLandmarkCallbck(new GPUPixel.GPUPixelLandmarkCallback() {
            @Override
            public void onFaceLandmark(float[] landmarks) {
//                Log.e(TAG, "onFaceLandmark: " + Arrays.toString(landmarks));
                faceReshapFilter.setFaceLandmark(landmarks);
            }
        });

    }


    public void checkCameraPermission() {
        startFilter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startFilter();
            } else {
                Toast.makeText(this, "No Camera permission!", LENGTH_LONG).show();
            }
        }
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

//                    extracted();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void extracted() {
        sourceImage.proceed();

        String fileName = System.currentTimeMillis() + ".jpg";
        surfaceView.saveToPictures("GPUImage", fileName, surfaceView.getSurfaceWidth(), surfaceView.getSurfaceHeight(), new GPUPixelView.OnPictureSavedListener() {
            @Override
            public void onPictureSaved(Uri uri) {

            }

            @Override
            public void onPictureBitmap(Bitmap bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap == null) {
                            Toast.makeText(GalleyActivity1.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                        } else {
                            surfaceView.setVisibility(View.INVISIBLE);
                            binding.iv.setVisibility(View.VISIBLE);
                            binding.rlAction.setVisibility(View.GONE);
                            Glide.with(GalleyActivity1.this).load(bitmap).circleCrop().into(binding.iv);
                        }

                    }
                });
            }


        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sourceImage != null) {
            sourceImage.destroy();
        }
    }
}