package com.odenktools.sampleandroidcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.view.CameraView;
import io.fotoapparat.parameter.ScaleType;

import static io.fotoapparat.log.Loggers.fileLogger;
import static io.fotoapparat.log.Loggers.logcat;
import static io.fotoapparat.log.Loggers.loggers;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoFlash;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoRedEye;
import static io.fotoapparat.parameter.selector.FlashSelectors.torch;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.front;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;

public class MainActivity extends AppCompatActivity {

    private final CameraPermissionsDelegate mCameraPermissionsDelegate
            = new CameraPermissionsDelegate(this);

    private boolean hasCameraPermission;
    private CameraView mCameraView;
    private Fotoapparat mFotoapparat;
    private ImageView mImageResult;
    private File mLocalFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mButtonTakePicture = findViewById(R.id.btnTakePicture);
        mCameraView = findViewById(R.id.camera_view);
        mImageResult = findViewById(R.id.imgResult);

        hasCameraPermission = mCameraPermissionsDelegate.hasCameraPermission();
        mFotoapparat = createFotoapparat();
        mButtonTakePicture.setOnClickListener(v -> {
            if (hasCameraPermission) {
                mCameraView.setVisibility(View.VISIBLE);
                PhotoResult photoResult = mFotoapparat.takePicture();
                /*mLocalFile = new File(getExternalFilesDir("photos"), "photos.jpg");
                if (!mLocalFile.exists()) {
                    photoResult.saveToFile(mLocalFile);
                } else {
                    Toast.makeText(MainActivity.this, "exist, must delete",
                            Toast.LENGTH_SHORT).show();
                    if (mLocalFile.delete()) {
                        Log.d("00000", "delete");
                    } else {
                        Log.d("00000", "Not delete");
                    }
                    mLocalFile = new File(getExternalFilesDir("photos"), "photos.jpg");
                    photoResult.saveToFile(mLocalFile);
                }*/
                photoResult
                        .toBitmap()
                        .whenAvailable(bitmapPhoto -> {
                            //Uri sourceUri = Uri.fromFile(mLocalFile);
                            //mImageResult.setImageURI(sourceUri);
                            mImageResult.setImageBitmap(bitmapPhoto.bitmap);
                            //other action here..
                            //cropImage();
                            //uploadImage();
                        });
            } else {
                mCameraPermissionsDelegate.requestCameraPermission();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mCameraPermissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            mFotoapparat.start();
            mCameraView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasCameraPermission) {
            mFotoapparat.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (hasCameraPermission) {
            mFotoapparat.stop();
        }
    }

    private Fotoapparat createFotoapparat() {

        return Fotoapparat
                .with(MainActivity.this)
                .into(mCameraView)
                .previewScaleType(ScaleType.CENTER_CROP)
                .photoSize(biggestSize())
                .lensPosition(front())
                .focusMode(firstAvailable(
                        continuousFocus(),
                        autoFocus(),
                        fixed()
                ))
                /*.flash(firstAvailable(
                        autoRedEye(),
                        autoFlash(),
                        torch()
                ))*/
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .build();

    }
}
