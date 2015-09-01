package wang.jack.update.library;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;
import android.widget.Toast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


/**
 * Created by Jack on 2015/5/4.
 */
public class HandyUpdate {

    private static final String TAG="HandyUpdate";

    /**
     * net error or parse exception or else
     */
    private static final int STATUS_ERROR=0;

    /**
     * current version was latest
     */
    private static final int STATUS_LATEST=1;

    /**
     * should be update version and down app
     */
    private static final int STATUS_UPDATE=2;

    static UpdateParseListener sUpdateParseCallback;

    Context mContext;

    /**
     * check version url
     */
    String mCheckUrl;

    /**
     * some update param can be custom set
     */
    UpdateParam mUpdateParam;



    public static void update(Context context,String url){
        update(context,url,null);
    }

    public static void update(Context context,String url,UpdateParam updateParam){
        if (context==null){
            throw new NullPointerException("context should not be null");
        }
        if (!URLUtil.isNetworkUrl(url)){
            return;
        }
        if (updateParam==null){
            UpdateParam defaultUpdateParam=new UpdateParam.Builder(context)
                    .build();
            updateParam=defaultUpdateParam;
        }
        HandyUpdate handyUpdate=new HandyUpdate(context,url,updateParam);
        handyUpdate.checkVersionFromNet();
    }

    public static void setCustomParseListener(UpdateParseListener updateParseListener){
        sUpdateParseCallback=updateParseListener;
    }


    public HandyUpdate(Context context,String url,UpdateParam param){
        this.mContext=context;
        this.mCheckUrl=url;
        this.mUpdateParam=param;
    }

    /**
     * check version was latest or not
     */
    void checkVersionFromNet(){
        Ion.with(mContext)
                .load(mCheckUrl)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        UpdateListener updateListener = mUpdateParam.updateListener;
                        if (e != null) {
                            updateListener.onUpdate(STATUS_ERROR, null);
                            return;
                        }
                        if (sUpdateParseCallback == null) {
                            //default json data return should be
                            // {"updateInfo":{"appName":"name","appDescription":"description","packageName":"com....","versionCode":9,"versionName":"1.08","apkUrl":"http://..."}}
                            UpdateParseListener defaultParseListener = new UpdateParseListener() {
                                @Override
                                public UpdateInfo getUpdateInfo(String result1) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(result1);
                                        JSONObject updateInfoJson = jsonObject.optJSONObject("updateInfo");
                                        UpdateInfo updateInfo = new UpdateInfo();
                                        updateInfo.appName = updateInfoJson.optString("appName");
                                        updateInfo.appDescription = updateInfoJson.optString("appDescription");
                                        updateInfo.packageName = updateInfoJson.optString("packageName");
                                        updateInfo.versionCode = updateInfoJson.optInt("versionCode");
                                        updateInfo.versionName = updateInfoJson.optString("versionName");
                                        updateInfo.apkUrl = updateInfoJson.optString("apkUrl");
                                        return updateInfo;
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    } catch (NullPointerException n) {
                                        n.printStackTrace();
                                    }
                                    return null;
                                }
                            };
                            sUpdateParseCallback = defaultParseListener;
                        }
                        UpdateInfo updateInfo = sUpdateParseCallback.getUpdateInfo(result);
                        if (updateInfo == null) {
                            updateListener.onUpdate(STATUS_ERROR, null);
                            return;
                        }
                        UpdateInfo appInfo = getAppInfo(mContext);
                        if (mUpdateParam.isCheckPackage()) {
                            if (!appInfo.packageName.equals(updateInfo.packageName)) {
                                updateListener.onUpdate(STATUS_ERROR, null);
                                return;
                            }
                        }
                        updateInfo.setUpdateParam(mUpdateParam);
                        if (updateInfo.versionCode <= appInfo.versionCode) {
                            mUpdateParam.updateListener.onUpdate(STATUS_LATEST, updateInfo);
                            return;
                        }
                        mUpdateParam.updateListener.onUpdate(STATUS_UPDATE, updateInfo);
                    }
                });
    }

    public interface UpdateParseListener{
        UpdateInfo getUpdateInfo(String result);
    }

    public static class SimpleUpdateListener implements UpdateListener{

        Context ctx;

        public SimpleUpdateListener(Context ctx){
            this.ctx=ctx;
        }

        @Override
        public void onUpdate(int status, final UpdateInfo updateInfo) {
            switch (status){
                case STATUS_ERROR:
                    break;
                case STATUS_LATEST:
                    UpdateParam updateParam=updateInfo.getUpdateParam();
                    if (ctx!=null&&updateParam.updatePrompt){
                        Toast.makeText(ctx, ctx.getString(R.string.latest), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case STATUS_UPDATE:
                    String message=updateInfo.appDescription;
                    if (TextUtils.isEmpty(message)){
                        message=ctx.getString(R.string.update_version);
                    }
                    new AlertDialog.Builder(ctx)
                            .setTitle(ctx.getString(R.string.update_title))
                            .setMessage(message)
                            .setPositiveButton(ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (updateInfo.getUpdateParam().backgroundService) {
                                        DownLoadManager.startBackground(ctx, updateInfo);
                                    } else {
                                        DownLoadManager.startForeground(ctx, updateInfo);
                                    }

                                }
                            }).setNegativeButton(ctx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                    break;
            }
        }
    }


    public static class DownLoadManager{

        public static void startForeground(final Context context,UpdateInfo updateInfo){
            File apkFile=new File(updateInfo.getUpdateParam().getApkPath(),String.valueOf(System.currentTimeMillis())+".apk");
            final ProgressDialog progressDialog=new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(context.getString(R.string.updating));
            progressDialog.show();
            Ion.with(context).load(updateInfo.apkUrl)
                    .progressDialog(progressDialog)
                    .write(apkFile)
                    .setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File result) {
                            progressDialog.cancel();
                            if (e != null) {
                                return;
                            }
                            install(context, result.getPath());
                        }
                    });
        }

        public static void startBackground(Context context,UpdateInfo updateInfo){
            DownLoadService.start(context,updateInfo);
        }


    }

    private static UpdateInfo getAppInfo(Context context){
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Integer versionCode = pinfo.versionCode; // 1
            String versionName = pinfo.versionName; // 1.0
            String packageName = context.getPackageName();

            UpdateInfo appInfo=new UpdateInfo();
            appInfo.versionCode=versionCode;
            appInfo.versionName=versionName;
            appInfo.packageName=packageName;
            return appInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * install the apk
     *
     * @param context
     * @param filePath
     */
    public static void install(Context context, String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}