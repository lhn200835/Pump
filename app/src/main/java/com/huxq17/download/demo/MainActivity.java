package com.huxq17.download.demo;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.huxq17.download.Pump;
import com.huxq17.download.core.DownloadListener;
import com.huxq17.download.demo.installapk.APK;
import com.huxq17.download.demo.remote.RemoteDownloadListActivity;
import com.huxq17.download.utils.LogUtil;

import java.io.File;
import java.net.URLEncoder;

import okhttp3.Request;

public class MainActivity extends AppCompatActivity {
    //        private String url = "http://dlied5.myapp.com/myapp/1104466820/sgame/2017_com.tencent.tmgp.sgame_h178_1.41.2.16_5a7ef8.apk";
    private String url = "http://down.youxifan.com/Q6ICeD";
    //    private String url = "http://www.anzhi.com/dl_app.php?s=3080740&n=5";
    //    private String url = "http://xiazai.3733.com/pojie/game/podsctjpjb.apk";
    private String url2 = "https://file.izuiyou.com/download/package/zuiyou.apk?from=ixiaochuan";
    //http://www.httpwatch.com/httpgallery/chunked/chunkedimage.aspx
    String url4 = "http://v.nq6.com/xinqu.apk";
    //    String url5 = "http://t2.hddhhn.com/uploads/tu/201612/98/st93.png";
    String url5 = "http://13303988.169.ctc.data.bego.cc/down/7b70f0f66130bc7e53548dde949d30d8/%E7%8E%8B%E7%BB%8E%E9%BE%99%5ESunny-%E6%91%87%E5%95%8A%E6%91%87_%E5%9B%BD%E8%AF%AD_%E6%83%85%E6%AD%8C%E5%AF%B9%E5%94%B1_NCB13263_%5Bmvmkv.com%5D_MV%E5%88%86%E4%BA%AB%E7%B2%BE%E7%81%B5_MTVP2P.mkv?cts=dir-048e357796657f660297bd902d097a10&ctp=210A22A80A138&ctt=1595164316&limit=2&spd=1500000&ctk=7b70f0f66130bc7e53548dde949d30d8&chk=ca23c1dd061a6554d0458d7d4bfe7ad6-51074337";
    private ProgressDialog progressDialog;
    private final static String TAG = "groupA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initProgressDialog();
//        Pump.subscribe(downloadListener);
//        try {
//            File httpCacheDir = new File(getCacheDir(), "http");
//            long httpCacheSize = 50 * 1024 * 1024;
//            Class.forName("android.net.http.HttpResponseCache")
//                    .getMethod("install", File.class, long.class)
//                    .invoke(null, httpCacheDir, httpCacheSize);
//        } catch (Exception httpResponseCacheNotAvailable) {
//        }
        final EditText etDownload = findViewById(R.id.etDownload);
        findViewById(R.id.add_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setProgress(0);
                progressDialog.show();
                String downloadUrl = etDownload.getText().toString();
                if (downloadUrl.isEmpty()) {
                    downloadUrl = url5;
                }

                Pump.newRequest(downloadUrl)
                        .setRequestBuilder(new Request.Builder()
                                .addHeader("referer", "http://www.mtv-ktv.net/mv/mtv15/ktv143092.htm"))
                        .listener(new DownloadListener(MainActivity.this) {

                            @Override
                            public void onProgress(int progress) {
                                progressDialog.setProgress(progress);
                            }

                            @Override
                            public void onSuccess() {
                                progressDialog.dismiss();
                                String apkPath = getDownloadInfo().getFilePath();
                                APK.with(MainActivity.this)
                                        .from(apkPath)
//                                        .forceInstall();
                                        .install();
                                Toast.makeText(MainActivity.this, "Download Finished", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailed() {
                                progressDialog.dismiss();
                                LogUtil.e("onFailed code=" + getDownloadInfo().getErrorCode());
                                Toast.makeText(MainActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                            }
                        })
                        //Set whether to repeatedly download the downloaded file,default false.
                        .forceReDownload(true)
                        //Set how many threads are used when downloading,default 3.
                        .threadNum(1)
                        //Pump will connect server by this OKHttp request builder,so you can customize http request.
                        .setRequestBuilder(new Request.Builder())
                        .setRetry(3, 200)
                        .submit();
            }
        });

        findViewById(R.id.add_download_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file1 = new File(getExternalCacheDir().getAbsolutePath(), "download1.apk");
                Pump.newRequest(url, file1.getAbsolutePath())
                        .setDownloadTaskExecutor(DemoApplication.getInstance().musicDownloadDispatcher)
                        .forceReDownload(true)
                        .submit();
                Pump.newRequest(url4)
                        .setDownloadTaskExecutor(DemoApplication.getInstance().musicDownloadDispatcher)
                        .forceReDownload(true)
                        .submit();
                Pump.newRequest(url2)
                        .tag(TAG)
                        .forceReDownload(true)
                        .submit();
            }
        });

        findViewById(R.id.jump_download_list).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean groupByTag = false;
                        DownloadListActivity.start(v.getContext(), groupByTag ? TAG : "");
                    }
                });
        findViewById(R.id.jump_remote_download_list).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RemoteDownloadListActivity.start(v.getContext());
                    }
                });
        findViewById(R.id.jump_webview_download).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WebViewDownloadActivity.start(v.getContext());
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading");
//        progressDialog.setMessage("Downloading now...");
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //shutdown will stop all tasks and release some resource.
//        Pump.shutdown();
    }
}
