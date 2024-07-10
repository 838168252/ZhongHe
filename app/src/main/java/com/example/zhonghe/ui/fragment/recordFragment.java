package com.example.zhonghe.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.zhonghe.Adapter.dataBtn2ClickListener;
import com.example.zhonghe.Adapter.dataCAdapter;
import com.example.zhonghe.MainActivity;
import com.example.zhonghe.R;
import com.example.zhonghe.SQLite.dataDao;
import com.example.zhonghe.pojo.data;
import com.example.zhonghe.ui.base.BaseFragment;
import com.example.zhonghe.util.App;
import com.example.zhonghe.util.CommonUtils;
import com.example.zhonghe.util.FileChooseUtil;
import com.example.zhonghe.util.ScanUtil;
import com.example.zhonghe.util.SheetHelper;
import com.example.zhonghe.util.Util;
import com.uhf.api.cls.Reader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.serialport.Tools;


public class recordFragment extends BaseFragment {
    @BindView(R.id.r_list)
    ListView rList;
    @BindView(R.id.r_bd)
    TextView rBd;
    @BindView(R.id.r_rk)
    TextView rRk;
    @BindView(R.id.r_ck)
    TextView rCk;
    @BindView(R.id.r_QR)
    EditText rQr;
    @BindView(R.id.r_search)
    Button rSearch;
    @BindView(R.id.r_enter)
    Button rEnter;
    @BindView(R.id.r_leave)
    Button rLeave;
    private static final int REQUEST_CODE = 1;
    private MainActivity mainActivity;
    private dataDao dataDao;
    private dataCAdapter adapter;
    private Handler mainHandler; //主线程
    private int page = 1;
    private boolean mReceiverQR = false; //广播接收标识-二维码
    private boolean mReceiverTag = false;   //广播接受者标识
    private KeyReceiver keyReceiver;
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
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        ButterKnife.bind(this, view);

        initView();
        showLvData(page);
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
        //获取主线程
        mainHandler = new Handler();
        //初始化扫描
        scanUtil = ScanUtil.getInstance(mainActivity);

    }
    //显示列表数据的方法
    private void showLvData(int page) {
        if (page == 1) {
            rBd.setTextColor(getResources().getColor(R.color.colorGreen));
            rRk.setTextColor(getResources().getColor(R.color.colorA8));
            rCk.setTextColor(getResources().getColor(R.color.colorA8));
            List<data> list = dataDao.getListData("已绑定");
            ListViewA(list);
        } else if (page == 2) {
            rBd.setTextColor(getResources().getColor(R.color.colorA8));
            rRk.setTextColor(getResources().getColor(R.color.colorGreen));
            rCk.setTextColor(getResources().getColor(R.color.colorA8));
            List<data> list = dataDao.getListData("已入库");
            ListViewA(list);
        } else if (page == 3) {
            rBd.setTextColor(getResources().getColor(R.color.colorA8));
            rRk.setTextColor(getResources().getColor(R.color.colorA8));
            rCk.setTextColor(getResources().getColor(R.color.colorGreen));
            List<data> list = dataDao.getListData("已出库");
            ListViewA(list);
        }
    }
    //列表显示数据适配器
    private void ListViewA(List<data> data) {
        if (data != null) {
            adapter = new dataCAdapter(mainActivity, data);
            rList.setAdapter(adapter);
        }
        adapter.setDataBtn2ClickListener(new dataBtn2ClickListener() {
            @Override
            public void dataBtn2ClickListener(View view, int position) {
                data item = data.get(position);
                Tooltip(item.getTID());
            }
        });
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
    //删除
    private void remove(String item) {

        int i = dataDao.detele2(item);
        if (i > 0) {
            CommonUtils.showShorMsg(mainActivity, "删除成功");
            showLvData(page);
        } else {
            CommonUtils.showShorMsg(mainActivity, "删除失败");

        }
    }
    @OnClick(R.id.r_bd)
    public void rbd(){
        page = 1;
        showLvData(page);
    }
    @OnClick(R.id.r_rk)
    public void rrk(){
        page = 2;
        showLvData(page);
    }
    @OnClick(R.id.r_ck)
    public void rck(){
        page = 3;
        showLvData(page);
    }
    @OnClick(R.id.r_leave)
    public void rLeave(){
        fab_excel();
    }
    @OnClick(R.id.r_enter)
    public void rEnter(){
        excel_to_channel();
    }
    @OnClick(R.id.r_search)
    public void rSearch(){
        search();
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    //导出
    public void fab_excel() {
        List<data> list = dataDao.all();
        try {
            String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            int permission = ActivityCompat.checkSelfPermission(mainActivity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mainActivity, PERMISSIONS_STORAGE, REQUEST_CODE);
            } else {
                String[] title = {"序号", "TID", "二维码", "批次号", "货物种类", "备注", "时间", "状态"};
                boolean isSuccess = SheetHelper.exportExcel(title, list, "Download", "仓管资料" + dateFormat.format(new Date()), mainActivity, true);
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSuccess) {
                            CommonUtils.showShorMsg(mainActivity, "导出成功");
                        } else {
                            CommonUtils.showShorMsg(mainActivity, "导出失败");
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //导入
    public void excel_to_channel() {
        //首先调用系统文件管理器
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1000);

    }

    //接受管理器选中的文件
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
                Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
                String absolutePath = FileChooseUtil.uriToPath(mainActivity, uri);
                Log.d("选择了文件", "文件路径：" + absolutePath);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> strings = SheetHelper.readExcel(absolutePath);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (strings != null) {
                                    for (String string : strings) {
                                        data item = new data();
                                        String[] split = string.split("&&");
                                        item.setTID(split[0]);
                                        item.setQR(split[1]);
                                        item.setBatch(split[2]);
                                        item.setType(split[3]);
                                        item.setComment(split[4]);
                                        item.setTime(split[5]);
                                        item.setCondition(split[6]);
                                        dataDao.add(item);
                                    }
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            CommonUtils.showShorMsg(mainActivity, "导入成功");
                                            showLvData(page);
                                        }
                                    });
                                } else {
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            CommonUtils.showShorMsg(mainActivity, "导入失败");
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                }).start();
            }
        } catch (Exception e) {
            CommonUtils.showShorMsg(mainActivity, "导入异常");
            e.printStackTrace();
        }
    }

    private void search() {
        String i = rQr.getText().toString().trim();
        if (i.length() > 0 && i != null) {
            List<data> list = dataDao.getDatas(i);
            if (list != null && list.size() > 0) {
                ListViewA(list);
            } else {
                CommonUtils.showShorMsg(mainActivity, "未查询出信息!");
            }
        }else {
            CommonUtils.showShorMsg(mainActivity, "搜索内容不可为空!");

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
        if (mReceiverTag) {   //判断广播是否注册
            mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
            mainActivity.unregisterReceiver(keyReceiver);
        }
        if (mReceiverQR) {
            mReceiverQR = false;   //赋值为false 表示该广播已被注销
            mainActivity.unregisterReceiver(receiver);
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
                    rQr.setText(barcode);
                    getQR(barcode);
                }
            }
        }
    };
    //根据QR查询本地库
    private void getQR(String qr) {
        List<data> list = dataDao.getDatas(qr);
        if (list != null && list.size() > 0) {
            ListViewA(list);
        } else {
            CommonUtils.showShorMsg(mainActivity, "未查询出信息!");
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
                        GetTid();
                        break;
                }
            }
        }
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
            rQr.setText(tid);
            getQR(tid);
        }
    }
}