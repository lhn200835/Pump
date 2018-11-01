package com.huxq17.download.task;

import android.util.Log;

import com.buyi.huxq17.serviceagency.ServiceAgency;
import com.huxq17.download.DownloadChain;
import com.huxq17.download.DownloadInfo;
import com.huxq17.download.SpeedMonitor;
import com.huxq17.download.TransferInfo;
import com.huxq17.download.action.Action;
import com.huxq17.download.action.CorrectDownloadInfoAction;
import com.huxq17.download.action.GetContentLengthAction;
import com.huxq17.download.action.MergeFileAction;
import com.huxq17.download.action.StartDownloadAction;
import com.huxq17.download.db.DBService;
import com.huxq17.download.listener.DownLoadLifeCycleObserver;
import com.huxq17.download.message.IMessageCenter;

import java.util.ArrayList;
import java.util.List;

public class DownloadTask implements Task {
    private TransferInfo downloadInfo;
    private DBService dbService;
    private boolean isStopped;
    private boolean isDestroyed;
    private boolean isNeedDelete;
    private IMessageCenter messageCenter;
    private DownLoadLifeCycleObserver downLoadLifeCycleObserver;
    private SpeedMonitor speedMonitor;
    private Thread thread;

    public DownloadTask(TransferInfo downloadInfo, DownLoadLifeCycleObserver downLoadLifeCycleObserver) {
        downloadInfo.setDownloadTask(this);
        this.downloadInfo = downloadInfo;
        isStopped = false;
        isNeedDelete = false;
        isDestroyed = false;
        dbService = DBService.getInstance();
        downloadInfo.setUsed(true);
        speedMonitor = new SpeedMonitor(downloadInfo);
        messageCenter = ServiceAgency.getService(IMessageCenter.class);
        this.downLoadLifeCycleObserver = downLoadLifeCycleObserver;
        downloadInfo.setStatus(DownloadInfo.Status.WAIT);
        notifyProgressChanged(downloadInfo);
    }

    private long start, end;

    @Override
    public void run() {
        thread = Thread.currentThread();
        if (!isStopped) {
            downloadInfo.setStatus(DownloadInfo.Status.RUNNING);
        }
        downLoadLifeCycleObserver.onDownloadStart(this);
        if (!shouldStop()) {
            download();
        }
        thread = null;
        downLoadLifeCycleObserver.onDownloadEnd(this);
    }

    private void log(String msg) {
        Log.e("tag", msg);
    }

    private void downloadWithDownloadChain() {
        List<Action> actions = new ArrayList<>();
        actions.add(new GetContentLengthAction());
        actions.add(new CorrectDownloadInfoAction());
        actions.add(new StartDownloadAction());
        actions.add(new MergeFileAction());
        DownloadChain chain = new DownloadChain(this, actions);
        chain.proceed();
    }

    private void download() {
        start = System.currentTimeMillis();
        downloadWithDownloadChain();
        end = System.currentTimeMillis();
        log("download spend=" + (end - start));
    }

    private int lastProgress = 0;

    public void onDownload(int length) {
        synchronized (downloadInfo) {
            downloadInfo.download(length);
            speedMonitor.compute(length);
            int progress = (int) (downloadInfo.getCompletedSize() * 1f / downloadInfo.getContentLength() * 100);
            if (progress != lastProgress) {
                lastProgress = progress;
                if (progress != 100) {
                    notifyProgressChanged(downloadInfo);
                }
            }
        }
    }

    public void notifyProgressChanged(TransferInfo downloadInfo) {
        if (messageCenter != null)
            messageCenter.notifyProgressChanged(downloadInfo);
    }


    public TransferInfo getDownloadInfo() {
        return downloadInfo;
    }

    public void pause() {
        synchronized (downloadInfo) {
            if (!isDestroyed) {
                downloadInfo.setStatus(DownloadInfo.Status.PAUSING);
                notifyProgressChanged(downloadInfo);
                interrupt();
            }
        }
    }

    public void stop() {
        synchronized (downloadInfo) {
            if (!isDestroyed) {
                isStopped = true;
                downloadInfo.setStatus(DownloadInfo.Status.STOPPED);
                downloadInfo.setDownloadTask(null);
                interrupt();
            }
        }
    }

    private void interrupt() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void delete() {
        synchronized (downloadInfo) {
            if (!isDestroyed) {
                isNeedDelete = true;
                interrupt();
            }
        }
    }

    public boolean isNeedDelete() {
        return isNeedDelete;
    }

    public void setErrorCode(int errorCode) {
        if (downloadInfo.getStatus() != DownloadInfo.Status.PAUSING) {
            downloadInfo.setErrorCode(errorCode);
        }
    }

    public void updateInfo(TransferInfo transferInfo) {
        synchronized (transferInfo) {
            if (!isNeedDelete) {
                dbService.updateInfo(transferInfo);
            }
        }
    }

    public void destroy() {
        isDestroyed = true;
    }

    public boolean shouldStop() {
        DownloadInfo.Status status = downloadInfo.getStatus();
        return status != DownloadInfo.Status.RUNNING || isStopped || isNeedDelete;
    }

}
