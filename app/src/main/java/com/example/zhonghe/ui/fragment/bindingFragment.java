package com.example.zhonghe.ui.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.zhonghe.Adapter.bindingAdapter;
import com.example.zhonghe.Adapter.bindingBAdapter;
import com.example.zhonghe.MainActivity;
import com.example.zhonghe.R;
import com.example.zhonghe.SQLite.dataDao;
import com.example.zhonghe.pojo.TagInfo;
import com.example.zhonghe.pojo.data;
import com.example.zhonghe.pojo.power;
import com.example.zhonghe.ui.base.BaseFragment;
import com.example.zhonghe.util.App;
import com.example.zhonghe.util.CommonUtils;
import com.example.zhonghe.util.SPDataUtils;
import com.example.zhonghe.util.ScanUtil;
import com.example.zhonghe.util.SharedUtil;
import com.example.zhonghe.util.Util;
import com.uhf.api.cls.Reader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.serialport.Tools;

public class bindingFragment extends BaseFragment {
    @BindView(R.id.b_TID)
    EditText bTid;
    @BindView(R.id.b_scan)
    Button bScan;
    @BindView(R.id.b_QR)
    EditText bQr;
    @BindView(R.id.b_batch)
    EditText bBatch;
    @BindView(R.id.b_type)
    EditText bType;
    @BindView(R.id.b_comment)
    EditText bComment;
    private MainActivity mainActivity;
    private boolean isReader = false;//是否在读
    private boolean window = false;//是否扫描过
    private Handler handler1 = new Handler();
    private Long index = 1l;//
    private dataDao dataDao;
    private ScanUtil scanUtil;
    private KeyReceiver keyReceiver;
    private boolean mReceiverTag = false;   //广播接受者标识
    private boolean mReceiverQR = false; //广播接收标识-二维码
    Reader.READER_ERR err;
    private SharedUtil sharedUtil;
    List<String> list;
//    String TID = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_binding, container, false);
        ButterKnife.bind(this, view);

        initView();
        setPower();
        return view;
    }

    private void initView() {
        //本地库
        dataDao = new dataDao(mainActivity);
        //二维码广播接收
        ContextWrapper contextWrapper = new ContextWrapper(mainActivity);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");//getStringExtra的参数
        mReceiverQR = true;
        contextWrapper.registerReceiver(receiver, filter);
        //初始化扫描
        scanUtil = ScanUtil.getInstance(mainActivity);
        //声音
        Util.initSoundPool(mainActivity);//Init sound pool
        sharedUtil = new SharedUtil(mainActivity);
    }


    @OnClick(R.id.b_scan)
    public void invenroty() {
        //超高频
//        if (App.mUhfrManager == null) {
//            CommonUtils.showShorMsg(mainActivity, "通讯超时");
//            return;
//        }
//        if (!isReader) {
//            startScanLabels();//开始盘点
//        } else {
//            stopScanLabels();//停止盘点
//        }
        GetTid();
    }

    @OnClick(R.id.b_but1)
    public void but1() {
        if (isReader) {
            CommonUtils.showShorMsg(mainActivity, "请先停止扫描TID");
            return;
        }
        add();
    }

    @OnClick(R.id.b_but2)
    public void but2() {
        if (isReader) {
            CommonUtils.showShorMsg(mainActivity, "请先停止扫描TID");
            return;
        }
        initPane();
    }

    private void GetTid() {
        if (App.mUhfrManager == null) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        List<Reader.TAGINFO> listTag;
        listTag = App.mUhfrManager.tagEpcTidInventoryByTimer((short) 50);//开始读卡
        if (listTag != null && !listTag.isEmpty()) {
            Util.play(1, 0);//声音
            String tid = Tools.Bytes2HexString(listTag.get(0).EmbededData, listTag.get(0).EmbededData.length);
            bTid.setText(tid);
        }
    }

    //开始盘存
//    private void startScanLabels() {
//        list = new ArrayList();
//        handler1.postDelayed(runnable_MainActivity, 0);
//        bScan.setText("结束");
//        isReader = true;
//    }

    //结束盘存
//    private void stopScanLabels() {
//        if (App.isConnectUHF) {
//            if (isReader) {
//                handler1.removeCallbacks(runnable_MainActivity);
//                isReader = false;
//                bScan.setText("扫描");
//            }
//        } else {
//            CommonUtils.showLonMsg(mainActivity, "通讯超时");
//            return;
//        }
//        isReader = false;
//        removeDuplicate();
//    }

//    private Runnable runnable_MainActivity = new Runnable() {
//        @Override
//        public void run() {
//            if (App.mUhfrManager == null) {
//                stopScanLabels();
//                return;
//            }
//            List<Reader.TAGINFO> listTag;
//            listTag = App.mUhfrManager.tagEpcTidInventoryByTimer((short) 50);//开始读卡
//            if (listTag != null && !listTag.isEmpty()) {
//                for (Reader.TAGINFO tfs : listTag) {
//                    String tid = Tools.Bytes2HexString(tfs.EmbededData, tfs.EmbededData.length);
//                    Util.play(1, 0);//声音
//                    System.out.println(tid);
//                    list.add(tid);
//                }
//            }
//            handler1.postDelayed(runnable_MainActivity, 0);
//        }
//    };

    //利用HashSet不能添加重复数据的特性，来让list不可添加重复EPC
