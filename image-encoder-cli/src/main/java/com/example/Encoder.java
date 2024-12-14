package com.example;

public class Encoder {

    static {
//        String projectDir = System.getProperty("user.dir");
//        String libPath = projectDir + "/libs/codec/libencoder_jni.so";
//        System.out.println(libPath);
//        System.out.println("java.library.path: " + System.getProperty("java.library.path"));
        System.loadLibrary("encoder_jni");
    }

    public static native int mpegEncodeProcedure(String imagesPath, String bitstream, String output, int quality);
}
