 package com.example.zhonghe.ui.fragment;

import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

public class leaveFragment extends BaseFragment {
    @BindView(R.id.l_listA)
    ListView lListA;
    @BindView(R.id.l_listB)
    ListView lListB;
    @BindView(R.id.l_scan)
    Button lScan;
    @BindView(R.id.l_batch)
    Button lBatch;
    @BindView(R.id.l_clear)
    Button lClear;
    @BindView(R.id.l_QR)
    EditText lQR;
    @BindView(R.id.l_search)
    Button lSearch;
    @BindView(R.id.l_amount1)
    TextView lAmount1;
    @BindView(R.id.l_amount2)
    TextView lAmount2;
    @BindView(R.id.che_all)
    CheckBox cheAll;
    @BindView(R.id.l_delete)
    Button lDelete;
    @BindView(R.id.hide)
    TextView hide;

    private MainActivity mainActivity;
    private dataBAdapter adapter;
    private elseAdapter elseAdapter;
    private ImageView l_but_switchover;
    private int patternType = 1;//1 = 超高频、0 = 条形码
    private ScanUtil scanUtil;
    private boolean isReader = false;//是否在读
    private Handler handler1 = new Handler();
    private Map<String, data> dataMap = new LinkedHashMap<String, data>();//
    private Map<String, data> dasM = new LinkedHashMap<String, data>();//
    private List<data> dataList = new ArrayList<data>();//
    private List<data> das = new ArrayList<>();
    private dataDao dataDao;
    private KeyReceiver keyReceiver;
    private boolean mReceiverTag = false;   //广播接受者标识
    private boolean mReceiverQR = false; //广播接收标识-二维码
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

        View view = inflater.inflate(R.layout.fragment_leave, container, false);
        ButterKnife.bind(this, view);

