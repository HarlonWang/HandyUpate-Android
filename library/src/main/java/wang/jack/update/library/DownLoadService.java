package wang.jack.update.library;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;

/**
 * Created by Jack on 2015/5/4.
 */
public class DownLoadService extends Service{

    private static final String TAG="DownLoadService";

    private NotificationManager nfManager = null;
    private NotificationCompat.Builder nfBuilder = null;

    public static void start(Context context,UpdateInfo updateInfo){
        Intent intent=new Intent(context,DownLoadService.class);
        intent.putExtra("filePath",updateInfo.getUpdateParam().apkPath+File.separator+String.valueOf(System.currentTimeMillis())+".apk");
        intent.putExtra("downloadUrl",updateInfo.apkUrl);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String filePath=intent.getStringExtra("filePath");
        String downloadUrl=intent.getStringExtra("downloadUrl");
        downloadApp(filePath,downloadUrl);
        return super.onStartCommand(intent, flags, startId);
    }

    void downloadApp(String filePath,String downloadUrl){
        Ion.with(this)
                .load(downloadUrl)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        final int percent = (int) ((float) downloaded / total * 100f);
                        Log.d(TAG, "percent=" + percent);
                        DownLoadService.this.updateUIProgress(percent);
                    }
                })
                .write(new File(filePath))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (e != null) {
                            DownLoadService.this.stopSelf();
                            return;
                        }
                        HandyUpdate.install(DownLoadService.this, result.getPath());
                        DownLoadService.this.stopSelf();
                    }
                });
    }

    void updateUIProgress(int progress){
        String contentText = new StringBuffer().append(progress)
                .append("%").toString();

        if (nfManager == null) {
            nfManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (nfBuilder == null) {
            int smallIcon = getApplicationInfo().icon;
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
            nfBuilder = new NotificationCompat.Builder(this)
                    // .setLargeIcon(largeIcon)
                    .setSmallIcon(smallIcon)
                    /*.setContentTitle(contentTitle)*/
                    .setContentText(contentText)
                    .setContentIntent(contentIntent)
                    .setTicker(getString(R.string.updating))
                    .setAutoCancel(true);
        }
        nfBuilder.setContentText(contentText);
        nfBuilder.setProgress(100, progress, false);
        nfManager.notify(0, nfBuilder.build());
    }

}
