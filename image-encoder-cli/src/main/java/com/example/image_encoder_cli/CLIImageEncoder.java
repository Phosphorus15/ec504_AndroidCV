package com.example.image_encoder_cli;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;
import com.example.image_encoder_core.ImageEncoder;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class CLIImageEncoder extends ImageEncoder {
    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    @Override
    protected Frame loadAndConvertImage(File imageFile) throws Exception {
        BufferedImage originalImage = ImageIO.read(imageFile);
        if (originalImage == null) {
            throw new IllegalArgumentException("Failed to read image: " + imageFile.getAbsolutePath());
        }

        try {
            BufferedImage resizedImage = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
            resizedImage.createGraphics().drawImage(originalImage, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
            Frame frame = converter.convert(resizedImage);

            originalImage.flush();
            resizedImage.flush();

            return frame;
        } catch (Exception e) {
            originalImage.flush();
            throw e;
        }
    }
}