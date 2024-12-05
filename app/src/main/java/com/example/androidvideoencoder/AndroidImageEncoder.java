package com.example.androidvideoencoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import com.example.image_encoder_core.ImageEncoder;
import java.io.File;
import java.util.Arrays;

public class AndroidImageEncoder extends ImageEncoder {
    private final AndroidFrameConverter converter = new AndroidFrameConverter();
    private final ProgressCallback progressCallback;

    public interface ProgressCallback {
        void onProgress(int progress);
    }

    public AndroidImageEncoder(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    @Override
    protected Frame loadAndConvertImage(File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        if (bitmap == null) {
            throw new IllegalArgumentException("Failed to decode image: " + imageFile.getAbsolutePath());
        }

        try {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
            Frame frame = converter.convert(scaledBitmap);

            if (scaledBitmap != bitmap) {
                bitmap.recycle();
            }
            scaledBitmap.recycle();

            return frame;
        } catch (Exception e) {
            bitmap.recycle();
            throw e;
        }
    }

    @Override
    protected void processDirectory(String inputDir, FFmpegFrameRecorder recorder) {
        File directory = new File(inputDir);
        File[] files = directory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".png")
        );

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No images found in directory: " + inputDir);
        }

        Arrays.sort(files);
        int totalFiles = files.length;

        for (int i = 0; i < totalFiles; i++) {
            try {
                Frame frame = loadAndConvertImage(files[i]);
                if (frame != null) {
                    recorder.record(frame);
                    Thread.sleep(1000 / FRAME_RATE);

                    if (progressCallback != null) {
                        progressCallback.onProgress((i + 1) * 100 / totalFiles);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process image: " + files[i].getName(), e);
            }
        }
    }
}