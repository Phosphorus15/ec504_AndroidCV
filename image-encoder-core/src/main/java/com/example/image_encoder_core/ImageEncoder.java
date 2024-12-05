package com.example.image_encoder_core;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

import java.io.File;

public abstract class ImageEncoder {
    protected static final int DEFAULT_WIDTH = 1920;
    protected static final int DEFAULT_HEIGHT = 1080;
    protected static final int FRAME_RATE = 30;

    public void encodeImages(String inputDir, String outputFile, String format, int quality) throws Exception {
        FFmpegFrameRecorder recorder = null;
        try {
            recorder = new FFmpegFrameRecorder(outputFile, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat(format);
            recorder.setFrameRate(FRAME_RATE);
            recorder.setVideoBitrate(quality * 10000);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.start();

            processDirectory(inputDir, recorder);
        } finally {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
        }
    }

    protected abstract void processDirectory(String inputDir, FFmpegFrameRecorder recorder) throws Exception;
    protected abstract Frame loadAndConvertImage(File imageFile) throws Exception;
}