        initView();
        ListViewA();
        ListViewB();
        setPower();
        initlistener();
        return view;
    }

    private void initView() {
        //二维码广播接收
        ContextWrapper contextWrapper = new ContextWrapper(mainActivity);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");//getStringExtra的参数
        mReceiverQR = true;
        contextWrapper.registerReceiver(receiver, filter);
        //本地库
        dataDao = new dataDao(mainActivity);
        //初始化扫描
        scanUtil = ScanUtil.getInstance(mainActivity);
        sharedUtil = new SharedUtil(mainActivity);
        //隐藏控件
        cheAll.setVisibility(View.INVISIBLE);
        lDelete.setVisibility(View.INVISIBLE);

    }

    //列表显示数据适配器A
    private void ListViewA() {
        if (dataList != null) {
            lAmount1.setText(dataList.size()+"");
            adapter = new dataBAdapter(lListA,mainActivity, dataList);
            lListA.setAdapter(adapter);
        }
    }

    //列表显示数据适配器B
    private void ListViewB() {
        if (das != null) {
            lAmount2.setText(das.size()+"");
            elseAdapter = new elseAdapter(mainActivity, das);
            lListB.setAdapter(elseAdapter);
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

    @OnClick(R.id.l_search)
    public void search() {
        String qr = lQR.getText().toString().trim();
        if (qr == null || qr.length() == 0) {
            CommonUtils.showShorMsg(mainActivity, "二维码不可为空");
            return;
        }
        QR(qr);
    }

    @OnClick(R.id.l_scan)
    public void lscan() {
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
//                else {//若列表中没有数据则隐藏全选复选框
//                    cheAll.setVisibility(View.GONE);
//                }
            }
        });
        //删除按钮点击事件
        lDelete.setOnClickListener(new View.OnClickListener() {
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
                    lAmount1.setText(dataList.size() + "");
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
        if (patternType == 0) {
            scanUtil.startScan();//二维扫描
        } else if (patternType == 1) {
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
    }

    //开始盘存
    private void startScanLabels() {
        handler1.postDelayed(runnable_MainActivity, 0);
        lScan.setText("结束");
        isReader = true;
    }

    //结束盘存
    private void stopScanLabels() {
        if (App.isConnectUHF) {
            if (isReader) {
                handler1.removeCallbacks(runnable_MainActivity);
                isReader = false;
                lScan.setText("扫描");
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
                    dataList.addAll(infoMap.values());
                    das.clear();
                    das.addAll(dasM.values());
                }
            }
            adapter.notifyDataSetChanged();//刷新adapter
            elseAdapter.notifyDataSetChanged();
            lAmount1.setText(dataList.size()+"");
            lAmount2.setText(dasM.size()+"");
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
            } else if (tag.getId() != 0 && tag.getCondition().equals("已绑定")) {
                tag.setCondition("未入库");
                dasM.put(epcAndTid, tag);
            } else if (tag.getId() != 0 && tag.getCondition().equals("已出库")) {
                dasM.put(epcAndTid, tag);
            } else {
                dataMap.put(epcAndTid, tag);
            }
        }
        return dataMap;
    }


    @OnClick(R.id.l_batch)
    public void lbatch() {
        if (isReader) {
            CommonUtils.showShorMsg(mainActivity, "正在扫描,请先停止");
            return;
        }
        batch();
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //批量出库
    private void batch() {
        if (dataList != null && dataList.size() > 0) {
            for (int i = 0; i < dataList.size(); i++) {
                int j = dataDao.batch2(dataList.get(i).getId(), "已出库", dateFormat.format(new Date()));
                if (j == 0) {
                    CommonUtils.showShorMsg(mainActivity, "【" + dataList.get(i).getTID() + "】出库失败,请重新扫描");
                    continue;
                }
            }
            CommonUtils.showShorMsg(mainActivity, "出库成功");
            initPane();
        } else {
            CommonUtils.showShorMsg(mainActivity, "请先扫描TID");
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

    //清空
    private void initPane() {
        dataMap.clear();
        dataList.clear();
        lQR.setText("");
        adapter.notifyDataSetChanged();
        lAmount1.setText(dataList.size()+"");
        lAmount2.setText(dasM.size()+"");
    }

    @OnClick(R.id.l_clear)
    public void clear() {
        initPane();
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
                        lscan();
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
                    lQR.setText(barcode);
                    QR(barcode);
                }
            }
        }
    };

    //根据QR查询本地库
    private void QR(String qr) {
        List<data> tag = dataDao.getDatas(qr);
        if (tag == null || tag.size() == 0) {
            CommonUtils.showShorMsg(mainActivity, "查询为空");
            return;
        }
        dataList.clear();
        das.clear();
        for (int i = 0; i < tag.size(); i++) {
            data da = tag.get(i);
            da.setChecked(false);//给复选框状态为false
            if (da.getId() != 0 && da.getCondition().equals("已绑定")) {
                da.setCondition("未入库");
                dasM.put(da.getTID(), da);
            } else if (da.getId() != 0 && da.getCondition().equals("已出库")) {
                dasM.put(da.getTID(), da);
            } else {
                dataMap.put(da.getTID(), da);
            }
        }
        das.addAll(dasM.values());
        dataList.addAll(dataMap.values());
        adapter.notifyDataSetChanged();//刷新adapter
        elseAdapter.notifyDataSetChanged();
        lAmount1.setText(dataList.size()+"");
        lAmount2.setText(dasM.size()+"");
    }

    //设置功率
    private void setPower(){
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        power p = SPDataUtils.getInfo(mainActivity);
        err = App.mUhfrManager.setPower(p.getS3(), p.getS3());
        if (err == Reader.READER_ERR.MT_OK_ERR) { 
            sharedUtil.savePower(p.getS3());
        } else {
            //5101 仅支持30db
            CommonUtils.showShorMsg(mainActivity, "功率设置失败");
        }
    }

    private void HideAndShow(boolean t) {
        if (t) {
            //隐藏控件
            cheAll.setVisibility(View.INVISIBLE);
            lDelete.setVisibility(View.INVISIBLE);
        } else {
            //显示控件
            cheAll.setVisibility(View.VISIBLE);
            lDelete.setVisibility(View.VISIBLE);
        }

    }

}