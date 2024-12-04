package com.example.image_encoder_cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import com.example.image_encoder_core.ImageEncoder;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "image-encoder",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Encodes a sequence of images into a video file with the specified format and quality."
)
public class ImageEncoderCLI implements Callable<Integer> {

    @Option(
            names = {"-i", "--input"},
            description = "Input directory containing image files.",
            required = true
    )
    private File inputDir;

    @Option(
            names = {"-o", "--output"},
            description = "Output video file path.",
            required = true
    )
    private File outputFile;

    @Option(
            names = {"-f", "--format"},
            description = "Video format (supported: mpeg1, mp4, avi, mkv).",
            defaultValue = "mpeg1"
    )
    private String format;

    @Option(
            names = {"-q", "--quality"},
            description = "Encoding quality (1-100).",
            defaultValue = "80"
    )
    private int quality;

    @Option(
            names = {"-v", "--verbose"},
            description = "Enable verbose output."
    )
    private boolean verbose;

    @Override
    public Integer call() {
        try {
            // Validate input parameters
            validateInputs();

            if (verbose) {
                System.out.println("Starting encoding process...");
                System.out.println("Input directory: " + inputDir.getAbsolutePath());
                System.out.println("Output file: " + outputFile.getAbsolutePath());
                System.out.println("Format: " + format);
                System.out.println("Quality: " + quality);
            }

            // Perform encoding
            ImageEncoder.encodeImages(
                    inputDir.getAbsolutePath(),
                    ensureCorrectOutputFileExtension(outputFile.getAbsolutePath(), format),
                    format,
                    quality
            );

            if (verbose) {
                System.out.println("Encoding completed successfully.");
            }
            return 0; // Success

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1; // Error
        }
    }

    /**
     * Validates input parameters for the CLI tool.
     */
    private void validateInputs() {
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new IllegalArgumentException("Input directory does not exist or is not a directory: " + inputDir);
        }

        if (outputFile.exists()) {
            System.out.println("Warning: Output file already exists and will be overwritten.");
        }

        if (quality < 1 || quality > 100) {
            throw new IllegalArgumentException("Quality must be between 1 and 100.");
        }

        // Validate format
        if (!format.matches("mpeg1|mp4|avi|mkv")) {
            throw new IllegalArgumentException("Unsupported video format: " + format);
        }
    }

    /**
     * Ensures the output file has the correct extension based on the specified format.
     *
     * @param outputPath The original output file path.
     * @param format     The specified video format.
     * @return The corrected output file path.
     */
    private String ensureCorrectOutputFileExtension(String outputPath, String format) {
        String extension = format.equalsIgnoreCase("mpeg1") ? "mpeg" : format.toLowerCase();
        if (!outputPath.endsWith("." + extension)) {
            outputPath += "." + extension;
            if (verbose) {
                System.out.println("Adjusted output file path to match format: " + outputPath);
            }
        }
        return outputPath;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ImageEncoderCLI()).execute(args);
        System.exit(exitCode);
    }
}
