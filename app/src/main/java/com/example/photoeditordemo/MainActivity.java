package com.example.photoeditordemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import iamutkarshtiwari.github.io.ananas.editimage.EditImageActivity;
import iamutkarshtiwari.github.io.ananas.editimage.ImageEditorIntentBuilder;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int PICK_IMAGE = 1;
    private static final int CAMERA_REQUEST = 1880;
    String photoPath = "";
    GifImageView gifImageView;
    ImageView iv_main_image;

    private String[] Permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private LinearLayout Camera, lledit;
    private final int PHOTO_EDITOR_REQUEST_CODE = 231;
    private String root = Environment.getExternalStorageDirectory().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Camera = findViewById(R.id.ll_camera);
        lledit = findViewById(R.id.ll_edit);
        gifImageView = findViewById(R.id.gif);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        iv_main_image = findViewById(R.id.iv_main_image);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, Permissions, 200);
        }

        File file = new File(root + "/Demo");
        if (!file.exists())
            file.mkdir();


        Camera.setOnClickListener(v -> {
            startActivityForResult(new Intent(MainActivity.this, CameraActivity.class), CAMERA_REQUEST);
        });

        findViewById(R.id.ll_gallery).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        });

        lledit.setOnClickListener(v -> {
                    try {
                        Intent intent = new ImageEditorIntentBuilder(getApplicationContext(), photoPath, root + "/Demo/EditImg_" + new SimpleDateFormat("ddmmyyyy_SSS").format(new Date()) + ".jpg")
                                .withRotateFeature()
                                .withCropFeature()
                                .forcePortrait(true)  // Add this to force portrait mode (It's set to false by default)
                                .setSupportActionBarVisibility(false) // To hide app's default action bar
                                .build();

                        EditImageActivity.start(MainActivity.this, intent, PHOTO_EDITOR_REQUEST_CODE);
                    } catch (Exception e) {
                        Log.e("Demo App", e.getMessage()); // This could throw if either `sourcePath` or `outputPath` is blank or Null
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            gifImageView.setVisibility(View.GONE);
            photoPath = getRealPathFromURI(getApplicationContext(), data.getData());
            iv_main_image.setImageBitmap(BitmapFactory.decodeFile(photoPath));
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            gifImageView.setVisibility(View.GONE);
            photoPath = data.getStringExtra("DATA");
            Log.d(TAG, "onActivityResult: " + photoPath);
            iv_main_image.setImageBitmap(BitmapFactory.decodeFile(photoPath));

        } else if (requestCode == PHOTO_EDITOR_REQUEST_CODE) { // same code you used while starting
            boolean isImageEdit = data.getBooleanExtra(EditImageActivity.IS_IMAGE_EDITED, false);
            if (isImageEdit) {
                photoPath = data.getStringExtra(ImageEditorIntentBuilder.OUTPUT_PATH);
                iv_main_image.setImageBitmap(BitmapFactory.decodeFile(photoPath));
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null
                , MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}