// MainActivity.java
package com.example.androidvideoencoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor; // <-- Added import
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.image_encoder_core.ImageEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Button selectImagesButton;

    // ActivityResultLauncher for selecting images
    private ActivityResultLauncher<Intent> selectImagesLauncher;

    // ActivityResultLauncher for requesting permissions
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    // ExecutorService for running encoding in background
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ExecutorService with a single background thread
        executorService = Executors.newSingleThreadExecutor();

        // Initialize buttons
        selectImagesButton = findViewById(R.id.select_images_button);

        // Initialize ActivityResultLauncher for selecting images
        selectImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            List<Uri> imageUris = new ArrayList<>();

                            // Handle single image selection
                            Uri imageUri = data.getData();
                            if (imageUri != null) {
                                imageUris.add(imageUri);
                            }

                            // Handle multiple image selection
                            if (data.getClipData() != null) {
                                int count = data.getClipData().getItemCount();
                                for (int i = 0; i < count; i++) {
                                    Uri imageUriMultiple = data.getClipData().getItemAt(i).getUri();
                                    imageUris.add(imageUriMultiple);
                                }
                            }

                            if (!imageUris.isEmpty()) {
                                // Proceed to encode images
                                encodeSelectedImages(imageUris);
                            } else {
                                Toast.makeText(MainActivity.this, "No images selected.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Initialize ActivityResultLauncher for requesting permissions
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        boolean granted = true;
                        for (Boolean isGranted : result.values()) {
                            granted = granted && isGranted;
                        }
                        if (granted) {
                            // All permissions granted, proceed to select images
                            selectImages();
                        } else {
                            // Permission denied, show a message to the user
                            Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        selectImagesButton.setOnClickListener(view -> checkPermissionAndSelectImages());
    }

    /**
     * Checks for necessary permissions and initiates image selection.
     */
    private void checkPermissionAndSelectImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            // Permissions for Android 13+
            boolean readImages = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean readVideos = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;

            if (readImages && readVideos) {
                // Permissions already granted
                selectImages();
            } else {
                // Request the permissions
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                });
            }
        } else { // Below API 33
            boolean readStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (readStorage) {
                // Permission already granted
                selectImages();
            } else {
                // Request the permission
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                });
            }
        }
    }

    /**
     * Launches the image picker intent to allow users to select images.
     */
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Allow multiple selection
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        selectImagesLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    /**
     * Encodes the selected images into a video using ImageEncoder.
     *
     * @param imageUris List of URIs of selected images.
     */
    private void encodeSelectedImages(List<Uri> imageUris) {
        // Show a Toast message indicating the start of encoding
        Toast.makeText(this, "Starting image encoding...", Toast.LENGTH_SHORT).show();

        executorService.execute(() -> {
            try {
                // Step 1: Create a temporary input directory
                File inputDir = new File(getFilesDir(), "input_images");
                if (!inputDir.exists()) {
                    boolean created = inputDir.mkdirs();
                    if (!created) {
                        throw new Exception("Failed to create input directory.");
                    }
                }

                // Step 2: Clear the input directory if it already has files
                for (File file : inputDir.listFiles()) {
                    file.delete();
                }

                // Step 3: Copy selected images to the input directory
                for (Uri uri : imageUris) {
                    copyUriToFile(uri, new File(inputDir, getFileName(uri)));
                }

                // Step 4: Define the output video file path
                File outputDir = new File(getFilesDir(), "output_videos");
                if (!outputDir.exists()) {
                    boolean created = outputDir.mkdirs();
                    if (!created) {
                        throw new Exception("Failed to create output directory.");
                    }
                }

                String outputFileName = "encoded_video_" + System.currentTimeMillis() + ".mpeg1";
                File outputFile = new File(outputDir, outputFileName);

                // Step 5: Invoke ImageEncoder.encodeImages
                // Parameters:
                // - inputDir path
                // - outputFile path
                // - format: "mpeg1"
                // - quality: 80
                ImageEncoder.encodeImages(inputDir.getAbsolutePath(), outputFile.getAbsolutePath(), "mpeg1", 80);

                // Step 6: Notify user of success
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Encoding completed successfully.\nOutput: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                // Handle exceptions and notify user
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error during encoding: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    /**
     * Copies the content from a URI to a specified file.
     *
     * @param uri      The URI to copy from.
     * @param destFile The destination file to copy to.
     * @throws Exception If an error occurs during copying.
     */
    private void copyUriToFile(Uri uri, File destFile) throws Exception {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(destFile)) {

            if (inputStream == null) {
                throw new Exception("Unable to open input stream for URI: " + uri.toString());
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Retrieves the display name (filename) of a URI.
     *
     * @param uri The URI to retrieve the filename from.
     * @return The filename as a String.
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service to prevent memory leaks
        executorService.shutdown();
    }
}
