package com.example.zhonghe.SQLite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.example.zhonghe.pojo.data;

import java.util.ArrayList;
import java.util.List;

public class dataDao {
    private SQLhelper sqLhelper;
    private SQLiteDatabase db;

    public dataDao(Context context) {
        sqLhelper = new SQLhelper(context);
    }

    //添加
    public int add(data item) {
        int iRow = 0;
        try {
            db = sqLhelper.getWritableDatabase();
            db.execSQL("insert OR IGNORE into datas (" +
                    "TID" +
                    ",QR" +
                    ",batch" +
                    ",type" +
                    ",comment" +
                    ",time" +
                    ",condition) values (?,?,?,?,?,?,?)", new Object[]
                    {
                            item.getTID(),
                            item.getQR(),
                            item.getBatch(),
                            item.getType(),
                            item.getComment(),
                            item.getTime(),
                            item.getCondition()
                    });
            iRow = 1;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return iRow;
    }

    //查询所有(data)
    @SuppressLint("Range")
    public List<data> all() {
        List<data> list = new ArrayList<>();
        db = sqLhelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from datas ", null);
        while (cursor.moveToNext()) {
            list.add(new data(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("TID")),
                    cursor.getString(cursor.getColumnIndex("QR")),
                    cursor.getString(cursor.getColumnIndex("batch")),
                    cursor.getString(cursor.getColumnIndex("type")),
                    cursor.getString(cursor.getColumnIndex("comment")),
                    cursor.getString(cursor.getColumnIndex("time")),
                    cursor.getString(cursor.getColumnIndex("condition"))
                    ));
        }
        return list;
    }

    //删除
    public int detele() {
        int iRow = 0;
        try {
            db = sqLhelper.getWritableDatabase();
            //db.delete("qz", "cardno=?", new String[]{"123456789"});
            db.execSQL("delete from datas where 1 = 1", new String[]{});
            iRow = 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return iRow;
    }
    //根据epc删除
    public int detele2(String TID) {
        int iRow = 0;
        try {
            db = sqLhelper.getWritableDatabase();
            //db.delete("qz", "cardno=?", new String[]{"123456789"});
            db.execSQL("delete from datas where TID = ?", new String[]{String.valueOf(TID)});
            iRow = 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return iRow;
    }

    //根据rfid查询本地信息
    public data getData(String tid){
        data da = new data();
        db = sqLhelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from datas where TID = ?", new String[]{String.valueOf(tid)});
        while (cursor.moveToNext()) {
            da.setId(cursor.getInt(cursor.getColumnIndex("id")));
            da.setTID(cursor.getString(cursor.getColumnIndex("TID")));
            da.setQR(cursor.getString(cursor.getColumnIndex("QR")));
            da.setBatch(cursor.getString(cursor.getColumnIndex("batch")));
            da.setType(cursor.getString(cursor.getColumnIndex("type")));
            da.setComment(cursor.getString(cursor.getColumnIndex("comment")));
            da.setTime(cursor.getString(cursor.getColumnIndex("time")));
            da.setCondition(cursor.getString(cursor.getColumnIndex("condition")));
        }
        return da;
    }

    //根据QR查询本地信息
    public List<data> getDatas(String qr){
        List<data> list = new ArrayList<>();
        db = sqLhelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from datas where QR like ? OR TID like ?", new String[]{"%"+ String.valueOf(qr)+"%","%"+ String.valueOf(qr)+"%"});
        while (cursor.moveToNext()) {
            list.add(new data(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("TID")),
                    cursor.getString(cursor.getColumnIndex("QR")),
                    cursor.getString(cursor.getColumnIndex("batch")),
                    cursor.getString(cursor.getColumnIndex("type")),
                    cursor.getString(cursor.getColumnIndex("comment")),
                    cursor.getString(cursor.getColumnIndex("time")),
                    cursor.getString(cursor.getColumnIndex("condition"))
            ));
        }
        return list;
    }

    //根据id修改字段
    public int batch(int id, String condition, String time, String batch) {
        int iRow = 0;
        try {
            db = sqLhelper.getWritableDatabase();
            db.execSQL("UPDATE datas set batch = ?, time = ?,condition = ? where id = ?", new String[]{String.valueOf(batch), String.valueOf(time), String.valueOf(condition), String.valueOf(id)});
            iRow = 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return iRow;
    }
    //根据id修改字段
    public int batch2(int id, String condition, String time) {
        int iRow = 0;
        try {
            db = sqLhelper.getWritableDatabase();
            db.execSQL("UPDATE datas set  time = ?,condition = ? where id = ?", new String[]{String.valueOf(time), String.valueOf(condition), String.valueOf(id)});
            iRow = 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return iRow;
    }
    //根据Tid修改字段
    public int batch3(String Tid1, String Tid2) {
        int iRow = 0;
        try {
            db = sqLhelper.getWritableDatabase();
            db.execSQL("UPDATE datas set TID = ? where TID = ?", new String[]{String.valueOf(Tid2), String.valueOf(Tid1)});
            iRow = 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return iRow;
    }
    //根据condition查询本地信息
    public List<data> getListData(String condition){
        List<data> list = new ArrayList<>();
        db = sqLhelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from datas where condition = ?", new String[]{String.valueOf(condition)});
        while (cursor.moveToNext()) {
            list.add(new data(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("TID")),
                    cursor.getString(cursor.getColumnIndex("QR")),
                    cursor.getString(cursor.getColumnIndex("batch")),
                    cursor.getString(cursor.getColumnIndex("type")),
                    cursor.getString(cursor.getColumnIndex("comment")),
                    cursor.getString(cursor.getColumnIndex("time")),
                    cursor.getString(cursor.getColumnIndex("condition"))
            ));
        }
        return list;
    }
}
