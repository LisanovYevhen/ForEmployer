package com.example.testwork;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SQLDataBase extends SQLiteOpenHelper {

    private SQLiteDatabase sqLiteDatabase;
    private static final String KEY_FOR_MAP_NULL_ITEM="null";

    public SQLDataBase(@Nullable Context context, @Nullable String nameDataBase, int versionDataBase) {
        super(context, nameDataBase, null, versionDataBase);
    }


    public  SQLiteDatabase  openConnection(){
        sqLiteDatabase=this.getWritableDatabase();
        return sqLiteDatabase;
    }

    public int  saveValues(String number,String hardness,Context context) {
        Cursor cursor = sqLiteDatabase.query("Hardness", new String[]{"number"}, "number = ?", new String[]{number}, null, null, null);
        if(cursor.moveToFirst()){
            int columnIndexNumber=cursor.getColumnIndex("number");
            if(cursor.getInt(columnIndexNumber)==Integer.valueOf(number)){
                cursor.close();
                Log.d(GeneralActivity.LOG, "CursorCount!=0, введите другой номер в базе такой замер есть");
                return -1;
            } else {
               checkGetMethod(cursor,number,hardness,context);
               return Integer.valueOf(number);
            }
        } else {
            checkGetMethod(cursor,number,hardness,context);
            return Integer.valueOf(number);
        }

    }

    public int clean(Context context,String[] whereArgs) {
        Cursor cursor = sqLiteDatabase.query("Hardness", new String[]{"number"}, "number = ?", new String[]{whereArgs[0]}, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndexNumber = cursor.getColumnIndex("number");
            if (cursor.getInt(columnIndexNumber)==Integer.valueOf(whereArgs[0])) {
                cursor.close();
                int clearCount = sqLiteDatabase.delete("Hardness", "number = ?", whereArgs);
                return Integer.valueOf(whereArgs[0]);
                //Toast.makeText(context, "Колическо удаленных номеров=" + String.valueOf(clearCount), Toast.LENGTH_LONG).show();
            }
        } else {
            cursor.close();
            return -1;
            //Toast.makeText(context, "По указаному номеру нет записей", Toast.LENGTH_LONG).show();
        }
        return -1;

    }

    public int deleteAllBase(Context context){
        //SQLiteDatabase.deleteDatabase(new File(sqLiteDatabase.getPath()));
         return sqLiteDatabase.delete("Hardness", null, null);
        //Toast.makeText(context, "Все замеры стертые, кол-во стертых замеров=" + String.valueOf(clearCount), Toast.LENGTH_LONG).show();
    }

    public SimpleAdapter createAndReturnAdapter(Context context){
        List<Map<String,String>> data = new ArrayList<>();
        Cursor cursor =sqLiteDatabase.query("Hardness",null,null,null,null,null,"number ASC");
        if(cursor.moveToFirst()){
            int columnIndexNumber=cursor.getColumnIndex("number");
            int columnIndexDate=cursor.getColumnIndex("time");
            int columnIndexHardness=cursor.getColumnIndex("hardness");
            do{
                Map<String,String> map = new HashMap<>();
                map.put(GeneralActivity.KEY_FOR_MAP_NUMBER,String.valueOf(cursor.getInt(columnIndexNumber)));
                map.put(GeneralActivity.KEY_FOR_MAP_DATE,cursor.getString(columnIndexDate));
                map.put(GeneralActivity.KEY_FOR_MAP_HARDNESS,String.valueOf(cursor.getFloat(columnIndexHardness)));
                data.add(map);
            } while (cursor.moveToNext());
            String[] from ={GeneralActivity.KEY_FOR_MAP_NUMBER,GeneralActivity.KEY_FOR_MAP_DATE,GeneralActivity.KEY_FOR_MAP_HARDNESS};
            int [] to = {R.id.textViewListNumber,R.id.textViewListData,R.id.textViewListHardness};
            cursor.close();
            return new SimpleAdapter(context,data,R.layout.items,from,to);

        } else {
            List<Map<String,String>> datanull = new ArrayList<>();
            Map<String,String> mapnull = new HashMap<>();
            mapnull.put(SQLDataBase.KEY_FOR_MAP_NULL_ITEM,"Нету данных для отображения");
            datanull.add(mapnull);
            String[] fromnull={SQLDataBase.KEY_FOR_MAP_NULL_ITEM};
            int [] tonull = {R.id.textViewNull};
            return new SimpleAdapter(context,datanull,R.layout.items_null,fromnull,tonull);
        }
    }


    private void checkGetMethod(Cursor cursor,String number, String hardness, Context context){
        cursor.close();
        ContentValues contentValues = new ContentValues();
        Integer integer = Integer.valueOf(number);
        contentValues.put("number", integer);
        Float aFloat = Float.valueOf(hardness);
        contentValues.put("hardness", aFloat);
        String timeStamp = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss").format(System.currentTimeMillis());
        contentValues.put("time", timeStamp);
        long numberPut = sqLiteDatabase.insert("Hardness", null, contentValues);
        //Toast.makeText(context,"Замер №="+number+" save",Toast.LENGTH_LONG).show();
    }



    public String[] getHardnessValues(String[] selectionsArgs,Context context){
        String[] resultCursor= new String[2];
        resultCursor[0]="false";
        resultCursor[1]="false";
        //"number = ? AND time =?" new String[] {"First despr", "Second despr"},
        Cursor cursor=sqLiteDatabase.query("Hardness",new String[]{"hardness","time"},"number = ?",selectionsArgs,null, null,null);
        if((cursor.moveToFirst())){
            int columnIndexForHardness=cursor.getColumnIndex("hardness");
            int columnIndexForTime=cursor.getColumnIndex("time");
            do{
                float resultHardness=cursor.getFloat(columnIndexForHardness);
                resultCursor[0]=String.valueOf(resultHardness);
                String resultTime=cursor.getString(columnIndexForTime);
                resultCursor[1]=resultTime;
            } while (cursor.moveToNext());
            cursor.close();
            return resultCursor;
            }

        //Toast.makeText(context,"Неправильный введен номер замера",Toast.LENGTH_LONG).show();
        return resultCursor;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Hardness ("
                   + "id integer primary key autoincrement,"
                   + "number integer,"
                   + "time text,"
                   + "hardness real" + ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
