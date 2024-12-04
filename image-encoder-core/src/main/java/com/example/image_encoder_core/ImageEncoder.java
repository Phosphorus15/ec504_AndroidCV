package com.example.image_encoder_core;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.ffmpeg.global.swscale;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageEncoder {

    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;
    private static final int DEFAULT_FRAME_RATE = 10;

    /**
     * Encodes images from the input directory into a compressed video file.
     *
     * @param inputDir   Path to the directory containing JPEG images.
     * @param outputFile Path where the encoded video will be saved.
     * @param format     Desired video format (e.g., "mp4", "avi").
     * @param quality    Encoding quality (1-100, affects bitrate).
     * @throws Exception If an error occurs during encoding.
     */
    public static void encodeImages(String inputDir, String outputFile, String format, int quality) throws Exception {
        // Validate inputs
        validateInputParameters(inputDir, outputFile, format, quality);

        // Get and sort image files
        List<File> imageFiles = getImageFiles(inputDir);

        // Configure FFmpeg recorder
        FFmpegFrameRecorder recorder = configureRecorder(outputFile, format, quality);

        try {
            // Start the recorder
            recorder.start();

            // Convert and encode images
            Java2DFrameConverter converter = new Java2DFrameConverter();
            for (File imageFile : imageFiles) {
                BufferedImage image = ImageIO.read(imageFile);
                if (image == null) {
                    throw new IllegalArgumentException("Failed to read image: " + imageFile.getAbsolutePath());
                }

                // Resize and convert image
                BufferedImage resizedImage = resizeImage(image, DEFAULT_WIDTH, DEFAULT_HEIGHT);

                // Convert to FFmpeg frame and record
                recorder.record(converter.convert(resizedImage));
            }
        } finally {
            // Stop and release the recorder
            recorder.stop();
            recorder.release();
        }
    }

    private static void validateInputParameters(String inputDir, String outputFile, String format, int quality) {
        if (inputDir == null || inputDir.trim().isEmpty()) {
            throw new IllegalArgumentException("Input directory path cannot be null or empty");
        }
        if (outputFile == null || outputFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Output file path cannot be null or empty");
        }
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Format cannot be null or empty");
        }
        if (quality < 1 || quality > 100) {
            throw new IllegalArgumentException("Quality must be between 1 and 100");
        }
    }

    private static List<File> getImageFiles(String inputDir) throws Exception {
        File inputFolder = new File(inputDir);
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            throw new IllegalArgumentException("Invalid input directory: " + inputDir);
        }

        File[] files = inputFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No supported image files found in: " + inputDir);
        }

        List<File> imageFiles = new ArrayList<>();
        Collections.addAll(imageFiles, files);
        imageFiles.sort(File::compareTo);
        return imageFiles;
    }

    private static FFmpegFrameRecorder configureRecorder(String outputFile, String format, int quality) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        recorder.setFormat(format);
        recorder.setFrameRate(DEFAULT_FRAME_RATE);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4); // MPEG4 codec
        recorder.setVideoBitrate(quality * 1000); // Bitrate scaled by quality
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        return recorder;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, width, height, null);
        return resizedImage;
    }
}
