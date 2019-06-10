/*
* Copyright (c) 2014-2015 Qualcomm Technologies, Inc. All Rights Reserved.
* Qualcomm Technologies Proprietary and Confidential.
*
* Not a Contribution.
* Apache license notifications and license are retained
* for attribution purposes only.
*/

 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
#define LOG_TAG "Sensors_CAL"

#include <hardware/hardware.h>
#include <utils/Timers.h>
#include "sensors_extension.h"
#include <sensor_cal.h>
//#include <hash_map>
#include <string.h>
#include <jni.h>
#include <JNIHelp.h>
#include <utils/Log.h>

static const char *subcmd_calibration = "calibration";

static const char *extra_cmd_list[] = {
    subcmd_calibration,
};

static struct sensors_poll_device_1_ext_t *device = NULL;
static struct sensor_t const *sensor_list = NULL;
static int dev_count = 0;
static int cur_sensor_type = 0;
static char cur_module_name[32];
static int calibration_result = FAILED;
//static mutex_locker g_mutex;

static char const *get_sensor_name(int type) {
    switch (type) {
    case SENSOR_TYPE_ACCELEROMETER:
        return "Acc";
    case SENSOR_TYPE_MAGNETIC_FIELD:
        return "Mag";
    case SENSOR_TYPE_ORIENTATION:
        return "Ori";
    case SENSOR_TYPE_GYROSCOPE:
        return "Gyr";
    case SENSOR_TYPE_LIGHT:
        return "Lux";
    case SENSOR_TYPE_PRESSURE:
        return "Bar";
    case SENSOR_TYPE_TEMPERATURE:
        return "Tmp";
    case SENSOR_TYPE_PROXIMITY:
        return "Prx";
    case SENSOR_TYPE_GRAVITY:
        return "Grv";
    case SENSOR_TYPE_LINEAR_ACCELERATION:
        return "Lac";
    case SENSOR_TYPE_ROTATION_VECTOR:
        return "Rot";
    case SENSOR_TYPE_RELATIVE_HUMIDITY:
        return "Hum";
    case SENSOR_TYPE_AMBIENT_TEMPERATURE:
        return "Tam";
    }
    return "ukn";
}

static int get_sensor_type(const char *name) {
    int sensor_type = 0;

    if(!strncmp(name, "acc", 3))
        sensor_type = SENSOR_TYPE_ACCELEROMETER;
    else if(!strncmp(name, "mag", 3))
        sensor_type = SENSOR_TYPE_MAGNETIC_FIELD;
    else if(!strncmp(name, "ori", 3))
        sensor_type = SENSOR_TYPE_ORIENTATION;
    else if(!strncmp(name, "gyr", 3))
        sensor_type = SENSOR_TYPE_GYROSCOPE;
    else if(!strncmp(name, "lig", 3))
        sensor_type = SENSOR_TYPE_LIGHT;
    else if(!strncmp(name, "bar", 3))
        sensor_type = SENSOR_TYPE_PRESSURE;
    else if(!strncmp(name, "tmp", 3))
        sensor_type = SENSOR_TYPE_TEMPERATURE;
    else if(!strncmp(name, "pro", 3))
        sensor_type = SENSOR_TYPE_PROXIMITY;
    else if(!strncmp(name, "grv", 3))
        sensor_type = SENSOR_TYPE_GRAVITY;
    else if(!strncmp(name, "lac", 3))
        sensor_type = SENSOR_TYPE_LINEAR_ACCELERATION;
    else if(!strncmp(name, "rot", 3))
        sensor_type = SENSOR_TYPE_ROTATION_VECTOR;
    else if(!strncmp(name, "hum", 3))
        sensor_type = SENSOR_TYPE_RELATIVE_HUMIDITY;
    else if(!strncmp(name, "tam", 3))
        sensor_type = SENSOR_TYPE_AMBIENT_TEMPERATURE;

    return sensor_type;
}

static int sensor_enable(int sensor_type, int delay, bool enable) {
    int err = FAILED;

    if(sensor_list == NULL || sensor_type <= 0) {
        ALOGE("Invalid sensor number %d passed to initSensor", sensor_type);
        return FAILED;
    }

    for(int i = 0; i < dev_count; i++) {
        if(sensor_list[i].type == sensor_type) {

            if(enable)
                device->setDelay((sensors_poll_device_t *) device, sensor_list[i].handle, ms2ns(delay));

            ALOGI("Activating/Deactiveating sensor : %s", get_sensor_name(sensor_type));
            err = device->activate((sensors_poll_device_t *) device, sensor_list[i].handle, enable);
            if(err != SUCCESS) {
                ALOGE("activate() for '%s'failed (%s)\n", sensor_list[i].name, strerror(-err));
            }
            break;
        }
    }

    return err;
}


