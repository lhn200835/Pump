package com.huxq17.download.core;


import java.io.File;

public final class DownloadInfo {
    private final String url;
    private final File downloadFile;
    private final String id;

    private final long completedSize;
    private final long contentLength;
    private final int finished;
    private final Status status;
    private final String speed;
    private final int errorCode;
    private final String tag;
    private final long createTime;
    private final int progress;

    private DownloadDetailsInfo downloadDetailsInfo;

    DownloadInfo(String url, File downloadFile, String tag, String id, long createTime,
                 String speed, long completedSize, long contentLength, int errorCode,
                 Status status, int finished, int progress, DownloadDetailsInfo downloadDetailsInfo) {
        this.url = url;
        this.downloadFile = downloadFile;
        this.tag = tag;
        this.id = id;
        this.createTime = createTime;
        this.speed = speed;
        this.completedSize = completedSize;
        this.contentLength = contentLength;
        this.errorCode = errorCode;
        this.status = status;
        this.finished = finished;
        this.progress = progress;
        this.downloadDetailsInfo = downloadDetailsInfo;
    }

    DownloadDetailsInfo getDownloadDetailsInfo() {
        return downloadDetailsInfo;
    }

    public void updateFilePath(String filePath) {
        downloadDetailsInfo.updateFilePath(filePath);
    }

    public void setExtraData(Object extraData) {
        downloadDetailsInfo.setExtraData(extraData);
    }

    public Object getExtraData() {
        return downloadDetailsInfo.getWfExtraData();
    }

    public String getSpeed() {
        return speed;
    }

    public String getTag() {
        return tag == null ? "" : tag;
    }

    public String getId() {
        return id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getUrl() {
        return url;
    }

    public String getFilePath() {
        return downloadFile != null ? downloadFile.getPath() : null;
    }

    public String getName() {
        return downloadFile == null ? "" : downloadFile.getName();
    }

    public long getCompletedSize() {
        return completedSize;
    }

    public long getContentLength() {
        return contentLength;
    }

    public int getProgress() {
        return progress;
    }

    public String getMD5() {
        return downloadDetailsInfo.getMd5();
    }

    public int getFinished() {
        return finished;
    }

    public boolean isFinished() {
        return finished == 1;
    }

    public Status getStatus() {
        return status;
    }

    public void setErrorCode(int errorCode) {
        downloadDetailsInfo.setErrorCode(errorCode, true);
    }

    public boolean isRunning() {
        return status.isRunning();
    }

    public enum Status {
        STOPPED, WAIT, RUNNING, PAUSING, PAUSED, FAILED, FINISHED, DELETED;

        public boolean isRunning() {
            return this.ordinal() >= WAIT.ordinal() && this.ordinal() <= RUNNING.ordinal();
        }

        public boolean shouldStop() {
            return this.ordinal() > STOPPED.ordinal() && this.ordinal() < FAILED.ordinal();

        }

        public boolean isCanceled() {
            return ordinal() >= PAUSING.ordinal() && ordinal() <= PAUSED.ordinal();
        }
    }
}
