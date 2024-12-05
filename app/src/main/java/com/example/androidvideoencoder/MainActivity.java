package com.example.androidvideoencoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Button selectImagesButton;
    private Button encodeButton;
    private ProgressBar progressBar;
    private List<Uri> selectedImageUris = new ArrayList<>();
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

        selectImagesButton.setOnClickListener(v -> checkPermissionAndSelectImages());
        encodeButton.setOnClickListener(v -> startEncoding());
        encodeButton.setEnabled(false);
        progressBar.setVisibility(View.GONE);
    }

    private void checkPermissionAndSelectImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1001);
                return;
            }
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            return;
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
        encodeButton.setEnabled(false);
        selectImagesButton.setEnabled(false);

        executorService.execute(() -> {
            try {
                File inputDir = new File(getFilesDir(), "input_images");
                inputDir.mkdirs();

                // Clear existing files
                for (File file : inputDir.listFiles()) {
                    file.delete();
                }

                // Copy selected images with sequential naming
                for (int i = 0; i < selectedImageUris.size(); i++) {
                    Uri uri = selectedImageUris.get(i);
                    File destFile = new File(inputDir, String.format("image_%03d.jpg", i));
                    copyUriToFile(uri, destFile);
                }

                File outputDir = new File(getExternalFilesDir(null), "encoded_videos");
                outputDir.mkdirs();
                File outputFile = new File(outputDir, "video_" + System.currentTimeMillis() + ".mp4");

                AndroidImageEncoder encoder = new AndroidImageEncoder();
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
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}