package com.shaoxia.elevator.log;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class L {
    public static final String LOG_TAG = "XuiDeskClock";

    private static final String VERSION_ENG = "eng";
    private static final String VERSION_USER = "user";
    private static final String VERSION_USER_DEBUG = "userdebug";

    private static final ExecutorService SINGLE_THREAD_POLL = Executors.newSingleThreadExecutor();

    private static boolean sDebug = false;

    static {
        switch (Build.TYPE) {
            case VERSION_USER:
                sDebug = true;
                break;
            case VERSION_ENG:
            case VERSION_USER_DEBUG:
                sDebug = true;
                break;
            default:
                Log.e(LOG_TAG, "unknown build type, type = " + Build.TYPE);
                break;
        }
    }

    public static void d(String tag, boolean isOut2SDCard, String... messages) {
        if (sDebug) {
            Log.d(LOG_TAG, tag + " : " + getMessage(messages));
        }
        if (isOut2SDCard) {
            printLog2SDCard(tag, "=", messages);
        }
    }

    public static void v(String tag, boolean isOut2SDCard, String... messages) {
        if (sDebug) {
            Log.v(LOG_TAG, tag + " : " + getMessage(messages));
        }
        if (isOut2SDCard) {
            printLog2SDCard(tag, "=", messages);
        }
    }

    public static void i(String tag, boolean isOut2SDCard, String... messages) {
        if (sDebug) {
            Log.i(LOG_TAG, tag + " : " + getMessage(messages));
        }
        if (isOut2SDCard) {
            printLog2SDCard(tag, "=", messages);
        }
    }

    public static void w(String tag, boolean isOut2SDCard, String... messages) {
        if (sDebug) {
            Log.w(LOG_TAG, tag + " : " + getMessage(messages));
        }
        if (isOut2SDCard) {
            printLog2SDCard(tag, "=", messages);
        }
    }

    public static void e(String tag, boolean isOut2SDCard, String... messages) {
        Log.e(LOG_TAG, tag + " : " + getMessage(messages));
        if (isOut2SDCard) {
            printLog2SDCard(tag, "=", messages);
        }
    }

    public static void formatLogD(String tag, String separationStyle, String... messages) {
        printLog2SDCard(tag, separationStyle, messages);
    }

    private static String getMessage(String... messages) {
        return null == messages ? "No message" : messages[0];
    }

    private static void printLog2SDCard(final String tag, final String separationStyle, final String... messages) {
        SINGLE_THREAD_POLL.execute(new Runnable() {
            @Override
            public void run() {
                LogPrinter.println(tag, separationStyle, messages);
            }
        });
    }

    public static class LogPrinter {
        private static final String TAG = "LogPrinter";

        private static final String LOG_FILE_NAME = "elevator.log";
        private static final SimpleDateFormat LOG_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

        public static void println(String tag, String separationStyle, String... messages) {
            PrintWriter logPrinter = null;
            try {
                File parentFile = new File(Environment.getExternalStorageDirectory() + File.separator + "log" + File.separator);

                if (!parentFile.exists()) {
                    boolean isSuccess = parentFile.mkdirs();
                    if (!isSuccess) {
                        e(TAG, false, "create log directory failed");
                        return;
                    }
                }

                File logFile = new File(parentFile, LOG_FILE_NAME);

                if (!logFile.exists()) {
                    boolean isSuccess = logFile.createNewFile();
                    if (!isSuccess) {
                        e(TAG, false, "create DeskClock.log failed");
                        return;
                    }
                }

                logPrinter = new PrintWriter(new FileWriter(logFile, true), true);
                String time = LOG_TIME_FORMATTER.format(new Date());
                String prefix = time + " " + tag + " : ";
                if (null == messages) {
                    logPrinter.println(prefix + "No Message");
                } else if (1 == messages.length) {
                    logPrinter.println(prefix + messages[0]);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 60; i++) {
                        sb.append(separationStyle);
                    }

                    logPrinter.println(prefix + sb.toString());
                    for (String message : messages) {
                        logPrinter.println(prefix + message);
                    }
                    logPrinter.println(prefix + sb.toString());
                }
            } catch (IOException e) {
                e(TAG, false, e.toString());
            } finally {
                if (null != logPrinter) {
                    logPrinter.close();
                }
            }
        }

    }

}
