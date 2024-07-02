package com.example.zhonghe.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.zhonghe.Adapter.dataBAdapter;
import com.example.zhonghe.Adapter.dataBtn2ClickListener;
import com.example.zhonghe.Adapter.elseAdapter;
import com.example.zhonghe.MainActivity;
import com.example.zhonghe.R;
import com.example.zhonghe.SQLite.dataDao;
import com.example.zhonghe.pojo.data;
import com.example.zhonghe.ui.base.BaseFragment;
import com.example.zhonghe.util.App;
import com.example.zhonghe.util.CommonUtils;
import com.example.zhonghe.util.ScanUtil;
import com.uhf.api.cls.Reader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.serialport.Tools;

public class enterFragment extends BaseFragment implements View.OnClickListener {
    @BindView(R.id.e_listA)
    ListView eListA;
    @BindView(R.id.e_listB)
    ListView elistB;
    @BindView(R.id.e_scan)
    Button eScan;
    @BindView(R.id.e_batch)
    Button eBatch;
    @BindView(R.id.e_clear)
    Button eClear;
    @BindView(R.id.e_QR)
    EditText eQR;
    @BindView(R.id.e_search)
    Button eSearch;
    //弹窗
    private EditText in_batch_B;
    private Button in_but1_B, in_but2_B;

