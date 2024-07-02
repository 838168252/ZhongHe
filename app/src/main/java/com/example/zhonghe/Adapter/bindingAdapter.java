package com.example.zhonghe.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.zhonghe.R;
import com.example.zhonghe.pojo.TagInfo;

import java.util.List;

public class bindingAdapter extends BaseAdapter {
    private Context context;//上下文信息
    private List<TagInfo> dataList; //信息数据集合

    public bindingAdapter(Context context, List<TagInfo> dataList) {
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
            convertView = View.inflate(context, R.layout.item_bindinglist, null);
            viewHolder.b_serial = convertView.findViewById(R.id.b_serial);
            viewHolder.b_TID = convertView.findViewById(R.id.b_TID);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();//处理Listview加载重复
        }
        if (viewHolder != null) {
            TagInfo item = dataList.get(position);
            viewHolder.b_serial.setText(position + 1 + "");
            viewHolder.b_TID.setText(item.getTid() != null ? item.getTid() : "");
        }
        return convertView;
    }

    private class ViewHolder {
        private TextView b_serial, b_TID;
    }
}
