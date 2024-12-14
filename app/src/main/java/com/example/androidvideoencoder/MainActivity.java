package com.example.androidvideoencoder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int IMAGE_SELECT_REQUEST_CODE = 1002;

    private VideoView videoView;
    private ListView videoListView;
    private ProgressBar progressBar;

    private final List<Uri> selectedImageUris = new ArrayList<>();
    private final List<File> videoFiles = new ArrayList<>();

    private ExecutorService executorService;
    private Uri currentVideoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void initializeViews() {
        videoView = findViewById(R.id.video_view);
        videoListView = findViewById(R.id.video_list_view);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.select_images_button).setOnClickListener(v -> checkPermissionsAndSelectImages());
        findViewById(R.id.encode_button).setOnClickListener(v -> startEncoding());
        findViewById(R.id.load_videos_button).setOnClickListener(v -> loadVideosFromDirectory());

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
        startActivityForResult(Intent.createChooser(intent, "Select Images"), IMAGE_SELECT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_SELECT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
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
                Toast.makeText(this, selectedImageUris.size() + " images selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startEncoding() {
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "No images selected for encoding.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        executorService.execute(() -> {
            try {
                File inputDir = new File(getFilesDir(), "input_images");
                if (!inputDir.exists() && !inputDir.mkdirs()) {
                    throw new IOException("Failed to create input directory");
                }

                File outputFile = createOutputVideoFile();
                for (int i = 0; i < selectedImageUris.size(); i++) {
                    Uri uri = selectedImageUris.get(i);
                    File destFile = new File(inputDir, "image_" + i + ".jpg");
                    copyUriToFile(uri, destFile);
                }

                // Simulated encoding logic (replace with real encoder)
                Thread.sleep(2000);  // Simulate encoding time

                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    currentVideoUri = Uri.fromFile(outputFile);
                    playVideo(currentVideoUri);
                    Toast.makeText(this, "Video encoding complete!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Encoding failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadVideosFromDirectory() {
        videoFiles.clear();
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "AndroidVideoEncoder");

        if (!directory.exists() || !directory.isDirectory()) {
            Toast.makeText(this, "No videos found.", Toast.LENGTH_SHORT).show();
            return;
        }

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));
        if (files != null && files.length > 0) {
            for (File file : files) {
                videoFiles.add(file);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getFileNames(videoFiles));
            videoListView.setAdapter(adapter);
            videoListView.setOnItemClickListener((parent, view, position, id) -> playVideo(Uri.fromFile(videoFiles.get(position))));
        } else {
            Toast.makeText(this, "No videos found.", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> getFileNames(List<File> files) {
        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName());
        }
        return names;
    }

    private void playVideo(Uri videoUri) {
        videoView.setVideoURI(videoUri);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(mp -> videoView.start());
        videoView.setOnCompletionListener(mp -> Toast.makeText(this, "Playback complete.", Toast.LENGTH_SHORT).show());
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Playback error.", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private File createOutputVideoFile() throws IOException {
        File outputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "AndroidVideoEncoder");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Failed to create output directory");
        }
        return new File(outputDir, "video_" + System.currentTimeMillis() + ".mp4");
    }

    private void copyUriToFile(Uri uri, File destFile) throws IOException {
        try (InputStream in = getContentResolver().openInputStream(uri); FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
