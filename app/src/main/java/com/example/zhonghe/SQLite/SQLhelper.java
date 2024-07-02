package com.example.zhonghe.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLhelper extends SQLiteOpenHelper
{
	public final static String QZ_DBNAME = "ZHWW.db";
	public final static int VERSION = 1;
	public SQLhelper(Context context, String name, CursorFactory factory,
                     int version)
	{
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public SQLhelper(Context context)
	{
		super(context, QZ_DBNAME, null, VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("create table datas (id integer primary key AUTOINCREMENT,TID TEXT(255) UNIQUE  , QR TEXT(255) ,batch varcher(255),type varcher(255),comment varcher(255),time varcher(255),condition varcher(255))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		//数据备份，比如软件升级
		// TODO Auto-generated method stub

	}

}
