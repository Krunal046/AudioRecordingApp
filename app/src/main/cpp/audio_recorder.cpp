// app/src/main/cpp/audio_recorder.cpp

#include <jni.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <android/log.h>
#include <cstdio>
#include <cstring>

#define TAG "AudioRecorder"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// OpenSL ES engine & recorder objects
static SLObjectItf engineObject = nullptr;
static SLEngineItf engineEngine = nullptr;
static SLObjectItf recorderObject = nullptr;
static SLRecordItf recorderRecord = nullptr;
static SLAndroidSimpleBufferQueueItf recorderBufferQueue = nullptr;

// File handling
static FILE* outFile = nullptr;
static char gFilePath[512];

// PCM buffer
static constexpr int16_t BUFFER_SIZE = 4096;
static int16_t buffer[BUFFER_SIZE];

// JNI signatures must match your package & class
extern "C" JNIEXPORT jint JNICALL
Java_com_example_audiorecordingapp_MainActivity_nativeStartRecording(
        JNIEnv* env, jobject /* this */, jstring jPath);

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_audiorecordingapp_MainActivity_nativeStopRecording(
        JNIEnv* env, jobject /* this */);

// Callback: write each filled buffer to file, then re-enqueue
static void recorderCallback(SLAndroidSimpleBufferQueueItf bq, void* /*ctx*/) {
    if (outFile) {
        fwrite(buffer, sizeof(buffer), 1, outFile);
    }
    (*recorderBufferQueue)->Enqueue(recorderBufferQueue, buffer, sizeof(buffer));
}

// Helper: create engine and recorder objects
static void createRecorder() {
    // 1) Engine
    slCreateEngine(&engineObject, 0, nullptr, 0, nullptr, nullptr);
    (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

    // 2) Data source: microphone
    SLDataLocator_IODevice loc_dev = {
            SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT,
            SL_DEFAULTDEVICEID_AUDIOINPUT, nullptr
    };
    SLDataSource audioSrc = { &loc_dev, nullptr };

    // 3) Data sink: simple buffer queue + PCM format
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2
    };
    SLDataFormat_PCM formatPcm = {
            SL_DATAFORMAT_PCM,
            1,                  // mono
            SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_CENTER,
            SL_BYTEORDER_LITTLEENDIAN
    };
    SLDataSink audioSnk = { &loc_bq, &formatPcm };

    // 4) Request RECORD + BUFFERQUEUE
    const SLInterfaceID reqIIDs[] = { SL_IID_ANDROIDSIMPLEBUFFERQUEUE, SL_IID_RECORD };
    const SLboolean reqFlags[]  = { SL_BOOLEAN_TRUE,             SL_BOOLEAN_TRUE };
    SLresult res = (*engineEngine)->CreateAudioRecorder(
            engineEngine, &recorderObject, &audioSrc, &audioSnk,
            2, reqIIDs, reqFlags
    );
    if (res != SL_RESULT_SUCCESS) {
        LOGE("CreateAudioRecorder failed: %d", res);
        return;
    }

    (*recorderObject)->Realize(recorderObject, SL_BOOLEAN_FALSE);
    (*recorderObject)->GetInterface(recorderObject, SL_IID_RECORD, &recorderRecord);
    (*recorderObject)->GetInterface(
            recorderObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &recorderBufferQueue
    );

    (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, recorderCallback, nullptr);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_audiorecordingapp_MainActivity_nativeStartRecording(
        JNIEnv* env, jobject /* this */, jstring jPath)
{
    // Copy Java string to C
    const char* path = env->GetStringUTFChars(jPath, nullptr);
    strncpy(gFilePath, path, sizeof(gFilePath) - 1);
    env->ReleaseStringUTFChars(jPath, path);

    // Open file & write 44-byte WAV header placeholder
    outFile = std::fopen(gFilePath, "wb");
    if (!outFile) {
        LOGE("Failed to open %s", gFilePath);
        return -1;
    }
    uint8_t header[44] = {0};
    memcpy(header, "RIFF", 4);
    memcpy(header + 8, "WAVEfmt ", 8);
    uint32_t le16 = 16;     memcpy(header + 16, &le16, 4);
    uint16_t le1  = 1;      memcpy(header + 20, &le1, 2);
    uint16_t ch   = 1;      memcpy(header + 22, &ch,   2);
    uint32_t sr   = 44100;  memcpy(header + 24, &sr,   4);
    uint32_t br   = sr * ch * 16/8; memcpy(header + 28, &br, 4);
    uint16_t ba   = ch * 16/8;      memcpy(header + 32, &ba, 2);
    uint16_t bp   = 16;             memcpy(header + 34, &bp, 2);
    memcpy(header + 36, "data", 4);
    fwrite(header, sizeof(header), 1, outFile);

    // Create & start recorder
    if (!recorderObject) createRecorder();
    (*recorderBufferQueue)->Enqueue(recorderBufferQueue, buffer, sizeof(buffer));
    return (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_RECORDING);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_audiorecordingapp_MainActivity_nativeStopRecording(
        JNIEnv* env, jobject /* this */)
{
    // Stop & destroy recorder
    if (recorderRecord) {
        (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
        (*recorderObject)->Destroy(recorderObject);
        recorderObject = nullptr;
    }
    if (engineObject) {
        (*engineObject)->Destroy(engineObject);
        engineObject = nullptr;
    }

    // Fix WAV header with actual sizes
    if (outFile) {
        long fileSize = std::ftell(outFile);
        uint32_t riffSize = fileSize - 8;
        uint32_t dataSize = fileSize - 44;

        std::fseek(outFile, 4,  SEEK_SET);
        fwrite(&riffSize, 4, 1, outFile);
        std::fseek(outFile, 40, SEEK_SET);
        fwrite(&dataSize, 4, 1, outFile);

        std::fclose(outFile);
        outFile = nullptr;
    }

    // Return the file path back to Java
    return env->NewStringUTF(gFilePath);
}


