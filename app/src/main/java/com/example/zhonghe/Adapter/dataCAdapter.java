package com.example.zhonghe.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;


import com.example.zhonghe.R;
import com.example.zhonghe.pojo.data;

import java.util.List;

public class dataCAdapter extends BaseAdapter {
    private Context context;//上下文信息
    private List<data> dataList; //信息数据集合
    private dataBtn2ClickListener dataBtn2ClickListener;

    public dataCAdapter(Context context, List<data> dataList) {
        this.context = context;
        this.dataList = dataList;
    }


    public void setDataBtn2ClickListener(dataBtn2ClickListener dataBtn2ClickListener) {
        this.dataBtn2ClickListener = dataBtn2ClickListener;
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
            convertView = View.inflate(context, R.layout.item_datalist_c, null);
            viewHolder.a_TID = convertView.findViewById(R.id.a_TID);
            viewHolder.a_QR = convertView.findViewById(R.id.a_QR);
            viewHolder.a_batch = convertView.findViewById(R.id.a_batch);
            viewHolder.a_type = convertView.findViewById(R.id.a_type);
            viewHolder.a_comment = convertView.findViewById(R.id.a_comment);
            viewHolder.a_time = convertView.findViewById(R.id.a_time);
            viewHolder.a_condition = convertView.findViewById(R.id.a_condition);
            viewHolder.a_but2 = convertView.findViewById(R.id.a_but2);
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
            viewHolder.a_time.setText(item.getTime() != null ? item.getTime() : "");
            viewHolder.a_condition.setText(item.getCondition() != null ? item.getCondition() : "");
            //按钮2
            viewHolder.a_but2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dataBtn2ClickListener.dataBtn2ClickListener(view,position);
                }
            });
        }

        return convertView;
    }

    private class ViewHolder {
        private TextView a_TID, a_QR,a_batch,a_type,a_comment,a_time,a_condition;
        private Button a_but2;
    }
}
