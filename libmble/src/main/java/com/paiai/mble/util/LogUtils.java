package com.paiai.mble.util;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.subutil.util.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 带日志文件输入的，又可控开关的日志调试
 *
 * @author dessmann developper
 * @version 1.0
 */
public class LogUtils {

    private static final String TAG = "LogUtils";
    private static Boolean LOG_SWITCH = true; // 日志控制总开关 true 在开发工具后台打印日志 false 不打印日志
    private static String LOG_PATH = Utils.getApp().getFilesDir() + "data/" + Utils.getApp().getPackageName() + "/log/";//日志文件的目录，是一个目录
    private static String LOG_FILEPATH;//日志文件的路径，是一个文件
    private static String LOG_FILEPATH_UPLOAD;//待上传的日志文件的路径，是一个文件
    private static SimpleDateFormat consoleLogFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.getDefault());// 日志的输出格式
    private static String companyName;//公司名字
    private static String logTag;//日志标记

    static {
        delFile();
    }

    public static String getLogTag() {
        return logTag;
    }

    public static void setLogTag(String logTag) {
        if (TextUtils.isEmpty(logTag)) {
            return;
        }
        LogUtils.logTag = logTag;
    }

    private static void load() {
        String packageName = Utils.getApp().getPackageName();
        companyName = packageName.substring(packageName.lastIndexOf(".") + 1);
        //单个日志文件名
        String LOG_FILENAME = companyName + "_log_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + "_" + Build.MANUFACTURER + "_" + Build.MODEL + ".txt";
        LOG_FILEPATH = LOG_PATH + LOG_FILENAME;

    }

    private static void loadUploadFile(String LOG_FILENAME_UPLOAD) {
        try {
            //上传的日志文件名
            if (TextUtils.isEmpty(LOG_FILENAME_UPLOAD)) {
                if (TextUtils.isEmpty(logTag)) {
                    LOG_FILENAME_UPLOAD = companyName + "_log_upload_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + "_" + Build.MANUFACTURER + "_" + Build.MODEL + ".zip";
                } else {
                    LOG_FILENAME_UPLOAD = companyName + "_log_upload_" + logTag + "_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + "_" + Build.MANUFACTURER + "_" + Build.MODEL + ".zip";
                }
            }
            LOG_FILEPATH_UPLOAD = LOG_PATH + LOG_FILENAME_UPLOAD;
            File file_upload = new File(LOG_FILEPATH_UPLOAD);
            if (new File(LOG_PATH).mkdirs()) {
                if (file_upload.createNewFile()) {
                    Log.i(TAG, "日志系统已加载");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据tag, msg和等级，输出日志
     */
    private static void log(String tag, String msg, char level) {
        load();
        if (LOG_SWITCH && !TextUtils.isEmpty(msg)) {
            for (int i = 0; i < msg.length(); i += 2000) {
                String str = msg.substring(i,i+2000>msg.length()?msg.length():i+2000);
                if ('e' == level) { // 输出错误信息
                    Log.e(tag, str);
                } else if ('w' == level) {
                    Log.w(tag, str);
                } else if ('d' == level) {
                    Log.d(tag, str);
                } else if ('i' == level) {
                    Log.i(tag, str);
                } else {
                    Log.v(tag, str);
                }
            }
        }
        writeLogtoFile(String.valueOf(level), tag, msg);
    }

    /**
     * 日志文件写入日志
     **/
    private static void writeLogtoFile(String mylogtype, String tag, String text) {// 新建或打开日志文件
        Date nowtime = new Date();
        String msg = consoleLogFormat.format(nowtime) + "    " + mylogtype + "    " + tag + "    " + text + "\n";
        try {
            FileUtils.writeFile(LOG_FILEPATH, msg, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除一周前的日志文件
     */
    public static void delFile() {
        List<String> list = FileUtils.getFileNameList(LOG_PATH);
        if (list == null) {
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        for (String fileName : list) {
            Log.i(TAG, "delFile,检索到日志,file:" + fileName);
            try {
                String deadLineDateString = lastWeek();
                Date deadLineDate = simpleDateFormat.parse(deadLineDateString);
                Log.i(TAG, "delFile,日志截止日期(deadLineDate):" + simpleDateFormat.format(deadLineDate));
                Matcher matcher = Pattern.compile("\\d{8}").matcher(fileName);
                if (!matcher.find()) {
                    continue;
                }
                String fileDateString = matcher.group(0);
                Log.i(TAG, "delFile,日志日期索引(fileDateString):" + fileDateString);
                Date fileDate = simpleDateFormat.parse(fileDateString);
                if (fileDate.before(deadLineDate)) {
                    String filePath = LOG_PATH + fileName;
                    Log.i(TAG, "delFile,删除日志,file:" + filePath);
                    FileUtils.deleteFile(filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得一周前的日期
     */
    public static String lastWeek(){
        Date date = new Date();
        int year=Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(date));
        int month=Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(date));
        int day=Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(date))-6;

        if(day<1){
            month-=1;
            if(month==0){
                year-=1;month=12;
            }
            if(month==4||month==6||month==9||month==11){
                day=30+day;
            }else if(month==1||month==3||month==5||month==7||month==8||month==10||month==12)
            {
                day=31+day;
            }else if(month==2){
                if(year%400==0||(year %4==0&&year%100!=0))day=29+day;
                else day=28+day;
            }
        }
        String y = year+"";String m ="";String d ="";
        if(month<10) m = "0"+month;
        else m=month+"";
        if(day<10) d = "0"+day;
        else d = day+"";
        return y+m+d;
    }

    /**
     * 获取默认的待上传的日志文件
     */
    public static File getLogFile() {
        return getLogFile(null);
    }

    /**
     * 获取指定文件名的待上传的日志文件
     */
    public static File getLogFile(String filenameForUpload) {
        File file;
        loadUploadFile(filenameForUpload);
        List<File> files = new ArrayList<>();
        List<String> list = FileUtils.getFileNameList(LOG_PATH);
        if (list == null || list.size() == 0) {
            return null;
        }
        for (String name : list) {
            files.add(new File(LOG_PATH + name));
        }
        file = new File(LOG_FILEPATH_UPLOAD);
        ZipUtils.zipFiles(files, file, new ZipUtils.ZipListener() {
            @Override
            public void zipProgress(int zipProgress) {
                Log.i(TAG, "zipProgress:" + zipProgress);
            }
        });
        return file;
    }

    /**
     * 根据日志级别显示日志标签
     */
    public static String getLogTag(int loglevel) {
        if (loglevel == Log.ASSERT) {
            return "Log.ASSERT";
        } else if (loglevel == Log.ERROR) {
            return "Log.ERROR";
        } else if (loglevel == Log.WARN) {
            return "Log.WARN";
        } else if (loglevel == Log.INFO) {
            return "Log.INFO";
        } else if (loglevel == Log.DEBUG) {
            return "Log.DEBUG";
        } else if (loglevel == Log.VERBOSE) {
            return "Log.VERBOSE";
        }
        return "UNKNOWN LOG TAG";
    }

    public static void w(Object msg) { // 警告信息
        log(TAG, msg == null ? "empty msg" : msg.toString(), 'w');
    }

    public static void e(Object msg) { // 错误信息
        log(TAG, msg == null ? "empty msg" : msg.toString(), 'e');
    }

    public static void d(Object msg) {// 调试信息
        log(TAG, msg == null ? "empty msg" : msg.toString(), 'd');
    }

    public static void i(Object msg) {//
        log(TAG, msg == null ? "empty msg" : msg.toString(), 'i');
    }

    public static void v(Object msg) {
        log(TAG, msg == null ? "empty msg" : msg.toString(), 'v');
    }

    public static void w(String text) {
        log(TAG, text, 'w');
    }

    public static void e(String text) {
        log(TAG, text, 'e');
    }

    public static void d(String text) {
        log(TAG, text, 'd');
    }

    public static void i(String text) {
        log(TAG, text, 'i');
    }

    public static void v(String text) {
        log(TAG, text, 'v');
    }

    public static void w(String tag, Object msg) { // 警告信息
        log(tag, msg == null ? "empty msg" : msg.toString(), 'w');
    }

    public static void e(String tag, Object msg) { // 错误信息
        log(tag, msg == null ? "empty msg" : msg.toString(), 'e');
    }

    public static void d(String tag, Object msg) {// 调试信息
        log(tag, msg == null ? "empty msg" : msg.toString(), 'd');
    }

    public static void i(String tag, Object msg) {//
        log(tag, msg == null ? "empty msg" : msg.toString(), 'i');
    }

    public static void v(String tag, Object msg) {
        log(tag, msg == null ? "empty msg" : msg.toString(), 'v');
    }

    public static void w(String tag, String text) {
        log(tag, text, 'w');
    }

    public static void e(String tag, String text) {
        log(tag, text, 'e');
    }

    public static void d(String tag, String text) {
        log(tag, text, 'd');
    }

    public static void i(String tag, String text) {
        log(tag, text, 'i');
    }

    public static void v(String tag, String text) {
        log(tag, text, 'v');
    }

    public static Boolean getLogSwitch() {
        return LOG_SWITCH;
    }

    public static void setLogSwitch(Boolean logSwitch) {
        LOG_SWITCH = logSwitch;
    }
}  