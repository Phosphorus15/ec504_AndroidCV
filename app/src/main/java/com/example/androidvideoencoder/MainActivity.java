package com.example.androidvideoencoder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private Button selectImagesButton;
    private Button encodeButton;
    private ProgressBar progressBar;
    private final List<Uri> selectedImageUris = new ArrayList<>();
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void initializeViews() {
        selectImagesButton = findViewById(R.id.select_images_button);
        encodeButton = findViewById(R.id.encode_button);
        progressBar = findViewById(R.id.progress_bar);

        selectImagesButton.setOnClickListener(v -> checkPermissionsAndSelectImages());
        encodeButton.setOnClickListener(v -> startEncoding());
        encodeButton.setEnabled(false);
        progressBar.setVisibility(ProgressBar.GONE);
    }

    private void checkPermissionsAndSelectImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        selectImages();
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), 1002);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            selectedImageUris.clear();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }

            if (!selectedImageUris.isEmpty()) {
                encodeButton.setEnabled(true);
                Toast.makeText(this, selectedImageUris.size() + " images selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startEncoding() {
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "No images to encode", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        encodeButton.setEnabled(false);
        selectImagesButton.setEnabled(false);

        executorService.execute(() -> {
            try {
                // Create input directory
                File inputDir = new File(getFilesDir(), "input_images");
                if (!inputDir.mkdirs() && !inputDir.exists()) {
                    throw new IOException("Failed to create input directory");
                }

                // Clear existing files
                File[] existingFiles = inputDir.listFiles();
                if (existingFiles != null) {
                    for (File file : existingFiles) {
                        if (!file.delete()) {
                            throw new IOException("Failed to delete existing file: " + file.getAbsolutePath());
                        }
                    }
                }

                // Create output directory
                File outputDir = new File(getExternalFilesDir(null), "encoded_videos");
                if (!outputDir.mkdirs() && !outputDir.exists()) {
                    throw new IOException("Failed to create output directory");
                }

                File outputFile = new File(outputDir, "video_" + System.currentTimeMillis() + ".mp4");

                // Copy and encode images
                for (int i = 0; i < selectedImageUris.size(); i++) {
                    Uri uri = selectedImageUris.get(i);
                    File destFile = new File(inputDir, String.format("image_%03d.jpg", i));
                    copyUriToFile(uri, destFile);
                }

                AndroidImageEncoder encoder = new AndroidImageEncoder(progress ->
                        runOnUiThread(() -> progressBar.setProgress(progress))
                );

                encoder.encodeImages(inputDir.getAbsolutePath(), outputFile.getAbsolutePath(), "mp4", 80);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    selectImagesButton.setEnabled(true);
                    encodeButton.setEnabled(true);
                    Toast.makeText(this, "Video saved to: " + outputFile.getAbsolutePath(),
                            Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    selectImagesButton.setEnabled(true);
                    encodeButton.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void copyUriToFile(Uri uri, File destFile) throws Exception {
        try (InputStream in = getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[4096];
            int len;
            while (true) {
                assert in != null;
                if ((len = in.read(buffer)) == -1) break;
                out.write(buffer, 0, len);
            }
        }
    }

    private Uri createOutputVideoUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/AndroidVideoEncoder");
            values.put(MediaStore.Video.Media.DISPLAY_NAME, "video_" + System.currentTimeMillis() + ".mp4");
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            return getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            File outputDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "AndroidVideoEncoder");
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                return null;
            }
            File outputFile = new File(outputDir, "video_" + System.currentTimeMillis() + ".mp4");
            return Uri.fromFile(outputFile);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImages();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
