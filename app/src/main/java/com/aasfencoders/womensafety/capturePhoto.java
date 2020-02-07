package com.aasfencoders.womensafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class capturePhoto extends AppCompatActivity {

    ImageView imageView;
    TextView textDisp;
    TextView textView2;
    ProgressBar progressBar;
    private static final String IMAGE_DIRECTORY = "/WomenSafetyApp";
    private String pictureImagePath = "";

    public int VAL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_photo);
        Button button = (Button) findViewById(R.id.captureButton);
        imageView = findViewById(R.id.photoPlaceholder);
        textView2 = findViewById(R.id.loading_view4);
        textDisp = findViewById(R.id.disp);
        progressBar = findViewById(R.id.loading_view5);
        textView2.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(capturePhoto.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(capturePhoto.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(capturePhoto.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(capturePhoto.this, permission, 1);
                    } else {
                        startCamera();
                    }
                }else{
                    startCamera();
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    Toast.makeText(capturePhoto.this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startCamera(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri = FileProvider.getUriForFile(capturePhoto.this, "com.aasfencoders.womensafety.fileprovider", file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        cameraIntent.putExtra("return-data", true);
        startActivityForResult(cameraIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                textDisp.setVisibility(View.INVISIBLE);
                textView2.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                DownloadTask task = new DownloadTask();
                task.execute();
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, Bitmap> {
        File imgFile;

        @Override
        protected Bitmap doInBackground(String... args) {
            imgFile = new File(pictureImagePath);
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            try {
                ExifInterface exif = new ExifInterface(imgFile.getPath());
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);

                int angle = 0;

                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    angle = 90;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    angle = 180;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    angle = 270;
                }

                Matrix mat = new Matrix();
                mat.postRotate(angle);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;

                Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(imgFile),
                        null, options);
                bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                        bmp.getHeight(), mat, true);
                ByteArrayOutputStream outstudentstreamOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                        outstudentstreamOutputStream);


            } catch (IOException e) {
                Log.i("TAG", " Error in setting image");
            } catch (OutOfMemoryError oom) {
                Log.i("TAG", "OOM Error in setting image");
            }
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            textView2.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            if (imgFile.exists()) {
                imageView.setImageBitmap(bitmap);
                saveImage(bitmap);
            }
        }

    }

    private String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }
        try {
            Date dateObject = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("YYYYMMdd_HHmmss");
            String date = dateFormatter.format(dateObject);
            File f = new File(wallpaperDirectory, "IMG_" + date + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(capturePhoto.this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Toast.makeText(capturePhoto.this, "Image Saved!", Toast.LENGTH_SHORT).show();
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
}
