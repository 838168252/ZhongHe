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


import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhonghe.Adapter.dataBAdapter;
import com.example.zhonghe.Adapter.dataBtn2ClickListener;
import com.example.zhonghe.Adapter.elseAdapter;
import com.example.zhonghe.MainActivity;
import com.example.zhonghe.R;
import com.example.zhonghe.SQLite.dataDao;
import com.example.zhonghe.pojo.data;
import com.example.zhonghe.pojo.power;
import com.example.zhonghe.ui.base.BaseFragment;
import com.example.zhonghe.util.App;
import com.example.zhonghe.util.CommonUtils;
import com.example.zhonghe.util.SPDataUtils;
import com.example.zhonghe.util.ScanUtil;
import com.example.zhonghe.util.SharedUtil;
import com.uhf.api.cls.Reader;

import org.w3c.dom.ls.LSOutput;

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
    @BindView(R.id.e_amount1)
    TextView eAmount1;
    @BindView(R.id.e_amount2)
    TextView eAmount2;
    @BindView(R.id.che_all)
    CheckBox cheAll;
    @BindView(R.id.e_delete)
    Button eDelete;
    @BindView(R.id.hide)
    TextView hide;
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
    Reader.READER_ERR err;
    private SharedUtil sharedUtil;

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
        setPower();
        initlistener();
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
        sharedUtil = new SharedUtil(mainActivity);
        //隐藏控件
        cheAll.setVisibility(View.INVISIBLE);
        eDelete.setVisibility(View.INVISIBLE);
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
            eAmount1.setText(dataList.size() + "");
            adapter = new dataBAdapter(eListA, mainActivity, dataList);
            eListA.setAdapter(adapter);
        }
    }

    //列表显示数据适配器B
    private void ListViewB() {
        if (das != null) {
            eAmount2.setText(das.size() + "");
            elseAdapter = new elseAdapter(mainActivity, das);
            elistB.setAdapter(elseAdapter);
        }
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
//        scanUtil.startScan();//二维扫描
        uhf();
    }

    /**
     * 初始化事件监听方法
     */
    private void initlistener() {
        /**
         * 全选复选框设置事件监听
         */
        cheAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (dataList.size() != 0) {//判断列表中是否有数据
                    if (isChecked) {
                        for (int i = 0; i < dataList.size(); i++) {
                            dataList.get(i).setChecked(true);
                        }
                        //通知适配器更新UI
                        adapter.notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < dataList.size(); i++) {
                            dataList.get(i).setChecked(false);
                        }
                        //通知适配器更新UI
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        //删除按钮点击事件
        eDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建一个要删除内容的集合，不能直接在数据源data集合中直接进行操作，否则会报异常
                List<data> deleSelect = new ArrayList<data>();

                //把选中的条目要删除的条目放在deleSelect这个集合中
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).getChecked()) {
                        dataMap.remove(dataList.get(i).getTID());//删除Map中的元素
                        deleSelect.add(dataList.get(i));
                    }
                }
                //判断用户是否选中要删除的数据及是否有数据
                if (deleSelect.size() != 0 && dataList.size() != 0) {
                    //从数据源data中删除数据
                    dataList.removeAll(deleSelect);
                    eAmount1.setText(dataList.size() + "");
                    //把deleSelect集合中的数据清空
                    deleSelect.clear();
                    //把全选复选框设置为false
                    cheAll.setChecked(false);
                    //通知适配器更新UI
                    adapter.notifyDataSetChanged();
                } else if (dataList.size() == 0) {
                    CommonUtils.showShorMsg(mainActivity, "没有要删除的数据");
                } else if (deleSelect.size() == 0) {
                    CommonUtils.showShorMsg(mainActivity, "请选中要删除的数据");
                }
            }
        });

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
        eScan.setText("结束");
        isReader = true;
    }

    //结束盘存
    private void stopScanLabels() {
        if (App.isConnectUHF) {
            if (isReader) {
                handler1.removeCallbacks(runnable_MainActivity);
                isReader = false;
                eScan.setText("扫描");
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
            eAmount1.setText(dataList.size() + "");
            eAmount2.setText(dasM.size() + "");
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
            tag.setChecked(false);
            if (tag == null || tag.getId() == 0) {
                data da = new data();
                da.setTID(epcAndTid);
                da.setCondition("未绑定");
                dasM.put(epcAndTid, da);
            } else if (tag.getId() != 0 && tag.getCondition().equals("已入库")) {
                dasM.put(epcAndTid, tag);
            }
//            else if (tag.getId() != 0 && tag.getCondition().equals("已出库")) {
//                dasM.put(epcAndTid, tag);
//            }
            else {
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

    @OnClick(R.id.hide)
    public void hide() {
        boolean isShow = adapter.isShow();
        adapter.setShow(!isShow);
        adapter.notifyDataSetChanged();
        cheAll.setVisibility(View.GONE);
        HideAndShow(isShow);
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
        eAmount1.setText(dataList.size() + "");
        eAmount2.setText(dasM.size() + "");
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
            } else {
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
            list.get(i).setChecked(false);//给复选框状态为false
            if (list.get(i).getId() != 0 && list.get(i).getCondition().equals("已入库")) {
                dasM.put(list.get(i).getTID(), list.get(i));
            }
//            else if (list.get(i).getId() != 0 && list.get(i).getCondition().equals("已出库")) {
//                dasM.put(list.get(i).getTID(), list.get(i));
//            }
            else {
                dataMap.put(list.get(i).getTID(), list.get(i));
            }
        }
        dataList.addAll(dataMap.values());
        das.addAll(dasM.values());
        adapter.notifyDataSetChanged();//刷新adapter
        elseAdapter.notifyDataSetChanged();
        eAmount1.setText(dataList.size() + "");
        eAmount2.setText(dasM.size() + "");
    }

    //设置功率
    private void setPower() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        power p = SPDataUtils.getInfo(mainActivity);
        err = App.mUhfrManager.setPower(p.getS2(), p.getS2());
        if (err == Reader.READER_ERR.MT_OK_ERR) {
            sharedUtil.savePower(p.getS2());
        } else {
            //5101 仅支持30db
            CommonUtils.showShorMsg(mainActivity, "功率设置失败");
        }
    }

    private void HideAndShow(boolean t) {
        if (t) {
            //隐藏控件
            cheAll.setVisibility(View.INVISIBLE);
            eDelete.setVisibility(View.INVISIBLE);
        } else {
            //显示控件
            cheAll.setVisibility(View.VISIBLE);
            eDelete.setVisibility(View.VISIBLE);
        }

    }
}