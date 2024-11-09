#include <jni.h>
#include <stdlib.h>
#include <string>
#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"
#include "image_processing.h"
#include "jpeg_handler.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_androidvideoencoder_encoder_CEncoderInterface_stringFromJNI(JNIEnv *env,
                                                                             jobject thiz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}



extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_androidvideoencoder_encoder_CEncoderInterface_createImageHandler(JNIEnv *env,
                                                                                  jclass clazz,
                                                                                  jint width,
                                                                                  jint height,
                                                                                  jint channel,
                                                                                  jbyteArray arr) {
    auto *image = static_cast<Image *>(malloc(sizeof(Image)));
    jsize len = env->GetArrayLength(arr);
    image->width = width;
    image->height = height;
    image->channels = channel;
    image->data = static_cast<unsigned char *>(malloc(len));
    env->GetByteArrayRegion(arr, 0, len, reinterpret_cast<jbyte *>(image->data));
    return reinterpret_cast<jlong>(image);
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_androidvideoencoder_encoder_CEncoderInterface_fromImageHandler(JNIEnv *env,
                                                                                jclass clazz,
                                                                                jlong handler,
                                                                                jclass clz) {
    auto *image = reinterpret_cast<Image *>(handler);
    jmethodID constructor = env->GetMethodID(clz, "<init>", "(IIILjava/nio/ByteBuffer;)V");
    return env->NewObject(clz, constructor, image->width, image->height, image->channels,
                          env->NewDirectByteBuffer(
                                  reinterpret_cast<void *>(image->channels),
                                  image->width * image->height * image->channels));
}


