#include <cutils/log.h>
#include <cutils/properties.h>
#include <ctype.h>
#include <dlfcn.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

enum {
    FAILED = -1,
    SUCCESS = 0,
    ERR_UNKNOW = INT_MAX
};

static int32_t module_init();
static int32_t module_stop();
static int run_calibration(char* type);

/**************************/
/*class mutex_locker {
    pthread_mutex_t m_mutex;
  public:
      class autolock {
        mutex_locker & locker;
      public:
        inline autolock(mutex_locker & locker):locker(locker) {
            locker.lock();
        }
        inline ~ autolock() {
            locker.unlock();
        }
    };
    inline mutex_locker() {
        pthread_mutex_init(&m_mutex, 0);
    }
    inline ~ mutex_locker() {
        pthread_mutex_destroy(&m_mutex);
    }
    inline void lock() {
        pthread_mutex_lock(&m_mutex);
    }
    inline void unlock() {
        pthread_mutex_unlock(&m_mutex);
    }
};*/