//    private void removeDuplicate() {
//        if (list != null && list.size() != 0) {
//            HashSet<String> hset = new HashSet<String>(list);
//            List<String> numberList = new ArrayList<String>(hset);
//            spinner(numberList);
//        } else {
//            return;
//        }
//    }


    //下拉框
//    private void spinner(List<String> tids) {
//        if (null != tids && tids.size() > 0) {
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_item, tids);
//            adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
//            bTid.setAdapter(adapter);
//
//            bTid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//                    //获取Spinner控件的适配器
//                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) adapterView.getAdapter();
//                    TID = adapter.getItem(i);
//                }
//
//                //没有选中时的处理
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//                }
//            });
//        } else {
//            CommonUtils.showShorMsg(mainActivity, "没有搜索到TID");
//            return;
//        }
//    }


    @OnClick(R.id.b_but2)
    public void clear() {
        if (isReader) {
            CommonUtils.showShorMsg(mainActivity, "正在扫描,请先停止！");
            return;
        }
        initPane();
    }

    //使TID和QR为空
    private void setNull() {
//        list.clear();
//        bTid.setAdapter(null);
        bTid.setText("");
        bQr.setText("");
    }

    //清空
    private void initPane() {
//        if (list != null) {
//            list.clear();
//        }
        bTid.setText("");
        bQr.setText("");
        bBatch.setText("");
        bType.setText("");
        bComment.setText("");
//        bTid.setAdapter(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.mUhfrManager != null) {
            App.mUhfrManager.setCancleInventoryFilter();
        }
        registerKeyCodeReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
//        stopScanLabels();
        initPane();
        if (mReceiverTag) {   //判断广播是否注册
            mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
            mainActivity.unregisterReceiver(keyReceiver);
        }
        if (mReceiverQR) {
            mReceiverQR = false;   //赋值为false 表示该广播已被注销
            mainActivity.unregisterReceiver(receiver);
        }
    }

    //手柄按钮
    private void registerKeyCodeReceiver() {
        mReceiverTag = true;    //标识值 赋值为 true 表示广播已被注册
        keyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        mainActivity.registerReceiver(keyReceiver, filter);
    }


    //key receiver
    //
    private class KeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (keyDown) {
//                ToastUtils.showText("KeyReceiver:keyCode = down" + keyCode);
            } else {
//                ToastUtils.showText("KeyReceiver:keyCode = up" + keyCode);
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F1:
                        break;
                    case KeyEvent.KEYCODE_F2:
                        break;
                    case KeyEvent.KEYCODE_F5:
                        break;
                    case KeyEvent.KEYCODE_F3://C510x
                        break;
                    case KeyEvent.KEYCODE_F4://6100
                        break;
                    case KeyEvent.KEYCODE_F7://H3100
                        invenroty();
                        break;
                }
            }
        }
    }

    // BroadcastReceiver to receiver scan data
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                if (barcode.length() != 0 && barcode != null) {
                    bQr.setText(barcode);
                }
            }
        }
    };
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //添加
    private void add() {
        String Tid = bTid.getText().toString().trim();
        String QR = bQr.getText().toString().trim();
        String batch = bBatch.getText().toString().trim();
        String type = bType.getText().toString().trim();
        String comment = bComment.getText().toString().trim();

        if (Tid.length() > 0 && Tid != null) {
            data da = new data();
            da.setTID(Tid);
            da.setQR(QR);
            da.setBatch(batch);
            da.setType(type);
            da.setComment(comment);
            da.setTime(dateFormat.format(new Date()));
            da.setCondition("已绑定");
            int i = dataDao.add(da);
            if (i > 0) {
                CommonUtils.showShorMsg(mainActivity, "绑定成功!");
                setNull();
            }
        } else {
            CommonUtils.showShorMsg(mainActivity, "TID和二维码不可为空");
        }
    }

//    //禁止
//    private void setScanKeyDisable() {
//        int currentApiVersion = Build.VERSION.SDK_INT;
//        if (currentApiVersion > Build.VERSION_CODES.N) {
//            // For Android10.0 module
//            scanUtil.disableScanKey("134");
//            scanUtil.disableScanKey("137");
//        }
//
//    }
//
//    //启动
//    private void setScanKeyEnable() {
//        int currentApiVersion = Build.VERSION.SDK_INT;
//        if (currentApiVersion > Build.VERSION_CODES.N) {
//            // For Android10.0 module
//            scanUtil.enableScanKey("134");
//            scanUtil.enableScanKey("137");
//        }
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    //设置功率
    private void setPower(){
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        power p = SPDataUtils.getInfo(mainActivity);
        err = App.mUhfrManager.setPower(p.getS1(), p.getS1());
        if (err == Reader.READER_ERR.MT_OK_ERR) {
            sharedUtil.savePower(p.getS1());
        } else {
            //5101 仅支持30db
            CommonUtils.showShorMsg(mainActivity, "功率设置失败");
        }
    }
}