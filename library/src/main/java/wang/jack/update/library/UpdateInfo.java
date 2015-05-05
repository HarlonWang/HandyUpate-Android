package wang.jack.update.library;


/**
 * Created by Jack on 2015/5/4.
 */
public class UpdateInfo {

    String appName;
    String appDescription;
    String packageName;
    int versionCode;
    String versionName;
    String apkUrl;

    private UpdateParam updateParam;

    public void setUpdateParam(UpdateParam updateParam){
        this.updateParam=updateParam;
    }

    public UpdateParam getUpdateParam(){
        return updateParam;
    }


}
