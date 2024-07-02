package com.example.zhonghe.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

public class App extends Application {
    /**
     * uhf
     */
    public static UHFRManager mUhfrManager;
    private SharedPreferences mSharedPreferences;
    private ScanUtil scanUtil;
    public static boolean isConnectUHF = false;
    public static int type = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = this.getSharedPreferences("UHF", MODE_PRIVATE);
        AppStateTracker.track(this, new AppStateTracker.AppStateChangeListener() {
            @Override
            public void appTurnIntoForeground() {
                System.err.println("-----------------------------------上电");
                initModule();
                setScanKeyDisable();
            }
            @Override
            public void appTurnIntoBackGround() {
                System.err.println("-----------------------------------下电");
                setScanKeyEnable();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                closeModule();
            }
        });
    }

    /**
     * onStart：开启超高频模块
     * setScanKeyDisable：禁用扫描服务按键
     * onStop：关闭超高频模块
     * setScanKeyEnable：启用扫描服务按键
     * */
    //禁用扫描服务按键
    public void setScanKeyDisable() {
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion > Build.VERSION_CODES.N) {
            // For Android10.0 module
            scanUtil = ScanUtil.getInstance(this);
//            scanUtil.disableScanKey("134");
//            scanUtil.disableScanKey("137");
            scanUtil.setBarcodeSendMode(0);//设置为广播
        }
    }
    SharedUtil sharedUtil;
    /**
     * 初始化uhf模块
     */
    private void initModule() {
        mUhfrManager = UHFRManager.getInstance();// Init Uhf module
        if (mUhfrManager != null) {
            //5106和6106 /6107和6108 支持33db
            sharedUtil = new SharedUtil(this);
            Reader.READER_ERR err = mUhfrManager.setPower(sharedUtil.getPower(), sharedUtil.getPower());//set uhf module power

            if (err == Reader.READER_ERR.MT_OK_ERR) {
                isConnectUHF = true;
                Reader.READER_ERR err1 = mUhfrManager.setRegion(Reader.Region_Conf.valueOf(sharedUtil.getWorkFreq()));
                Toast.makeText(getApplicationContext(), "FreRegion:" + Reader.Region_Conf.valueOf(sharedUtil.getWorkFreq()) +
                        "\n" + "Read Power:" + sharedUtil.getPower() +
                        "\n" + "Write Power:" + sharedUtil.getPower(), Toast.LENGTH_LONG).show();
                setParam();
                if (mUhfrManager.getHardware().equals("1.1.01")) {
                    type = 0;
                }

            } else {
                //5101 30db
                Reader.READER_ERR err1 = mUhfrManager.setPower(30, 30);//set uhf module power
                if (err1 == Reader.READER_ERR.MT_OK_ERR) {
                    isConnectUHF = true;
                    mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
                    Toast.makeText(getApplicationContext(), "FreRegion:" + Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)) +
                            "\n" + "Read Power:" + 30 +
                            "\n" + "Write Power:" + 30, Toast.LENGTH_LONG).show();
                    setParam();
                } else {
                    CommonUtils.showShorMsg(this, "模块初始失败");
                }
            }

        } else {
            CommonUtils.showShorMsg(this, "模块初始失败");
        }
    }

    //设置参数
    private void setParam() {
        //session
        mUhfrManager.setGen2session(sharedUtil.getSession());
        //taget
        mUhfrManager.setTarget(sharedUtil.getTarget());
        //q value
        mUhfrManager.setQvaule(sharedUtil.getQvalue());
    }


    //启用扫描服务按键
    private void setScanKeyEnable() {
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion > Build.VERSION_CODES.N) {
            // For Android10.0 module
            scanUtil = ScanUtil.getInstance(this);
//            scanUtil.enableScanKey("134");
//            scanUtil.enableScanKey("137");
            scanUtil.setBarcodeSendMode(1);//设置为焦点
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //关闭超高频模块
    private void closeModule() {
        if (mUhfrManager != null) {//close uhf module
            mUhfrManager.close();
            mUhfrManager = null;
        }
    }
}
