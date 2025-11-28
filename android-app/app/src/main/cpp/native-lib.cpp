#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

#define TAG "FireGuardNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

// Example structs for returning results
extern "C" {

// Example placeholder: initialize models (call init functions from EI exports)
JNIEXPORT jboolean JNICALL
Java_com_fireguardedge_app_NativeBridge_nativeInitModels(JNIEnv *env, jobject thiz) {
    // call Edge Impulse init functions if they exist, e.g. ei_model_init()
    // return true on success
    LOGD("nativeInitModels called");
    return JNI_TRUE;
}

// Vision inference: pass image bytes (RGB888) and width,height. Returns label index and confidence.
JNIEXPORT jfloatArray JNICALL
Java_com_fireguardedge_app_NativeBridge_nativeRunVision(JNIEnv *env, jobject thiz,
                                                        jbyteArray imageData,
                                                        jint width, jint height) {
    jsize len = env->GetArrayLength(imageData);
    jbyte* data = env->GetByteArrayElements(imageData, 0);

    // TODO: convert to the expected input format and call the edge impulse vision inference function
    // Placeholder: return [label_index (float), confidence (0..1)]
    jfloatArray out = env->NewFloatArray(2);
    jfloat tmp[2];
    tmp[0] = 0;      // predicted label index (0=normal,1=smoke,2=fire) -- for example
    tmp[1] = 0.5f;   // confidence
    env->SetFloatArrayRegion(out, 0, 2, tmp);

    env->ReleaseByteArrayElements(imageData, data, JNI_ABORT);
    return out;
}

// Audio inference: pass float array of PCM16/float normalized samples and length. Returns [label_index, confidence]
JNIEXPORT jfloatArray JNICALL
Java_com_fireguardedge_app_NativeBridge_nativeRunAudio(JNIEnv *env, jobject thiz,
                                                       jfloatArray audioData, jint length) {
    jsize len = env->GetArrayLength(audioData);
    jfloat* data = env->GetFloatArrayElements(audioData, 0);

    // TODO: call Edge Impulse audio inference pipeline.
    jfloatArray out = env->NewFloatArray(2);
    jfloat tmp[2];
    tmp[0] = 0;      // label index
    tmp[1] = 0.4f;   // confidence
    env->SetFloatArrayRegion(out, 0, 2, tmp);

    env->ReleaseFloatArrayElements(audioData, data, JNI_ABORT);
    return out;
}

} // extern "C"