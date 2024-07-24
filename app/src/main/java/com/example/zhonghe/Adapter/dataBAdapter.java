package com.example.zhonghe.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;


import com.example.zhonghe.R;
import com.example.zhonghe.pojo.data;

import java.util.List;

public class dataBAdapter extends BaseAdapter {
    private ListView lv_data;
    private Context context;//上下文信息
    private List<data> dataList; //信息数据集合
    private boolean isShow = false;

    public dataBAdapter(ListView lv_data, Context context, List<data> dataList) {
        this.lv_data = lv_data;
        this.context = context;
        this.dataList = dataList;
    }
    public boolean isShow() {
        return isShow;
    }
    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }


    /**
     * 获取数据中要在listview中显示的条目
     *
     * @return 返回数据的条目
     */

    @Override
    public int getCount() {
        return this.dataList != null ? this.dataList.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_datalist_b, null);
            viewHolder.a_TID = convertView.findViewById(R.id.a_TID);
            viewHolder.a_QR = convertView.findViewById(R.id.a_QR);
            viewHolder.a_batch = convertView.findViewById(R.id.a_batch);
            viewHolder.a_type = convertView.findViewById(R.id.a_type);
            viewHolder.a_comment = convertView.findViewById(R.id.a_comment);
            viewHolder.a_but = convertView.findViewById(R.id.a_but);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();//处理Listview加载重复
        }
        if (viewHolder != null) {
            data item = dataList.get(position);
            viewHolder.a_TID.setText(item.getTID() != null ? item.getTID() : "");
            viewHolder.a_QR.setText(item.getQR() != null ? item.getQR() : "");
            viewHolder.a_batch.setText(item.getBatch() != null ? item.getBatch() : "");
            viewHolder.a_type.setText(item.getType() != null ? item.getType() : "");
            viewHolder.a_comment.setText(item.getComment() != null ? item.getComment() : "");

            if (isShow) {
                viewHolder.a_but.setVisibility(View.VISIBLE);
            } else {
                viewHolder.a_but.setVisibility(View.GONE);
            }

            viewHolder.a_but.setChecked(item.getChecked());
            //listView单个条目事件监听
            lv_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ViewHolder viewHolder1 = (ViewHolder) view.getTag();
                    //切换条目上复选框的选中状态
                    viewHolder1.a_but.toggle();
                    dataList.get(position).setChecked(viewHolder1.a_but.isChecked());
                    parent.getItemAtPosition(position);
                }
            });
        }

        return convertView;
    }

    private class ViewHolder {
        private TextView a_TID, a_QR,a_batch,a_type,a_comment;
        private CheckBox a_but;
    }


}
