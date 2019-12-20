package com.huxq17.download.core.interceptor;

import com.huxq17.download.core.DownloadDetailsInfo;
import com.huxq17.download.core.DownloadInfo;
import com.huxq17.download.core.DownloadInterceptor;
import com.huxq17.download.core.DownloadRequest;

import static com.huxq17.download.ErrorCode.NETWORK_UNAVAILABLE;

public class RetryInterceptor implements DownloadInterceptor {
    private int retryUpperLimit;
    private int tryCount;
    private DownloadDetailsInfo downloadDetailsInfo;

    @Override
    public DownloadInfo intercept(DownloadChain chain) {
        com.huxq17.download.core.DownloadChain realDownloadChain = (com.huxq17.download.core.DownloadChain) chain;
        DownloadRequest downloadRequest = chain.request();
        downloadDetailsInfo = downloadRequest.getDownloadInfo();
        int retryDelay = downloadRequest.getRetryDelay();
        retryUpperLimit = downloadRequest.getRetryCount();
        DownloadInfo downloadInfo;
        boolean shouldRetry = false;
        while (true) {
            downloadInfo = realDownloadChain.proceed(downloadRequest, shouldRetry);
            shouldRetry = shouldRetry();
            if (shouldRetry) {
                tryCount++;
                downloadDetailsInfo.setStatus(DownloadInfo.Status.RUNNING);
                downloadDetailsInfo.clearErrorCode();
                if (retryDelay > 0) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                break;
            }
        }
        return downloadInfo;
    }

    private boolean shouldRetry() {
        return downloadDetailsInfo.getErrorCode() == NETWORK_UNAVAILABLE
                && retryUpperLimit > tryCount;
    }
}
