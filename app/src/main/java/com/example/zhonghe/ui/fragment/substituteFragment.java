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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.zhonghe.Adapter.dataAdapter;
import com.example.zhonghe.Adapter.dataBtn1ClickListener;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.serialport.Tools;


public class substituteFragment extends BaseFragment implements View.OnClickListener {
    @BindView(R.id.s_listA)
    ListView sListA;
    @BindView(R.id.s_listB)
    ListView sListB;
    @BindView(R.id.s_scan)
    Button sScan;
    @BindView(R.id.s_clear)
    Button sClear;
    @BindView(R.id.s_QR)
    EditText sQR;
    @BindView(R.id.s_search)
    Button sSearch;

    private dataAdapter adapter;
    private elseAdapter elseAdapter;
    private boolean isReader = false;//是否在读
    private boolean window = false;//是否扫描过
    private Handler handler1 = new Handler();
    private Map<String, data> dataMap = new LinkedHashMap<String, data>();//
    private Map<String, data> dasM = new LinkedHashMap<String, data>();//
    private List<data> dataList = new ArrayList<data>();//
    private List<data> das = new ArrayList<>();
    private dataDao dataDao;
    private boolean mReceiverTag = false;   //广播接受者标识
    private boolean mReceiverQR = false; //广播接收标识-二维码
    private KeyReceiver keyReceiver;
    private Dialog inputDialog;
    private List<String> tids;
    private String TID = "";
    private Spinner in_TID;
    private TextView in_TID2;
    private Button in_but1, in_but2;
    private ScanUtil scanUtil;

    private MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_substitute, container, false);
        ButterKnife.bind(this, view);
        initView();
        Dialog();
        ListViewA();
        ListViewB();
        return view;
    }

    private void initView() {
        //本地库
        dataDao = new dataDao(mainActivity);
        //弹窗
        inputDialog = new Dialog(mainActivity);
        inputDialog.setContentView(R.layout.inputdialog3);
        inputDialog.setCanceledOnTouchOutside(false);//点击屏幕dialog不消失
        inputDialog.setCancelable(false);//点击返回键dialog不消失
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
        in_TID = inputDialog.findViewById(R.id.in_TID);
        in_TID2 = inputDialog.findViewById(R.id.in_TID2);
        in_but1 = inputDialog.findViewById(R.id.in_but1);
        in_but1.setOnClickListener(this);
        in_but2 = inputDialog.findViewById(R.id.in_but2);
        in_but2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.in_but1:
                replace();
                break;
            case R.id.in_but2:
                inputDialog.dismiss();
                break;
        }
    }

    //列表显示数据适配器A
    private void ListViewA() {
        if (dataList != null) {
            adapter = new dataAdapter(mainActivity, dataList);
            sListA.setAdapter(adapter);
        }
        adapter.setDataBtn1ClickListener(new dataBtn1ClickListener() {
            @Override
            public void dataBtn1ClickListener(View view, int position) {
                data item = dataList.get(position);
                if (das == null || das.size() == 0) {
                    CommonUtils.showShorMsg(mainActivity, "请先扫描TID");
                    return;
                }
                if (!mainActivity.isFinishing()) {
                    inputDialog.show();
                    in_TID2.setText(item.getTID() + "");
                    spinner();
                }
            }
        });
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
            sListB.setAdapter(elseAdapter);
        }
    }

    //下拉框
    private void spinner() {
        if (null != das && das.size() > 0) {
            getTid();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_item, tids);
            adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
            in_TID.setAdapter(adapter);

            in_TID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    //获取Spinner控件的适配器
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) adapterView.getAdapter();
                    TID = adapter.getItem(i);
                }

                //没有选中时的处理
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } else {
            CommonUtils.showShorMsg(mainActivity, "没有搜索到未绑定TID");
            return;
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
                }).setNegativeButton("否", new DialogInterface.OnClickListener() {//添加返回按钮

            @Override
            public void onClick(DialogInterface dialog, int which) {//响应事件
            }
        }).show();//在按键响应事件中显示此对话框
    }

    //取出tagInfoList中的TID
    private void getTid() {
        tids = new ArrayList<>();
        for (int i = 0; i < das.size(); i++) {
            tids.add(das.get(i).getTID());
        }
    }

    //根据TID清除dataList中的数据
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

    @OnClick(R.id.s_search)
    public void search() {
        String qr = sQR.getText().toString().trim();
        if (qr == null || qr.length() == 0) {
            CommonUtils.showShorMsg(mainActivity, "二维码不可为空");
            return;
        }
        getQr(qr);
    }

    @OnClick(R.id.s_scan)
    public void scan() {
        //超高频
        if (App.mUhfrManager == null) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        if (window == false) {
            CommonUtils.showShorMsg(mainActivity, "请先扫描二维码");
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
        sScan.setText("结束扫描");
        isReader = true;
    }

    //结束盘存
    private void stopScanLabels() {
        if (App.isConnectUHF) {
            if (isReader) {
                handler1.removeCallbacks(runnable_MainActivity);
                isReader = false;
                sScan.setText("扫描TID");
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
                    das.clear();
                    das.addAll(infoMap.values());
                }
            }
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
                return dasM;
            }
        } else {
            Log.i("Inv", "[pooled6cData] drop null tid tag");
            return dasM;
        }

        if (dasM.containsKey(epcAndTid)) {
        } else {
            data tag = dataDao.getData(epcAndTid);
            if (tag == null || tag.getId() == 0) {
                data da = new data();
                da.setTID(epcAndTid);
                da.setCondition("未绑定");
                dasM.put(epcAndTid, da);
            }
        }
        return dasM;
    }

    @OnClick(R.id.s_clear)
    public void clear() {
        if (isReader) {
            CommonUtils.showShorMsg(mainActivity, "正在扫描,请先停止");
            return;
        }
        initPane();
    }

    //清空
    private void initPane() {
        dataMap.clear();
        dataList.clear();
        dasM.clear();
        das.clear();
        window = false;
        sQR.setText("");
        adapter.notifyDataSetChanged();
        elseAdapter.notifyDataSetChanged();
    }

    private void replace() {
        String i = in_TID2.getText().toString().trim();
        int a = dataDao.batch3(i, TID);
        if (a > 0) {
            CommonUtils.showShorMsg(mainActivity, "替换成功,请重新扫描");
            inputDialog.dismiss();
            initPane();
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
                        if (window == false) {
                            scanUtil.startScan();//二维扫描
                        } else {
                            scan();
                        }
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
                    sQR.setText(barcode);
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
        for (int i = 0; i < list.size(); i++) {
            dataMap.put(list.get(i).getTID(), list.get(i));
        }
        dataList.addAll(dataMap.values());
        adapter.notifyDataSetChanged();//刷新adapter
        ChangeState();
    }

    private void ChangeState() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 * 延时改变状态,以免立马触发超高频
                 */
                window = true;
            }
        }, 2000); //延时2秒
    }
}