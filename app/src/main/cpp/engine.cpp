#include <jni.h>
#include <oboe/Oboe.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "OrbitalDSP", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "OrbitalDSP", __VA_ARGS__)

// The audio processing callback class
class OrbitalEngine : public oboe::AudioStreamDataCallback {
public:
    std::shared_ptr<oboe::AudioStream> stream;

    void startEngine() {
        oboe::AudioStreamBuilder builder;
        builder.setFormat(oboe::AudioFormat::Float)
               ->setChannelCount(oboe::ChannelCount::Stereo)
               ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
               ->setSharingMode(oboe::SharingMode::Exclusive)
               ->setDataCallback(this);
               
        oboe::Result result = builder.openStream(stream);
        
        if (result == oboe::Result::OK) {
            stream->requestStart();
            LOGI("Oboe Audio Stream Started Successfully");
        } else {
            LOGE("Failed to open stream: %s", oboe::convertToText(result));
        }
    }

    // The real-time DSP loop (called thousands of times per second)
    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream, 
            void *audioData, 
            int32_t numFrames) override {
            
        // float *floatData = static_cast<float *>(audioData);
        // TODO: The WOLA FFT and Ambisonic math will be injected here in Phase 3
        
        return oboe::DataCallbackResult::Continue;
    }
};

// Global instance of the engine
OrbitalEngine engine;

// The JNI Bridge exposed to Kotlin
extern "C" JNIEXPORT void JNICALL
Java_com_orbital_app_MainActivity_startDspEngine(JNIEnv* env, jobject /* this */) {
    engine.startEngine();
}