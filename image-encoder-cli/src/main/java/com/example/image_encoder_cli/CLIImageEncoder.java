package com.example.image_encoder_cli;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class CLIImageEncoder extends com.example.image_encoder_core.ImageEncoder {
    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    @Override
    protected void processImages(String inputDir, FFmpegFrameRecorder recorder) throws Exception {
        File directory = new File(inputDir);
        File[] imageFiles = directory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".png")
        );

        if (imageFiles == null || imageFiles.length == 0) {
            throw new IllegalArgumentException("No images found in directory: " + inputDir);
        }

        Arrays.sort(imageFiles); // Ensure consistent order
        for (File imageFile : imageFiles) {
            Frame frame = loadAndConvertImage(imageFile);
            if (frame != null) {
                recorder.record(frame);
            }
        }
    }

    @Override
    protected Frame loadAndConvertImage(File imageFile) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Failed to read image: " + imageFile.getAbsolutePath());
        }

        BufferedImage resizedImage = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        resizedImage.createGraphics().drawImage(bufferedImage, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);

        return converter.convert(resizedImage);
    }
}
