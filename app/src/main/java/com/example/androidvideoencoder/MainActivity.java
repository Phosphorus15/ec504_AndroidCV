// MainActivity.java
package com.example.androidvideoencoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button selectImagesButton;

    // ActivityResultLauncher for selecting images
    private ActivityResultLauncher<Intent> selectImagesLauncher;

    // ActivityResultLauncher for requesting permissions
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                            // Handle single image selection
                            Uri imageUri = data.getData();
                            if (imageUri != null) {
                                Toast.makeText(MainActivity.this, "Image Selected: " + imageUri.toString(), Toast.LENGTH_SHORT).show();
                                // TODO: Handle the selected image URI as needed
                            }

                            // Handle multiple image selection
                            if (data.getClipData() != null) {
                                int count = data.getClipData().getItemCount();
                                for (int i = 0; i < count; i++) {
                                    Uri imageUriMultiple = data.getClipData().getItemAt(i).getUri();
                                    Toast.makeText(MainActivity.this, "Image " + (i + 1) + " Selected: " + imageUriMultiple.toString(), Toast.LENGTH_SHORT).show();
                                    // TODO: Handle each selected image URI as needed
                                }
                            }
                        }
                    }
                });

        // Initialize ActivityResultLauncher for requesting permissions
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<java.util.Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(java.util.Map<String, Boolean> result) {
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

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Allow multiple selection
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        selectImagesLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }
}
