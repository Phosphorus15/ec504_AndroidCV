package com.example.image_encoder_core;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import java.io.File;
import java.util.Arrays;

public abstract class ImageEncoder {
    protected static final int DEFAULT_WIDTH = 1920;
    protected static final int DEFAULT_HEIGHT = 1080;
    protected static final int FRAME_RATE = 30;

    public void encodeImages(String inputDir, String outputFile, String format, int quality) throws Exception {
        FFmpegFrameRecorder recorder = null;
        try {
            File outputFileObj = new File(outputFile);
            if (!outputFileObj.getParentFile().exists()) {
                outputFileObj.getParentFile().mkdirs();
            }

            recorder = new FFmpegFrameRecorder(outputFile, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat(format);
            recorder.setFrameRate(FRAME_RATE);
            recorder.setVideoBitrate(quality * 10000);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setOption("preset", "ultrafast");
            recorder.setOption("tune", "zerolatency");
            recorder.setOption("crf", String.valueOf(23));
            recorder.setOption("movflags", "+faststart");
            recorder.start();

            processDirectory(inputDir, recorder);
        } finally {
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
            Frame frame = loadAndConvertImage(file);
            if (frame != null) {
                recorder.record(frame);
                Thread.sleep(1000 / FRAME_RATE);
            }
        }
    }

    protected abstract Frame loadAndConvertImage(File imageFile) throws Exception;
}