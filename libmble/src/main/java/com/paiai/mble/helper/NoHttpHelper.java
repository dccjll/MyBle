package com.paiai.mble.helper;

import android.graphics.Bitmap;

import com.blankj.subutil.util.Utils;
import com.paiai.mble.util.LogUtils;
import com.yolanda.nohttp.FileBinary;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.download.DownloadListener;
import com.yolanda.nohttp.download.DownloadQueue;
import com.yolanda.nohttp.download.DownloadRequest;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NoHttpHelper {

    private static final String TAG = NoHttpHelper.class.getSimpleName();
    private static NoHttpHelper mNoHttpInstance;

    private RequestQueue mRequestQueue;
    private DownloadQueue mDownloadQueue;

    private static final String USER_STATUS = "status";
    private static final String WHAT_VALUE = "what";
    private static final ArrayList<Map<String, Object>> mSignList = new ArrayList<>();
    private static final int maxRequestNumbers = 30;

    static {
        for (int i = 0; i < maxRequestNumbers; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put(USER_STATUS, false);
            map.put(WHAT_VALUE, 1000 + i);
            mSignList.add(map);
        }
    }

    public static NoHttpHelper getInstance() {
        if (mNoHttpInstance == null) {
            synchronized (NoHttpHelper.class) {
                if (mNoHttpInstance == null) {
                    mNoHttpInstance = new NoHttpHelper();
                    mNoHttpInstance.getNoHttpRequestQueue();
                    mNoHttpInstance.getNoHttpDownloadQueue();
                }
            }
        }
        return mNoHttpInstance;
    }

    private static synchronized int assignWhatValue() {
        int what = -1;
        for (int i = 0; i < mSignList.size(); i++) {
            Map<String, Object> map = mSignList.get(i);
            if (!((boolean) map.get(USER_STATUS))) {
                map.put(USER_STATUS, true);
                mSignList.set(i, map);
                what = (int) map.get(WHAT_VALUE);
                break;
            }
        }
        return what;
    }

    private static synchronized void recoverWhatValue(int what) {
        for (int i = 0; i < mSignList.size(); i++) {
            Map<String, Object> map = mSignList.get(i);
            if (what == (int) map.get(WHAT_VALUE)) {
                map.put(USER_STATUS, false);
                mSignList.set(i, map);
                break;
            }
        }
    }

    private void getNoHttpRequestQueue() {
        if (mRequestQueue == null) {
            synchronized (NoHttpHelper.class) {
                if (mRequestQueue == null) {
                    NoHttp.initialize(Utils.getApp());
                    mRequestQueue = NoHttp.newRequestQueue();
                }
            }
        }
    }

    private void getNoHttpDownloadQueue() {
        if (mDownloadQueue == null) {
            synchronized (NoHttpHelper.class) {
                if (mDownloadQueue == null) {
                    NoHttp.initialize(Utils.getApp());
                    mDownloadQueue = NoHttp.newDownloadQueue();
                }
            }
        }
    }

    /**
     * 上传文件
     */
    public void uploadFile(final String url, Map<String, String> postMap, String fileKey, File postFile, final OnResponseListener<String> onResponseListener) {
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.POST);
        request.add(postMap);
        request.add(fileKey, new FileBinary(postFile));
        if (url.startsWith("https")) {
            NoHttpSSLHelper.doHttps(request);
        }
        mRequestQueue.add(assignWhatValue(), request, new OnResponseListener<String>() {
            @Override
            public void onStart(int what) {
                onResponseListener.onStart(what);
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                onResponseListener.onSucceed(what, response);
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                onResponseListener.onFailed(what, response);
            }

            @Override
            public void onFinish(int what) {
                recoverWhatValue(what);
                onResponseListener.onFinish(what);
            }
        });
    }

    /**
     * 下载文件
     */
    public void downloadFile(String url, String fileFolder, String filename, final DownloadListener downloadListener) {
        DownloadRequest request = NoHttp.createDownloadRequest(url, RequestMethod.GET, fileFolder, filename, true, true);
        if (url.startsWith("https")) {
            NoHttpSSLHelper.doHttps(request);
        }
        mDownloadQueue.add(assignWhatValue(), request, new DownloadListener() {
            @Override
            public void onDownloadError(int what, Exception exception) {
                downloadListener.onDownloadError(what, exception);
            }

            @Override
            public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {
                downloadListener.onStart(what, isResume ,rangeSize, responseHeaders, allCount);
            }

            @Override
            public void onProgress(int what, int progress, long fileCount) {
                downloadListener.onProgress(what, progress, fileCount);
            }

            @Override
            public void onFinish(int what, String filePath) {
                recoverWhatValue(what);
                downloadListener.onFinish(what, filePath);
            }

            @Override
            public void onCancel(int what) {
                downloadListener.onCancel(what);
            }
        });
    }

    /**
     * 下载图片
     */
    public void downloadImage(String url, final OnResponseListener<Bitmap> onResponseListener) {
        Request<Bitmap> request = NoHttp.createImageRequest(url, RequestMethod.GET);
        if (url.startsWith("https")) {
            NoHttpSSLHelper.doHttps(request);
        }
        LogUtils.i(TAG,"downloadImage url:"+url);
        mRequestQueue.add(assignWhatValue(), request, new OnResponseListener<Bitmap>() {
            @Override
            public void onStart(int what) {
                onResponseListener.onStart(what);
            }

            @Override
            public void onSucceed(int what, Response<Bitmap> response) {
                onResponseListener.onSucceed(what, response);
            }

            @Override
            public void onFailed(int what, Response<Bitmap> response) {
                onResponseListener.onFailed(what, response);
            }

            @Override
            public void onFinish(int what) {
                recoverWhatValue(what);
                onResponseListener.onFinish(what);
            }
        });
    }

    /**
     * 发送异步post请求
     */
    public void sendAsyncPostStringRequest(String url, Map<String, String> header, Map<String, String> params, @NotNull final OnResponseListener<String> onResponseListener) {
        int what = assignWhatValue();
        // 取消队列中已开始的请求
        mRequestQueue.cancelBySign(what);

        // 创建请求对象
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.POST);
        // 设置请求失败后，重新尝试请求的次数
        request.setRetryCount(2);
        if(header!=null){
            for (String key : header.keySet()) {
                request.setHeader(key, header.get(key));
            }
        }
        // 添加请求参数
        if (params == null) {
            params = new HashMap<>();
        }
        String headerSt = header==null?"null":header.toString();
        LogUtils.i(TAG, "接收到服务器请求\n请求链接:" + url + " \n请求参数:" + params + "\nheader:" + headerSt + "\nwhat=" + what);
        request.add(params);

        // 设置请求取消标志
        request.setCancelSign(what);
        //设置超时
        request.setConnectTimeout(10000);
        //客户端请求使用短连接
        request.setHeader("Connection", "close");
        //https加密
        if (url.startsWith("https")) {
            NoHttpSSLHelper.doHttps(request);
        }
        // 向请求队列中添加请求
        // what: 当多个请求同时使用同一个OnResponseListener时，用来区分请求，类似Handler中的what
        mRequestQueue.add(what, request, new OnResponseListener<String>() {
            @Override
            public void onStart(int what) {
                onResponseListener.onStart(what);
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                int responseCode = response.getHeaders().getResponseCode();
                if (responseCode > 400) {
                    LogUtils.e(TAG, "\nresponseCode > 400\nresponseCode=" + responseCode + "\nurl=" + response.request().url());
                    onFailed(what, response);
                    return;
                }
                String data = response.get();
                /*if (response.request().url().contains("/lockpro/listAppLockPro.action")) {
                    data = "{数据太长,length=" + data.length() + "}";
                }*/
                LogUtils.i(TAG, "\n服务器请求结束\n请求链接:" + response.request().url() + "\n服务器响应的数据:" + data + "\nwhat=" + what);
                onResponseListener.onSucceed(what, response);
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                onResponseListener.onFailed(what, response);
            }

            @Override
            public void onFinish(int what) {
                recoverWhatValue(what);
                onResponseListener.onFinish(what);
            }
        });
    }
}
