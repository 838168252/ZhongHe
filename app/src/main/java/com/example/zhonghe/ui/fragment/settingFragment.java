package com.example.zhonghe.ui.fragment;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.zhonghe.BuildConfig;
import com.example.zhonghe.MainActivity;
import com.example.zhonghe.R;
import com.example.zhonghe.pojo.power;
import com.example.zhonghe.ui.base.BaseFragment;
import com.example.zhonghe.util.App;
import com.example.zhonghe.util.CommonUtils;
import com.example.zhonghe.util.LogUtil;
import com.example.zhonghe.util.SPDataUtils;
import com.example.zhonghe.util.SharedUtil;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class settingFragment extends BaseFragment {
    @BindView(R.id.spinner_work_freq)
    Spinner spinnerWorkFreq;
    @BindView(R.id.spinner_power1)
    Spinner spinnerPower1; //绑定功率
    @BindView(R.id.spinner_power2)
    Spinner spinnerPower2;//入库功率
    @BindView(R.id.spinner_power3)
    Spinner spinnerPower3;//出库功率
    @BindView(R.id.spinner_power4)
    Spinner spinnerPower4;//替换功率
    @BindView(R.id.spinner_session)
    Spinner spinnerSession;
    @BindView(R.id.spinner_q_value)
    Spinner spinnerQvalue;
    @BindView(R.id.spinner_inventory_type)
    Spinner spinnerInventoryType;
    @BindView(R.id.button_query_work_freq)
    Button buttonFreqQuery;
    @BindView(R.id.button_set_work_freq)
    Button buttonFreqSet;
    @BindView(R.id.editText_temp)
    EditText editTextTemp;
    @BindView(R.id.button_query_power)
    Button buttonQueryPower;
    @BindView(R.id.button_set_power)
    Button buttonSetPower;
    @BindView(R.id.button_query_inventory_type)
    Button buttonQueryInventory;
    @BindView(R.id.button_set_inventory_type)
    Button buttonSetInventory;
    @BindView(R.id.button_query_session)
    Button buttonQuerySession;
    @BindView(R.id.button_set_session)
    Button buttonSetSession;
    @BindView(R.id.textViewFirmware)
    TextView tvFirmware;
    @BindView(R.id.textViewSoft)
    TextView tvSoft;
    @BindView(R.id.textViewDate)
    TextView tvDate;
    private MainActivity mainActivity;
    //    private SharedPreferences mSharedPreferences;
    private String[] arrayWorkFreq;
    //Session
    private String[] arraySession;
    private String[] arrayPower;
    private String[] arrayQvalue;
    private String[] arrayInventoryType;

    private Reader.Region_Conf workFreq;    //
    private int s1 = 10; //
    private int s2 = 33; //
    private int s3 = 33; //
    private int s4 = 10; //

    private int session = 1; //session
    private int qvalue = 1;//Q
    private int target = 0; //A|B
    private UHFRManager uhfrManager;
    private SharedUtil sharedUtil;
    Reader.READER_ERR err;
    private Resources res;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        initView();
        execute();
        Click();
        versionInformation();
        return view;
    }

    private void initView() {
        sharedUtil = new SharedUtil(mainActivity);
        res = this.getResources();
    }

    @OnClick(R.id.button_query_work_freq)
    void queryFreq() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        String workFreqStr = "";
        Reader.Region_Conf region = App.mUhfrManager.getRegion();
//        if(region.value()==null)
//        LogUtil.e("workFraq = " + region.value());
        if (region == null) {
            CommonUtils.showShorMsg(mainActivity, "查询功率失败");
            return;
        }
        if (region == Reader.Region_Conf.RG_NA) {
            //902_928
            spinnerWorkFreq.setSelection(1);
            workFreqStr = arrayWorkFreq[1];
        } else if (region == Reader.Region_Conf.RG_PRC) {
            //_920_925
            spinnerWorkFreq.setSelection(0);
            workFreqStr = arrayWorkFreq[0];
        } else if (region == Reader.Region_Conf.RG_EU3) {
            //865_867
            spinnerWorkFreq.setSelection(2);
            workFreqStr = arrayWorkFreq[2];
        }
//        CommonUtils.showShorMsg(this, "工作频段:" + workFreqStr);
    }

    @OnClick(R.id.button_query_power)
    void queryPower() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        power p = SPDataUtils.getInfo(mainActivity);
        if (p != null) {
            spinnerPower1.setSelection(p.getS1());
            spinnerPower2.setSelection(p.getS2());
            spinnerPower3.setSelection(p.getS3());
            spinnerPower4.setSelection(p.getS4());
        } else {
            CommonUtils.showShorMsg(mainActivity, "查询失败");
        }


