package com.example.androidvideoencoder.encoder;

public class CEncoderInterface {
    public native String stringFromJNI();

    public static long toImageHandler(CImage image) {
        return createImageHandler(image.width, image.height, image.channels, image.data.array());
    }

    public static native CImage fromImageHandler(long handler, Class<CImage> clz);

    private static native long createImageHandler(int width, int height, int channel, byte[] arr);

    static {
        System.loadLibrary("libaencoder");
    }
}
