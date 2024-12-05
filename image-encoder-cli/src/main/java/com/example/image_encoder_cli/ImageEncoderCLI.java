package com.example.image_encoder_cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "image-encoder",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Encodes a sequence of images into a video file."
)
public class ImageEncoderCLI implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, description = "Input directory containing images", required = true)
    private File inputDir;

    @Option(names = {"-o", "--output"}, description = "Output video file path", required = true)
    private File outputFile;

    @Option(names = {"-f", "--format"}, description = "Output video format (e.g., mp4, avi)", defaultValue = "mp4")
    private String format;

    @Option(names = {"-q", "--quality"}, description = "Video quality (1-100)", defaultValue = "80")
    private int quality;

    @Override
    public Integer call() {
        try {
            // Use the CLI-specific encoder
            CLIImageEncoder encoder = new CLIImageEncoder();
            encoder.encodeImages(inputDir.getAbsolutePath(), outputFile.getAbsolutePath(), format, quality);

            System.out.println("Video saved to: " + outputFile.getAbsolutePath());
            return 0; // Success
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1; // Error
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ImageEncoderCLI()).execute(args);
        System.exit(exitCode);
    }
}
