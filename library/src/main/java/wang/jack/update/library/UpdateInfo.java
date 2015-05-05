package wang.jack.update.library;


/**
 * Created by Jack on 2015/5/4.
 */
public class UpdateInfo {

   public String appName;
   public String appDescription;
   public String packageName;
   public int versionCode;
   public String versionName;
   public String apkUrl;

    private UpdateParam updateParam;

    public void setUpdateParam(UpdateParam updateParam){
        this.updateParam=updateParam;
    }

    public UpdateParam getUpdateParam(){
        return updateParam;
    }


}
