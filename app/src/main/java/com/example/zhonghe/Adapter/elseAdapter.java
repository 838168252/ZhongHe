package com.example.zhonghe.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.zhonghe.R;
import com.example.zhonghe.pojo.data;

import java.util.List;

public class elseAdapter extends BaseAdapter {
    private Context context;//上下文信息
    private List<data> dataList; //信息数据集合

    public elseAdapter(Context context, List<data> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
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
            convertView = View.inflate(context, R.layout.item_elselist, null);
            viewHolder.el_serial = convertView.findViewById(R.id.el_serial);
            viewHolder.el_TID = convertView.findViewById(R.id.el_TID);
            viewHolder.el_condition = convertView.findViewById(R.id.el_condition);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();//处理Listview加载重复
        }
        if (viewHolder != null) {
            data item = dataList.get(position);
            viewHolder.el_serial.setText(position + 1 + "");
            viewHolder.el_TID.setText(item.getTID() != null ? item.getTID() : "");
            viewHolder.el_condition.setText(item.getCondition() != null ? item.getCondition() : "");
        }
        return convertView;
    }
    private class ViewHolder {
        private TextView el_serial, el_TID,el_condition;
    }
}
