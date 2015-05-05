package wang.jack.update.library;


import android.content.Context;
import android.os.Environment;

/**
 * Created by Jack on 2015/5/4.
 */
public class UpdateParam {

    boolean checkPackage;

    /**
     * if true ,download task will be run through service
     */
    boolean backgroundService;

    /**
     *     if version was latest, show prompt or not
     */
    boolean updatePrompt;

    /**
     * the new version was down where
     */
    String apkPath;

    UpdateListener updateListener;

    public boolean isCheckPackage(){
        return checkPackage;
    }

    public String getApkPath(){
        return apkPath;
    }


    public static class Builder{

        UpdateParam param;

        public Builder(Context context){
            param=new UpdateParam();
            setApkPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
            setUpdateListener(new HandyUpdate.SimpleUpdateListener(context));
        }

        public Builder setCheckPackage(boolean checkPackage){
            param.checkPackage=checkPackage;
            return this;
        }

        public Builder setBackgroundService(boolean backgroundService){
            param.backgroundService=backgroundService;
            return this;
        }

        public Builder setUpdateListener(UpdateListener updateListener){
            param.updateListener=updateListener;
            return this;
        }

        public Builder setApkPath(String apkPath){
            param.apkPath=apkPath;
            return this;
        }

        public Builder setUpdatePrompt(boolean isPrompt){
            param.updatePrompt=isPrompt;
            return this;
        }

        public UpdateParam build(){
            return param;
        }

    }


}