//        int[] powerArray = App.mUhfrManager.getPower();
//        if (powerArray != null && powerArray.length > 0) {
//            LogUtil.e("powerArray = " + powerArray[0]);
//            spinnerPower1.setSelection(powerArray[0]);
//
//            CommonUtils.showShorMsg(mainActivity, "输出功率:" + powerArray[0] + "dB");
//        } else {
//            CommonUtils.showShorMsg(mainActivity, "查询失败");
//        }
    }

    @OnClick(R.id.button_query_session)
    void querySession() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        int session = App.mUhfrManager.getGen2session();
        if (session != -1) {
            spinnerSession.setSelection(session);
//            CommonUtils.showShorMsg(this, "Session:" + session);
        } else {
            CommonUtils.showShorMsg(mainActivity, "查询失败");
        }
        LogUtil.e("session = " + session);

    }

    @OnClick(R.id.button_query_qvalue)
    void queryQvalue() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        int qvalue = App.mUhfrManager.getQvalue();
        if (qvalue != -1) {
            spinnerQvalue.setSelection((qvalue));
//            CommonUtils.showShorMsg(this, "Q = " + qvalue);
        } else {
            CommonUtils.showShorMsg(mainActivity, "查询失败");
        }
        LogUtil.e("qvalue = " + qvalue);

    }

    @OnClick(R.id.button_query_inventory_type)
    void queryInventory() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        target = App.mUhfrManager.getTarget();
        LogUtil.e("Target = " + target);
        if (target != -1) {
            spinnerInventoryType.setSelection(target);
//            CommonUtils.showShorMsg(this, "盘存方式:" + arrayInventoryType[target]);
        } else {
            CommonUtils.showShorMsg(mainActivity, "查询失败");
        }
    }

    @OnClick(R.id.button_set_work_freq)
    void setWorkFreq() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        Log.e("zeng-", "setworkFraq:" + workFreq);
        err = App.mUhfrManager.setRegion(workFreq);
        if (err == Reader.READER_ERR.MT_OK_ERR) {
            CommonUtils.showShorMsg(mainActivity, "设置成功");
            sharedUtil.saveWorkFreq(workFreq.value());
        } else {
            //5101 仅支持30db
            CommonUtils.showShorMsg(mainActivity, "设置失败");
        }
    }

    @OnClick(R.id.button_set_power)
    void setPower() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        boolean i = SPDataUtils.saveInfo(mainActivity, s1, s2, s3, s4);
        if (i == true) {
            CommonUtils.showShorMsg(mainActivity, "设置成功");
        } else {
            CommonUtils.showShorMsg(mainActivity, "设置失败");
        }

//        err = App.mUhfrManager.setPower(s2, s2);
//        if (err == Reader.READER_ERR.MT_OK_ERR) {
//            CommonUtils.showShorMsg(mainActivity, "设置成功");
//            sharedUtil.savePower(s2);
//        } else {
//            //5101 仅支持30db
//            CommonUtils.showShorMsg(mainActivity, "设置失败");
//        }
    }

    @OnClick(R.id.button_set_session)
    void setSession() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        boolean flag = App.mUhfrManager.setGen2session(session);
        if (flag) {
            CommonUtils.showShorMsg(mainActivity, "设置成功");
            sharedUtil.saveSession(session);
        } else {
            CommonUtils.showShorMsg(mainActivity, "设置失败");
        }
    }

    @OnClick(R.id.button_set_qvalue)
    void setQvalue() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        boolean flag = App.mUhfrManager.setQvaule(qvalue);
        if (flag) {
            CommonUtils.showShorMsg(mainActivity, "设置成功");
            sharedUtil.saveQvalue(qvalue);
        } else {
            CommonUtils.showShorMsg(mainActivity, "设置失败");
        }
    }

    @OnClick(R.id.button_set_inventory_type)
    void setTarget() {
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        boolean flag = App.mUhfrManager.setTarget(target);
        Log.e("zeng -", "setTarget:" + target);
        if (flag) {
            CommonUtils.showShorMsg(mainActivity, "设置成功");
            sharedUtil.saveTarget(target);
        } else {
            CommonUtils.showShorMsg(mainActivity, "设置失败");
        }
    }

    //版本信息
    private void versionInformation() {
        String strSoft = BuildConfig.VERSION_NAME;
        tvSoft.setText("  " + strSoft);
        tvDate.setText("  " + "2024-07-04");
        if (!App.isConnectUHF) {
            CommonUtils.showShorMsg(mainActivity, "通讯超时");
            return;
        }
        String version = App.mUhfrManager.getHardware();

        if (version != null && version.length() > 0) {
            tvFirmware.setText("  " + version);
        } else {
            tvFirmware.setText("未寻找到硬件模块");
        }
    }

    private void execute() {
        arrayWorkFreq = res.getStringArray(R.array.work_freq);
        arraySession = res.getStringArray(R.array.session_arrays);
        arrayPower = res.getStringArray(R.array.power_arrays);
        arrayQvalue = res.getStringArray(R.array.q_value_arrays);
        arrayInventoryType = res.getStringArray(R.array.inventory_type_arrays);


        //
        spinnerSession.setSelection(sharedUtil.getPower());
        int freq = sharedUtil.getWorkFreq();
//        Log.e("zeng-","freq:"+freq);
        if (Reader.Region_Conf.valueOf(freq) == Reader.Region_Conf.RG_NA) {
            spinnerWorkFreq.setSelection(2);
        } else if (Reader.Region_Conf.valueOf(freq) == Reader.Region_Conf.RG_PRC) {
            spinnerWorkFreq.setSelection(0);
        } else if (Reader.Region_Conf.valueOf(freq) == Reader.Region_Conf.RG_EU3) {
            spinnerWorkFreq.setSelection(3);
        }
        spinnerSession.setSelection(sharedUtil.getSession());
        spinnerQvalue.setSelection((sharedUtil.getQvalue()));
        spinnerInventoryType.setSelection(sharedUtil.getTarget());


        //
        spinnerWorkFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String workFreqStr = arrayWorkFreq[position];
                switch (position) {
                    case 0:
                        //1_920_925
                        workFreq = Reader.Region_Conf.RG_PRC;
                        break;
                    case 1:
                        //_902_928
                        workFreq = Reader.Region_Conf.RG_NA;
                        break;
                    case 2:
                        //865_867
                        workFreq = Reader.Region_Conf.RG_EU3;
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //
        spinnerPower1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s1 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerPower2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s2 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerPower3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s3 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerPower4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s4 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //session
        spinnerSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                session = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Q
        spinnerQvalue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                qvalue = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //
        spinnerInventoryType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                target = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void Click() {
        queryFreq();
        queryPower();
        querySession();
        queryQvalue();
        queryInventory();

    }

}