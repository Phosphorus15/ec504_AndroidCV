package com.example.image_encoder_cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import com.example.image_encoder_core.ImageEncoder;
import java.util.concurrent.Callable;

@Command(name = "image-encoder-cli", mixinStandardHelpOptions = true, version = "1.0",
        description = "Encodes a series of images into a compressed video format.")
public class ImageEncoderCLI implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, description = "Input directory containing JPEG images.", required = true)
    private String inputDir;

    @Option(names = {"-o", "--output"}, description = "Output file path for the encoded video.", required = true)
    private String outputFile;

    @Option(names = {"-f", "--format"}, description = "Output video format (e.g., mpeg1).", required = true)
    private String format;

    @Option(names = {"-q", "--quality"}, description = "Encoding quality (1-100).", defaultValue = "75")
    private int quality;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ImageEncoderCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            // Invoke the encoding process from the core module
            ImageEncoder.encodeImages(inputDir, outputFile, format, quality);
            System.out.println("Image encoding completed successfully.");
            return 0;
        } catch (Exception e) {
            System.err.println("Error during encoding: " + e.getMessage());
            e.printStackTrace(); // Optional: Print stack trace for debugging
            return 1;
        }
    }
}
