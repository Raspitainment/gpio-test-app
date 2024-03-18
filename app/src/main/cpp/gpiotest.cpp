#include <jni.h>
#include <string>
#include <vector>
#include <linux/gpio.h>
#include <sys/ioctl.h>
#include <unistd.h>

extern "C" JNIEXPORT jobject JNICALL
Java_raspitainment_gpiotest_MainActivity_00024Companion_setupGPIO(JNIEnv *env, jobject thiz) {
    int ret = system("echo 26 > /sys/class/gpio/export");
    if (ret == -1) {
        char buf[500];
        sprintf(buf, "system() failed: %s", strerror(errno));
        return env->NewStringUTF(buf);
    } else if (ret != 0) {
        char buf[500];
        sprintf(buf, "Error exporting GPIO 26: %d", ret);
        return env->NewStringUTF(buf);
    }

    ret = system("echo out > /sys/class/gpio/gpio26/direction");
    if (ret == -1) {
        char buf[500];
        sprintf(buf, "system() failed: %s", strerror(errno));
        return env->NewStringUTF(buf);
    } else if (ret != 0) {
        char buf[500];
        sprintf(buf, "Error setting GPIO 26 direction: %d", ret);
        return env->NewStringUTF(buf);
    }

    return nullptr;
}

extern "C" JNIEXPORT jobject JNICALL
Java_raspitainment_gpiotest_MainActivity_00024Companion_closeGPIO(JNIEnv *env, jobject thiz) {
    int ret = system("echo 26 > /sys/class/gpio/unexport");
    if (ret == -1) {
        char buf[500];
        sprintf(buf, "system() failed: %s", strerror(errno));
        return env->NewStringUTF(buf);
    } else if (ret != 0) {
        char buf[500];
        sprintf(buf, "Error unexporting GPIO 26: %d", ret);
        return env->NewStringUTF(buf);
    }

    return nullptr;
}

extern "C" JNIEXPORT jobject JNICALL
Java_raspitainment_gpiotest_MainActivity_00024Companion_readGPIO(JNIEnv *env, jobject thiz) {
    FILE *fp = fopen("/sys/class/gpio/gpio26/value", "r");
    if (fp == nullptr) {
        char buf[500];
        sprintf(buf, "Error opening GPIO 26: %s", strerror(errno));
        return env->NewStringUTF(buf);
    }

    char value;
    if (fread(&value, 1, 1, fp) != 1) {
        char buf[500];
        sprintf(buf, "Error reading GPIO 26: %s", strerror(errno));
        return env->NewStringUTF(buf);
    }

    if (fclose(fp) != 0) {
        char buf[500];
        sprintf(buf, "Error closing GPIO 26: %s", strerror(errno));
        return env->NewStringUTF(buf);
    }

    // make Boolean object
    jclass booleanClass = env->FindClass("java/lang/Boolean");
    jmethodID booleanConstructor = env->GetMethodID(booleanClass, "<init>", "(Z)V");
    jobject booleanObject = env->NewObject(booleanClass, booleanConstructor, value == '1');

    return booleanObject;
}
