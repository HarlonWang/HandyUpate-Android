package wang.jack.update;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import wang.jack.update.library.HandyUpdate;
import wang.jack.update.library.UpdateInfo;


public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HandyUpdate.setCustomParseListener(new HandyUpdate.UpdateParseListener() {
            @Override
            public UpdateInfo getUpdateInfo(String result) {
                UpdateInfo updateInfo=new UpdateInfo();
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    updateInfo.appName=jsonObject.optString("appName");
                    updateInfo.appDescription=jsonObject.optString("appDescription");
                    updateInfo.packageName=jsonObject.optString("packageName");
                    updateInfo.versionCode=jsonObject.optInt("versionCode");
                    updateInfo.versionName=jsonObject.optString("versionName");
                    updateInfo.apkUrl=jsonObject.optString("apkUrl");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return updateInfo;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
