package lsxx.waterassistant;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzh on 2017/5/11.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    final String CREATE_TABLE_SQL =
            "create table myTable(id integer primary key autoincrement, title varchar(50), content varchar(2000), imageid smallint, value real, style varchar(50) )";

    public MyDatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //第一次使用数据库自动建表
        //SQL语言实例create table student(name varchar(10), age smallint);
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("----------onUpdate Called------------" + oldVersion + "---->" + newVersion);
    }


    //表格数据的增，改，删，查
    public void insertDate(String title, String content, int imageIndex, float value, String style) {
        SQLiteDatabase db = this.getWritableDatabase();
        //插入语句
        //SQL语言实例insert into student values('张三', 20);因为主键为自增，所以为空
        db.execSQL("insert into myTable values(null,?,?,?,?,?)", new String[]{title, content, "" + imageIndex, "" + value, style});
    }

    public void updateDate(int id, String title, String content, int imageIndex) {
        SQLiteDatabase db = this.getWritableDatabase();
        //更新语句
        //SQL语言实例update contact set lastname='江南七怪', mobile='13912345678' where id=1028;
        db.execSQL("update myTable set title=?, content=?, imageid=? where id=?", new String[]{title, content, "" + imageIndex, "" + id});
    }

    public void deleteDate(int id, String title, String content, int imageIndex) {
        SQLiteDatabase db = this.getWritableDatabase();
        //删除语句
        //SQL语言实例delete from film where year < 1970;
        db.execSQL("delete from myTable where id like ?", new String[]{"" + id});
        db.close();
//        db.delete("myTable","id = ?", new String[]{String.valueOf(1)});
        Log.e("onon", "删除");
    }

    public void sumDate(int id, String title, String content, int imageIndex, float value, String style) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("sum (?) from table where style=? ", new String[]{"" + value, style});
    }

    public Cursor queryDate(int id, String title, String style, int imageIndex) {
        SQLiteDatabase db = this.getReadableDatabase();
        //查询语句
        //SQL语言实例select * from film where starring like 'Jodie%'
        Cursor cursor = db.rawQuery("select * from myTable where id like ? or title like ? or style like ?",
                new String[]{"" + id, "%" + title + "%", "%" + style + "%"});
        return cursor;
    }

    //查询所有数据
    public Cursor queryAllDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        //查询语句
        //SQL语言实例select * from film where starring like 'Jodie%'
        Cursor cursor = db.rawQuery("select * from myTable", new String[]{});
        return cursor;
    }

    public List<String> convertCursorToListString(Cursor cursor, int columnIndex) {
        List<String> stringList = new ArrayList<String>();
//        Log.e("cursor", "start");
        if (cursor.moveToFirst()) {
            do {
                Log.e("cursor", "strstart");
                stringList.add(cursor.getString(columnIndex));
                Log.e("cursor", "strend");
            } while (cursor.moveToNext());
        }
        return stringList;
    }

    public List<Integer> convertCursorToListInteger(Cursor cursor, int columnIndex) {
        List<Integer> integerList = new ArrayList<Integer>();
        if (cursor.moveToFirst()) {
            do {
                Log.e("cursor", "strstart");
                integerList.add(cursor.getInt(columnIndex));
                Log.e("cursor", "strend");
            } while (cursor.moveToNext());
        }
        return integerList;
    }
}
