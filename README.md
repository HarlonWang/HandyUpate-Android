#HandyUpdate
A simple version update for android app.

## Usage

At first :

Add dependencies in build.gradle.

```groovy
	dependencies {
	   compile 'wang.jack.update.library:library:1.0'
	}
```

An simple example use:

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HandyUpdate.update(this,url);
    }  

default json return should be like

    {
        "updateInfo": {
            "appName": "name",
            "appDescription": "description",
            "packageName": "com....",
            "versionCode": 9,
            "versionName": "1.08",
            "apkUrl": "http://..."
            }
    }
    
    
 or you can implements it with yourself json.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HandyUpdate.setCustomParseListener(new HandyUpdate.UpdateParseListener() {
            @Override
            public UpdateInfo getUpdateInfo(String yourselfJson) {
                //like this
                UpdateInfo updateInfo=new UpdateInfo();
                JSONObject jsonObject=new JSONObject(yourselfJson);
                updateInfo.appName=jsonObject.optString("appName");
                ....
                updateInfo.apkUrl=jsonObject.optString("apkUrl");
                return updateInfo;
            }
        });
        HandyUpdate.update(this, url);
    }

    
## Configuring

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UpdateParam updateParam=new UpdateParam.Builder(this)
                .setUpdatePrompt(true)//if current version was latest, show prompt or not
                .setApkPath(apkPath)
                .setBackgroundService(false)//if true ,download task will be run through service
                .setCheckPackage(false)
                .build();
        HandyUpdate.update(this, url, updateParam);
    }

    
## Tips 

- if version was downloaded ,should be not download again ,this will be solve future ,of course ,welcome send PR if you can help this.

## Thanks
- [android-autoupdater](https://github.com/SnowdreamFramework/android-autoupdater)
- [ion](https://github.com/koush/ion)

##Contact me

 if you have a better idea on this project or way, please let me know, thanks:)

[Email](mailto:81813780@qq.com)

[Weibo](http://weibo.com/601265161)
