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

    @Override
    protected void processDirectory(String inputDir, FFmpegFrameRecorder recorder) throws Exception {
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
        for (File file : files) {
            try {
                Frame frame = loadAndConvertImage(file);
                if (frame != null) {
                    recorder.record(frame);
                    // Add delay between frames
                    Thread.sleep(1000 / FRAME_RATE);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process image: " + file.getName(), e);
            }
        }
    }

    @Override
    protected Frame loadAndConvertImage(File imageFile) throws Exception {
        // Configure bitmap options for better memory handling
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // First decode bounds
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate sample size
        options.inSampleSize = calculateInSampleSize(options, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Decode bitmap with sample size
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        if (bitmap == null) {
            throw new IllegalArgumentException("Failed to decode image: " + imageFile.getAbsolutePath());
        }

        try {
            // Scale bitmap to desired dimensions
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT,
                    true
            );

            // Convert to Frame
            Frame frame = converter.convert(scaledBitmap);

            // Clean up bitmaps
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

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}