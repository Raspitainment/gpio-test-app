#include <jni.h>
#include <string>
#include <vector>
#include <linux/gpio.h>
#include <sys/ioctl.h>
#include <unistd.h>


jobject makeOptionalString(JNIEnv *env, bool present, const char *string) {
    jclass optionalClass = env->FindClass("java/util/Optional");
    jmethodID ofNullable = env->GetStaticMethodID(optionalClass, "of",
                                                  "(Ljava/lang/Object;)Ljava/util/Optional;");
    jmethodID empty = env->GetStaticMethodID(optionalClass, "empty", "()Ljava/util/Optional;");

    if (present) {
        jstring jValue = env->NewStringUTF(string);
        return env->CallStaticObjectMethod(optionalClass, ofNullable, jValue);
    } else {
        return env->CallStaticObjectMethod(optionalClass, empty);
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_raspitainment_MainActivity_00024Companion_setupGpio(JNIEnv *env, jobject thiz) {
    int ret = system("echo 26 > /sys/class/gpio/export");
    if (ret == -1) {
        char buf[500];
        sprintf(buf, "Error exporting GPIO 26: %s", strerror(errno));
        return makeOptionalString(env, false, buf);
    }

    ret = system("echo out > /sys/class/gpio/gpio26/direction");
    if (ret == -1) {
        char buf[500];
        sprintf(buf, "Error setting direction of GPIO 26: %s", strerror(errno));
        return makeOptionalString(env, false, buf);
    }

    return makeOptionalString(env, false, nullptr);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_raspitainment_MainActivity_00024Companion_getValue(JNIEnv *env, jobject thiz) {
    FILE *fp = fopen("/sys/class/gpio/gpio26/value", "r");
    if (fp == nullptr) {
        char buf[500];
        sprintf(buf, "Error opening GPIO 26: %s", strerror(errno));
        return false;
    }

    char value;
    fread(&value, 1, 1, fp);
    fclose(fp);

    return value == '1';
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_raspitainment_MainActivity_00024Companion_closeGpio(JNIEnv *env, jobject thiz) {
    system("echo 26 > /sys/class/gpio/unexport");
}