    private dataBAdapter adapter;
    private elseAdapter elseAdapter;
    private boolean isReader = false;//是否在读
    private Handler handler1 = new Handler();
    ;
    private Map<String, data> dataMap = new LinkedHashMap<String, data>();//
    private Map<String, data> dasM = new LinkedHashMap<String, data>();//
    private List<data> dataList = new ArrayList<data>();//
    private List<data> das = new ArrayList<>();
    private dataDao dataDao;
    private Dialog inputDialog;
    private boolean mReceiverTag = false;   //广播接受者标识
    private boolean mReceiverQR = false; //广播接收标识-二维码
    private KeyReceiver keyReceiver;
    private MainActivity mainActivity;
    private ScanUtil scanUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_enter, container, false);
        ButterKnife.bind(this, view);

        initView();
        Dialog();
        ListViewA();
        ListViewB();
        return view;
    }

    private void initView() {
        //弹窗
        inputDialog = new Dialog(mainActivity);
        inputDialog.setContentView(R.layout.inputdialog2);
        inputDialog.setCanceledOnTouchOutside(false);//点击屏幕dialog不消失
        inputDialog.setCancelable(false);//点击返回键dialog不消失
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
    }

    private void Dialog() {
        //弹窗
        in_batch_B = inputDialog.findViewById(R.id.in_batch_B);
        in_but1_B = inputDialog.findViewById(R.id.in_but1_B);
        in_but1_B.setOnClickListener(this);
        in_but2_B = inputDialog.findViewById(R.id.in_but2_B);
        in_but2_B.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.in_but1_B:
                batch();
                break;
            case R.id.in_but2_B:
                inputDialog.dismiss();
                break;
        }
    }

    //列表显示数据适配器A
    private void ListViewA() {
        if (dataList != null) {
            adapter = new dataBAdapter(mainActivity, dataList);
            eListA.setAdapter(adapter);
        }
        adapter.setDataBtn2ClickListener(new dataBtn2ClickListener() {
            @Override
            public void dataBtn2ClickListener(View view, int position) {
                data item = dataList.get(position);
                Tooltip(item.getTID());
            }
        });
    }

    //列表显示数据适配器B
    private void ListViewB() {
        if (das != null) {
            elseAdapter = new elseAdapter(mainActivity, das);
            elistB.setAdapter(elseAdapter);
        }
    }

    //确认提示框
    private void Tooltip(String item) {
        new AlertDialog.Builder(mainActivity).setTitle("确认提示")//设置对话框标题
                .setMessage("是否删除当前TID：" + item)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {//添加确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                        remove(item);
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {//添加返回按钮

                    @Override
                    public void onClick(DialogInterface dialog, int which) {//响应事件
                    }
                }).show();//在按键响应事件中显示此对话框
    }

    //根据TID清除tagInfoList中的数据
    private void remove(String tid) {
        Iterator<data> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            data item = iterator.next();
            if (item.getTID().equals(tid)) {
                iterator.remove();
                adapter.notifyDataSetChanged();//刷新adapterA
                CommonUtils.showShorMsg(mainActivity, "删除成功");
            }
        }
    }

    @OnClick(R.id.e_search)
    public void search() {
        String qr = eQR.getText().toString().trim();
        if (qr == null || qr.length() == 0) {
            CommonUtils.showShorMsg(mainActivity, "二维码不可为空");
            return;
        }
        getQr(qr);
    }

    @OnClick(R.id.e_scan)
    public void scan() {
        scanUtil.startScan();//二维扫描
    }

    private void uhf() {
        //超高频
        if (App.mUhfrManager == null) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        if (!isReader) {
            startScanLabels();//开始盘点
        } else {
            stopScanLabels();//停止盘点
        }
    }

    //开始盘存
    private void startScanLabels() {
        handler1.postDelayed(runnable_MainActivity, 0);
        eScan.setText("结束扫描");
        isReader = true;
    }

    //结束盘存
    private void stopScanLabels() {
        if (App.isConnectUHF) {
            if (isReader) {
                handler1.removeCallbacks(runnable_MainActivity);
                isReader = false;
                eScan.setText("扫描TID");
            }
        } else {
            CommonUtils.showLonMsg(mainActivity, "通讯超时");
            return;
        }
        isReader = false;
    }

    private Runnable runnable_MainActivity = new Runnable() {
        @Override
        public void run() {
            if (App.mUhfrManager == null) {
                stopScanLabels();
                return;
            }
            List<Reader.TAGINFO> listTag;
            listTag = App.mUhfrManager.tagEpcTidInventoryByTimer((short) 50);//开始读卡
            if (listTag != null && !listTag.isEmpty()) {
                for (Reader.TAGINFO taginfo : listTag) {
                    Map<String, data> infoMap = pooled6cData(taginfo);
                    dataList.clear();
                    das.clear();
                    dataList.addAll(infoMap.values());
                    das.addAll(dasM.values());
                }
            }
            adapter.notifyDataSetChanged();//刷新adapter
            elseAdapter.notifyDataSetChanged();
            handler1.postDelayed(runnable_MainActivity, 0);
        }
    };

    public Map<String, data> pooled6cData(Reader.TAGINFO info) {
        String epcAndTid = Tools.Bytes2HexString(info.EpcId, info.EpcId.length);
        Log.i("Inv", "[pooled6cData] tag epc: " + epcAndTid);
        if (info.EmbededData != null) {
            epcAndTid = Tools.Bytes2HexString(info.EmbededData, info.EmbededData.length);
            Log.i("Inv", "[pooled6cData] tag tid: " + epcAndTid);
            if (TextUtils.isEmpty(epcAndTid)) {
                return dataMap;
            }
        } else {
            Log.i("Inv", "[pooled6cData] drop null tid tag");
            return dataMap;
        }

        if (dataMap.containsKey(epcAndTid)) {
        } else {
            data tag = dataDao.getData(epcAndTid);
            if (tag == null || tag.getId() == 0) {
                data da = new data();
                da.setTID(epcAndTid);
                da.setCondition("未绑定");
                dasM.put(epcAndTid, da);
            } else if (tag.getId() != 0 && tag.getCondition().equals("已入库")) {
                dasM.put(epcAndTid, tag);
            } else if (tag.getId() != 0 && tag.getCondition().equals("已出库")) {
                dasM.put(epcAndTid, tag);
            } else {
                dataMap.put(epcAndTid, tag);
            }
        }
        return dataMap;
    }

    @OnClick(R.id.e_batch)
    public void ebatch() {
        if (!mainActivity.isFinishing()) {
            inputDialog.show();
        }
    }

    @OnClick(R.id.e_clear)
    public void clear() {
        initPane();
    }

    //清空
    private void initPane() {
        dataMap.clear();
        dataList.clear();
        eQR.setText("");
        adapter.notifyDataSetChanged();
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //批量入库
    private void batch() {
        String item = in_batch_B.getText().toString().trim();
        if (dataList != null && dataList.size() > 0) {
            if (item == null || item.length() == 0) {
                CommonUtils.showShorMsg(mainActivity, "批次号不可为空");
                return;
            }
            for (int i = 0; i < dataList.size(); i++) {
                int j = dataDao.batch(dataList.get(i).getId(), "已入库", dateFormat.format(new Date()), item);
                if (j == 0) {
                    CommonUtils.showShorMsg(mainActivity, "【" + dataList.get(i).getTID() + "】入库失败,请重新扫描");
                    continue;
                }
            }
            inputDialog.dismiss();
            CommonUtils.showShorMsg(mainActivity, "入库成功");
            initPane();
        } else {
            CommonUtils.showShorMsg(mainActivity, "请先扫描二维码");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("pang", "onResume()");
        if (App.mUhfrManager != null) {
            App.mUhfrManager.setCancleInventoryFilter();
        }
        registerKeyCodeReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("pang", "onPause()");
        //
        stopScanLabels();
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
                    case KeyEvent.KEYCODE_F7://H3100
                        scan();
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
                    eQR.setText(barcode);
                    getQr(barcode);
                }
            }
        }
    };

    //根据QR查询
    private void getQr(String qr) {
        List<data> list = dataDao.getDatas(qr);
        if (list == null || list.size() == 0) {
            CommonUtils.showShorMsg(mainActivity, "查询为空");
            return;
        }
        dataList.clear();
        das.clear();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() != 0 && list.get(i).getCondition().equals("已入库")) {
                dasM.put(list.get(i).getTID(), list.get(i));
            } else if (list.get(i).getId() != 0 && list.get(i).getCondition().equals("已出库")) {
                dasM.put(list.get(i).getTID(), list.get(i));
            } else {
                dataMap.put(list.get(i).getTID(), list.get(i));
            }
        }
        dataList.addAll(dataMap.values());
        das.addAll(dasM.values());
        adapter.notifyDataSetChanged();//刷新adapter
        elseAdapter.notifyDataSetChanged();
    }
}