static int do_calibration(int sensor_type) {
    int i = 0;
    int ret = FAILED;
    bool found = false;
    struct cal_cmd_t para;
    ALOGE("do_calibration ret ----- 1: %d", ret);
    memset(&para, 0, sizeof(cal_cmd_t));
    for(i = 0; i < dev_count; i++) {
        if(sensor_list[i].type == sensor_type) {
            switch (sensor_list[i].type) {
            case SENSOR_TYPE_ACCELEROMETER:
                found = true;
                para.axis = 3;
                para.save = 1;
                para.apply_now = 1;
                break;
            case SENSOR_TYPE_PROXIMITY:
                found = true;
                para.axis = 2;
                para.save = 1;
                para.apply_now = 1;
                break;
            default:
                break;
            }
            break;
        }
    }
    ALOGE("do_calibration ret ----- 2: %d", ret);
    if(found && !sensor_enable(sensor_type, 0, false)) {
        ret =
            device->calibrate(reinterpret_cast < struct sensors_poll_device_1_ext_t *>(device),
                              sensor_list[i].handle, &para);
    }
    ALOGE("do_calibration ret --- 3: %d", ret);

    return ret;
}

static void get_sensor_data(int sensor_type, sensors_event_t * event) {
    int n = 0;

    if(device == NULL || event == NULL)
        return;

    while(1) {
        n = device->poll((sensors_poll_device_t *) device, event, 1);
        if(n > 0 && event->type == sensor_type)
            break;
    }
}

static int run_calibration(const char *type) {
    int ret = FAILED;

    //mutex_locker::autolock _L(g_mutex);
    cur_sensor_type = get_sensor_type(type);
    calibration_result = do_calibration(cur_sensor_type);

    //sensor_enable(cur_sensor_type, 200, true);

    return calibration_result;
}

/*static int32_t module_run_calibration(hash_map < string, string > &params) {
    int ret = FAILED;

    //mutex_locker::autolock _L(g_mutex);
    cur_sensor_type = get_sensor_type(params["type"].c_str());
    calibration_result = do_calibration(cur_sensor_type);

    sensor_enable(cur_sensor_type, atoi(params["delay"].c_str()), true);

    return calibration_result;
}*/

static int32_t module_init() {
    ALOGI("%s start ", __FUNCTION__);
    struct sensors_module_t *hal_mod = NULL;
    int err = FAILED;
    int i = 0;

    err = hw_get_module(SENSORS_HARDWARE_MODULE_ID, (hw_module_t const **) &hal_mod);
    if(err != 0) {
        ALOGE("SENSOR: hw_get_module() failed (%s)\n", strerror(-err));
        return FAILED;
    }

    err = sensors_open_ext(&hal_mod->common, &device);
    if(err != 0) {
        ALOGE("SENSOR: sensors_open_ext() failed (%s)\n", strerror(-err));
        return FAILED;
    }

    dev_count = hal_mod->get_sensors_list(hal_mod, &sensor_list);
    for(i = 0; i < dev_count; i++) {
        ALOGI("SENSOR: Deactivating all sensor after open,current index: %d", i);
        err = device->activate((sensors_poll_device_t *) device, sensor_list[i].handle, 0);
        if(err != SUCCESS) {
            ALOGE("SENSOR: deactivate() for '%s'failed (%s)\n", sensor_list[i].name, strerror(-err));
            sensors_close_ext(device);
            return FAILED;
        }
    }

    return SUCCESS;
}

static int32_t module_deinit() {
    ALOGI("%s start.", __FUNCTION__);
    sensors_close_ext(device);
    return SUCCESS;
}

static int32_t module_stop() {
    ALOGI("%s start.", __FUNCTION__);
    ALOGI("thread exit, disable the sensor(%s) unlock", get_sensor_name(cur_sensor_type));
    sensor_enable(cur_sensor_type, 0, true);

    return SUCCESS;
}

static void init_native(JNIEnv *env, jobject clazz) {
   module_init();
}

static void finalize_native(JNIEnv *env, jobject clazz) {
  module_stop();
  module_deinit();
}

static int do_calibrate(JNIEnv *env, jobject clazz, jstring type) {
  const char *sbuf;
  sbuf = env->GetStringUTFChars(type,0);
  return run_calibration(sbuf);
}



static JNINativeMethod method_table[] = {
    { "init_native", "()V", (void*)init_native },
    { "finalize_native", "()V", (void*)finalize_native },
    { "doCalibration", "(Ljava/lang/String;)I", (void*)do_calibrate},
};

/*int register_NativeMethods(JNIEnv *env)
{
    return jniRegisterNativeMethods(env, "com/android/server/LightsService",
            method_table, NELEM(method_table));
}*/

static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        __android_log_print(4, LOG_TAG, "Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        __android_log_print(4, LOG_TAG, "RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, "com/sim/cit/SensorCalibration",
                 method_table, sizeof(method_table) / sizeof(method_table[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}
 
typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;
    
    if (vm->GetEnv( &uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        __android_log_print(4, LOG_TAG, "ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        __android_log_print(4, LOG_TAG, "ERROR: registerNatives failed");
        goto bail;
    }
    
    result = JNI_VERSION_1_4;
    
bail:
    return result;
}
