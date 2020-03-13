package com.example.testwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class GeneralActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String LOG="GeneralActivity";


    private final String MY_FOLDER="HardnessTester";
    private final String PREFIXFILENAME ="write";
    private final String SUFFIXFILENAME=".txt";
    private final String NAME_DATA_BASE="ForWork";
    private final int VERSION_DATA_BASE=1;




    private Button buttonSaveInFile, buttonLoadInFile;
    private  File fileToWrite=null;
    private EditText hardness, meteringNumber, editTextResult;
    private FileWriter fileWriter;
    private TextView textViewResult;



    private FileReader fileReader;
    private BufferedReader bufferedReader;
    private Map<String ,String> valueForReading=new HashMap<>();
    private Set<String> keyWithMap= new HashSet<>();
    private Set<String> key =  new TreeSet<>();
    private Integer count=0;

    private SQLDataBase sqlDataBase;
    private Button buttonSaveInSQL,buttonDeleteInSQL,buttonGetInSQL,buttonDeleteAllInSQL;
    private EditText editTextNumberForSQL,editTextHBForSQL,editTextNumberDeleteForSQL,editTextNumberGetForSQL;
    private TextView textViewResultForSQL;

    private ListView listViewForSQL;

    public static final String KEY_FOR_MAP_NUMBER="number";
    public static final String KEY_FOR_MAP_DATE="date";
    public static final String KEY_FOR_MAP_HARDNESS="hardness";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general);

        buttonSaveInFile = (Button) findViewById(R.id.buttonSaveInFile);
        buttonSaveInFile.setOnClickListener(this);
        hardness = (EditText) findViewById(R.id.editTextHB);
        meteringNumber=(EditText) findViewById(R.id.editTextNumber);
        buttonLoadInFile=(Button) findViewById(R.id.buttonLoadInFile);
        buttonLoadInFile.setOnClickListener(this);
        textViewResult=(TextView) findViewById(R.id.textView5);
        editTextResult=(EditText) findViewById(R.id.editTextHBResult);


        sqlDataBase= new SQLDataBase(this,NAME_DATA_BASE,VERSION_DATA_BASE);

        editTextNumberForSQL=(EditText) findViewById(R.id.editTextNumberForSQL);
        editTextHBForSQL=(EditText) findViewById(R.id.editTextHBForSQL);
        buttonSaveInSQL=(Button) findViewById(R.id.buttonSaveInSQL);
        buttonSaveInSQL.setOnClickListener(this);
        buttonDeleteInSQL=(Button) findViewById(R.id.buttonDeleteInSQL);
        buttonDeleteInSQL.setOnClickListener(this);
        editTextNumberDeleteForSQL=(EditText) findViewById(R.id.editTextNumberDeleteForSQL);
        editTextNumberGetForSQL=(EditText) findViewById(R.id.editTextNumberGetForSQL);
        buttonGetInSQL=(Button) findViewById(R.id.buttonGetInSQL);
        buttonGetInSQL.setOnClickListener(this);
        textViewResultForSQL=(TextView) findViewById(R.id.textViewResultForSQL);
        buttonDeleteAllInSQL=(Button) findViewById(R.id.buttonDeleteAllInSQL);
        buttonDeleteAllInSQL.setOnClickListener(this);

        listViewForSQL=(ListView) findViewById(R.id.listViewForSQL);
        sqlDataBase.openConnection();
        listViewForSQL.setAdapter(sqlDataBase.createAndReturnAdapter(this));
        sqlDataBase.close();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonSaveInFile:{
                if(fileToWrite==null){
                  createFileAndDirectory();
                }
                       if(PermissionClass.checkedPermission(this,PermissionClass.PERMISSION_ARRAY_WRITE_DIRECTORY)) {
                           writeForFile();
                        } else {
                            PermissionClass.createPermissionDialog(this,PermissionClass.PERMISSION_ARRAY_WRITE_DIRECTORY,PermissionClass.PERMISSION_ARRAY_DIALOG[0]);
                        }
                break;
            } case R.id.buttonLoadInFile:{
                readFile();
                break;
            } case R.id.buttonSaveInSQL:{
                String marker="buttonSaveInSQL";

                String number=editTextNumberForSQL.getText().toString();
                String hardness=editTextHBForSQL.getText().toString();

                new AsyncTaskSQL(this,marker,number,hardness).execute( sqlDataBase);
                editTextNumberForSQL.setText("");
                editTextHBForSQL.setText("");
                break;
            } case R.id.buttonDeleteInSQL:{
                String marker="buttonDeleteInSQL";
                String deleteNumber = editTextNumberDeleteForSQL.getText().toString();
                new AsyncTaskSQL(this,marker,deleteNumber,null).execute(sqlDataBase);
                editTextNumberDeleteForSQL.setText("");
                break;
            } case R.id.buttonGetInSQL:{
                String marker="buttonGetInSQL";

                String numberGetForSQL=editTextNumberGetForSQL.getText().toString();
                if(numberGetForSQL.isEmpty()){
                    Log.d(GeneralActivity.LOG, "number==isEmpty()");
                    Toast.makeText(this,"Вы не ввели номер замера",Toast.LENGTH_LONG).show();
                    editTextNumberGetForSQL.setText("");
                    return;
                }
                new AsyncTaskSQL(this,marker,numberGetForSQL,null).execute( sqlDataBase);
                editTextNumberGetForSQL.setText("");
                break;
            } case R.id.buttonDeleteAllInSQL:{
                String marker="buttonDeleteAllInSQL";
                new AsyncTaskSQL(this,marker,null,null).execute(sqlDataBase);
                break;
            }

        }
    }

    private void writeForFile(){
        try {
            fileWriter= new FileWriter(fileToWrite,true);
            String number = meteringNumber.getText().toString();
            String timeStamp = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss").format(System.currentTimeMillis());
            String hardnessString=hardness.getText().toString();
            if((count==0)||(key.size()==0)){
                restart();
            }
            for (String s:key) {
                if (s.equals(number)){
                    Toast.makeText(this,"Указанный номер существует, укажите другой номер",Toast.LENGTH_LONG).show();
                    return;
                }
            }
            String result="Номер замера="+number+" "+"Время замера="+timeStamp+" "+"Твердость="+hardnessString+"\n";
            fileWriter.write(result);
            count++;
            fileWriter.close();
            Toast.makeText(this,"Замер "+number+" cохранен",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
           Log.e(LOG,"Невозможно записать в файл данные");
        }

    }

    private void readFile() {
        if ((valueForReading.size()!=count)||(valueForReading.size()==0)||(keyWithMap.size()==0)) {
            if(fileToWrite==null){
                   createFileAndDirectory();
               if(!fileToWrite.canRead()){
                   Toast.makeText(this, "Файл не может быть прочитан введите данные", Toast.LENGTH_LONG).show();
                   return;
               }
               restart();
            } else {
                restart();
            }
        }
        for (String s:keyWithMap) {
            if (s.equals(editTextResult.getText().toString())){
                editTextResult.setText("");
                textViewResult.setText(valueForReading.get(s));
                return;
            }
        }
        Toast.makeText(this,"Замера с указанным номером не существует №",Toast.LENGTH_LONG).show();
        textViewResult.setText("");
        editTextResult.setText("");
    }

    private void restart(){
        try {
            fileReader = new FileReader(fileToWrite);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String hardness=line.split("Твердость=")[1];
                String stringForSplitNumber=line.split("Номер замера=")[1];
                String number= stringForSplitNumber.split(" ")[0];
                valueForReading.put(number,hardness);
                key.add(number);
                Log.d(LOG,valueForReading.get(number));
            }
            keyWithMap=valueForReading.keySet();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            Log.e(LOG, "Файл для чтение не найден или замер с Номером № не найден");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  void   createFileAndDirectory(){
       if(fileToWrite==null){
             File file =  getExternalFilesDir(MY_FOLDER).getAbsoluteFile();
             fileToWrite = new File(file,PREFIXFILENAME+SUFFIXFILENAME);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PermissionClass.PERMISSION_ARRAY_DIALOG[0]){
            for (int i:grantResults) {
                if(i!=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Вами отказано в доступе",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            writeForFile();
        }
    }

    private  class AsyncTaskSQL extends AsyncTask<SQLDataBase,Void,SimpleAdapter>{

        private Context context;
        private String  marker;
        private String number;
        private String hardness;

        private  String valueToast="true";
        private int valueSQL=0;

        private SQLDataBase sqlDataBase;
        private SimpleAdapter adapter;
        private String stringHardness="true";

        public AsyncTaskSQL(Context context, String marker, String number, String hardness) {
            this.context = context;
            this.marker = marker;
            this.number = number;
            this.hardness = hardness;
        }


        @Override
        protected void onPreExecute() {
            buttonSaveInSQL.setEnabled(false);
            buttonDeleteInSQL.setEnabled(false);
            buttonGetInSQL.setEnabled(false);
            buttonDeleteAllInSQL.setEnabled(false);
        }

        @Override
        protected SimpleAdapter doInBackground(SQLDataBase... sqlDataBases) {
            this.sqlDataBase=sqlDataBases[0];

            if(marker.equals("buttonSaveInSQL")){
                if((number.isEmpty())||(hardness.isEmpty())){
                    sqlDataBase.openConnection();
                    valueToast="false";
                    Log.d(GeneralActivity.LOG, "number for save==isEmpty() or hardness==isEmpty()");
                    adapter=sqlDataBase.createAndReturnAdapter(context);
                    sqlDataBase.close();
                    return adapter;
                } else{
                    sqlDataBase.openConnection();
                    valueSQL=sqlDataBase.saveValues(number,hardness,context);
                    adapter=sqlDataBase.createAndReturnAdapter(context);
                    sqlDataBase.close();
                    return adapter;
                }
            }
            if(marker.equals("buttonDeleteInSQL")){
                if(number.isEmpty()){
                    valueToast="false";
                    sqlDataBase.openConnection();
                    Log.d(GeneralActivity.LOG, "number for delete==isEmpty()");
                    adapter=sqlDataBase.createAndReturnAdapter(context);
                    sqlDataBase.close();
                    return adapter;
                } else {
                    String[] whereArgs={number};
                    sqlDataBase.openConnection();
                    valueSQL= sqlDataBase.clean(context,whereArgs);
                    adapter=sqlDataBase.createAndReturnAdapter(context);
                    sqlDataBase.close();
                    return adapter;
                }
            } if(marker.equals("buttonGetInSQL")){
                if(number.isEmpty()){
                    valueToast="false";
                    sqlDataBase.openConnection();
                    Log.d(GeneralActivity.LOG, "number for get==isEmpty()");
                    adapter=sqlDataBase.createAndReturnAdapter(context);
                    sqlDataBase.close();
                    return adapter;
                } else {
                    String[] selectionsArgs={number};
                    sqlDataBase.openConnection();
                    String s=sqlDataBase.getHardnessValues(selectionsArgs,context)[0];
                    if(!s.equals("false")) {
                        stringHardness=s;
                        adapter=sqlDataBase.createAndReturnAdapter(context);
                        sqlDataBase.close();
                        return adapter;
                    } else {
                        adapter=sqlDataBase.createAndReturnAdapter(context);
                        sqlDataBase.close();
                        return adapter;
                    }
                }
            } if(marker.equals("buttonDeleteAllInSQL")){
                sqlDataBase.openConnection();
                valueSQL=sqlDataBase.deleteAllBase(getApplicationContext());
                adapter=sqlDataBase.createAndReturnAdapter(context);
                sqlDataBase.close();
                return adapter;
            }
            sqlDataBase.openConnection();
            adapter=sqlDataBase.createAndReturnAdapter(context);
            sqlDataBase.close();
            return adapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter simpleAdapter) {
            switch (marker){
                case "buttonSaveInSQL":{
                    if(valueToast.equals("false")){
                        Toast.makeText(context,"Замер не сохранен так как вы не ввели номер или твердость",Toast.LENGTH_LONG).show();
                        break;
                    }
                    if (valueSQL==-1){
                        Toast.makeText(context,"Введите другой номер в базе такой замер есть",Toast.LENGTH_LONG).show();
                        break;
                    }
                    if(valueSQL>0){
                        Toast.makeText(context,"Замер №="+valueSQL+" save",Toast.LENGTH_LONG).show();
                    }
                    break;
                } case "buttonGetInSQL":{
                    if(stringHardness.equals("true")){
                        Toast.makeText(context,"Неправильный введен номер замера",Toast.LENGTH_LONG).show();
                        textViewResultForSQL.setText("");
                        break;
                    }
                    textViewResultForSQL.setText(stringHardness);
                    break;
                } case "buttonDeleteInSQL":{
                    if(valueToast.equals("false")){
                        Toast.makeText(context,"Вы не ввели номер для удаления",Toast.LENGTH_LONG).show();
                        break;
                    }

                    if (valueSQL==-1){
                        Toast.makeText(context,"По заданому номеру нет записей в БД для удаления",Toast.LENGTH_LONG).show();
                        break;
                    }
                    if(valueSQL>0){
                        Toast.makeText(context,"Замер №="+valueSQL+" delete",Toast.LENGTH_LONG).show();
                    }
                    break;

                } case "buttonDeleteAllInSQL":{
                    Toast.makeText(context,"База данных очищена удалено"+valueSQL+" записей",Toast.LENGTH_LONG).show();
                    break;
                }
            }

            buttonSaveInSQL.setEnabled(true);
            buttonDeleteInSQL.setEnabled(true);
            buttonGetInSQL.setEnabled(true);
            buttonDeleteAllInSQL.setEnabled(true);
            listViewForSQL.setAdapter(simpleAdapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        valueForReading.clear();
        count=0;
        keyWithMap.clear();
        key.clear();
    }



}
