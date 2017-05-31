package lsxx.waterassistant;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //组件

    private SurfaceView sfvDraw;
    private SurfaceHolder sfvHolder;
    private int DrawEndX, DrawEndY, DrawStartX, DrawStartY;
    private ImageButton ibtnHand;
    private ImageButton ibtnFood;
    private ImageButton ibtnBath;
    private ImageButton ibtnFlush;
    private ImageButton ibtnCloth;
    private ImageButton ibtnClean;
    private ImageButton ibtnCar;
    private ImageButton ibtnUndefinition2;
    private TextView tvCommand;
    private ListView lvRecord;
    private TextView tvCurrentValue;
    private TextView tvSet;
    private SeekBar skbValue;
    private LinearLayout lnlSet;
    private Button btnOK;
    private HttpRequestUtil HttpRequestUtil;

    //数据库相关
    private MyDatabaseHelper dbHelper;
    private MyDatabaseHelper dbHelperValueSet;

    //列表相关
    List<Integer> baseId = new ArrayList<Integer>();
    List<String> titles = new ArrayList<String>();
    List<String> contents = new ArrayList<String>();
    List<Integer> imageId = new ArrayList<Integer>();
    private String str_title;
    private String str_content;
    private String str_contentStart;
    private String str_contentEnd;
    private int imageIndex;

    //参数设置相关
    List<Integer> setId = new ArrayList<Integer>();
    List<String> setValue = new ArrayList<String>();
    private int currentId;
    private String currentStyle;
    private float currentValue;
    private float currentMAX;

    //状态
    private boolean flagBath = false;
    private boolean flagCar = false;
    private float valueBath;
    private float valueCar;
    long seconds = 0;
    String str_time = "";

    //显示相关
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
    private DecimalFormat decimalFormat2 = new DecimalFormat("0.0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
    //水量
    private float limitMax=500;
    private float currentLeft;
    private float currentUsed;

    //传输相关
    private String params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化组件
        ibtnHand = (ImageButton) findViewById(R.id.ibtnHand);
        ibtnFood = (ImageButton) findViewById(R.id.ibtnFood);
        ibtnBath = (ImageButton) findViewById(R.id.ibtnBath);
        ibtnFlush = (ImageButton) findViewById(R.id.ibtnFlush);
        ibtnCloth = (ImageButton) findViewById(R.id.ibtnCloth);
        ibtnClean = (ImageButton) findViewById(R.id.ibtnClean);
        ibtnCar = (ImageButton) findViewById(R.id.ibtnCar);
        ibtnUndefinition2 = (ImageButton) findViewById(R.id.ibtnUndefinition2);

        sfvDraw = (SurfaceView) findViewById(R.id.sfvDraw);
        sfvDraw.setZOrderOnTop(true);
        sfvDraw.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        sfvHolder = sfvDraw.getHolder();

        lnlSet = (LinearLayout) findViewById(R.id.lnlSet);
        btnOK = (Button) findViewById(R.id.btnOK);
        skbValue = (SeekBar) findViewById(R.id.skbValue);
        setListener();
        tvCommand = (TextView) findViewById(R.id.tvCommand);
        tvCurrentValue = (TextView) findViewById(R.id.tvCurrentValue);
        tvSet = (TextView) findViewById(R.id.tvSet);
        lvRecord = (ListView) findViewById(R.id.lvRecord);

        //历史记录
//        deleteDatabase("myRecord.db3");
        //创建对象，指定数据库版本为1，使用相对路径即可，数据库会保存在程序数据文件夹的databases目录下
        dbHelper = new MyDatabaseHelper(this, "myRecord.db3", 1);
        currentUsed=UpdateListView();
        currentLeft=limitMax-currentUsed;
        if(currentLeft<0) {
            currentLeft=0;
            Toast.makeText(MainActivity.this,"您今日用水量已超！",Toast.LENGTH_SHORT).show();
        }
                //用户设置值
//        deleteDatabase("mySet.db3");
        dbHelperValueSet = new MyDatabaseHelper(this, "mySet.db3", 1);

        ibtnUndefinition2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postData();
            }
        });
        handler2.postDelayed(runnable2, 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        RealTimeDraw(1,1);
    }

    public void clear(Canvas aCanvas) {
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        aCanvas.drawPaint(paint);
    }

    void RealTimeDraw() {
        int x1, y1, x2, y2;

        Canvas canvas = sfvHolder.lockCanvas(new Rect(0, 0, getWindowManager().getDefaultDisplay().getWidth(),
                getWindowManager().getDefaultDisplay().getHeight()));
//                getWindowManager().getDefaultDisplay().getHeight()));
        Log.i("POST_RESULT","12334");
        canvas.drawRGB(255, 255, 255);
        DrawStartX = (int) (sfvDraw.getWidth() * 0.15);
        DrawStartY = (int) (sfvDraw.getHeight() * 0.15);
        DrawEndX = (int) (sfvDraw.getWidth() * 0.45);
        DrawEndY = (int) (sfvDraw.getHeight() * 0.95);
        clear(canvas);
        Paint mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(10);
        mPaint.setAntiAlias(true);

        //边框
        x1 = DrawStartX;
        y1 = DrawStartY;
        x2 = DrawStartX;
        y2 = DrawEndY;
        canvas.drawLine(x1, y1, x2, y2, mPaint);

        x1 = DrawStartX;
        y1 = DrawEndY;
        x2 = DrawEndX;
        y2 = DrawEndY;
        canvas.drawLine(x1, y1, x2, y2, mPaint);

        x1 = DrawEndX;
        y1 = DrawEndY;
        x2 = DrawEndX;
        y2 = DrawStartY;
        canvas.drawLine(x1, y1, x2, y2, mPaint);

        //画水
        Paint mPaint_water = new Paint();
        mPaint_water.setColor(getResources().getColor(R.color.color_buttonback));
        mPaint_water.setStrokeWidth(10);
        mPaint_water.setAntiAlias(true);
        Path path1 = new Path();
        path1.moveTo(DrawStartX + 10, DrawEndY + 10 - (int) ((DrawEndY - DrawStartY-67) * (currentLeft/limitMax)));
        path1.lineTo(DrawStartX + 10, DrawEndY - 10);
        path1.lineTo(DrawEndX - 10, DrawEndY - 10);
        path1.lineTo(DrawEndX - 10, DrawEndY + 10 - (int) ((DrawEndY - DrawStartY-67) * (currentLeft/limitMax)));
        canvas.drawPath(path1, mPaint_water);

        //刻度左
        Paint mPaint_text = new Paint();
        mPaint_text.setColor(Color.BLACK);
        mPaint_text.setStrokeWidth(8);
        mPaint_text.setTextSize(40);
        mPaint_text.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i < 6; i++) {
            x1 = DrawStartX;
            y1 = DrawEndY - i * (DrawEndY - DrawStartY) / 6;
            x2 = DrawStartX + (DrawEndX - DrawStartX) / 9;
            y2 = DrawEndY - i * (DrawEndY - DrawStartY) / 6;
            canvas.drawLine(x1, y1, x2, y2, mPaint);
            int scalei=i*100;
            canvas.drawText(scalei+"", DrawStartX-50,DrawEndY-((int)(i/6.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        }
        canvas.drawText("单位/升"+"", DrawStartX-20,DrawEndY-((int)(6/6.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        mPaint_text.setTextSize(40);
        mPaint_text.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("今日剩余水量："+decimalFormat.format(currentLeft)+"L", DrawEndX+50,DrawEndY-((int)(7/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("洗手-"+decimalFormat2.format(sumValueByStyle("hand"))+"L", DrawEndX+50,DrawEndY-((int)(6/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("煮饭-"+decimalFormat2.format(sumValueByStyle("food"))+"L", DrawEndX+50,DrawEndY-((int)(5/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("洗澡-"+decimalFormat2.format(sumValueByStyle("bath"))+"L", DrawEndX+50,DrawEndY-((int)(4/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("马桶-"+decimalFormat2.format(sumValueByStyle("flush"))+"L", DrawEndX+50,DrawEndY-((int)(3/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("洗衣-"+decimalFormat2.format(sumValueByStyle("cloth"))+"L", DrawEndX+50,DrawEndY-((int)(2/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("清洁-"+decimalFormat2.format(sumValueByStyle("clean"))+"L", DrawEndX+50,DrawEndY-((int)(1/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("洗车-"+decimalFormat2.format(sumValueByStyle("car"))+"L", DrawEndX+50,DrawEndY-((int)(0/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本


        for (int i=0;i<7;i++){
            float value=0;
            switch (i) {
                case 0: value=sumValueByStyle("hand"); break;
                case 1: value=sumValueByStyle("food"); break;
                case 2: value=sumValueByStyle("bath"); break;
                case 3: value=sumValueByStyle("flush"); break;
                case 4: value=sumValueByStyle("cloth"); break;
                case 5: value=sumValueByStyle("clean"); break;
                case 6: value=sumValueByStyle("car"); break;
            }
            path1.moveTo(DrawEndX + 290, DrawStartY + 55+(int)(i*sfvDraw.getWidth()*0.067));
            path1.lineTo(DrawEndX +290+(int)(400*value/limitMax), DrawStartY + 55+(int)(i*sfvDraw.getWidth()*0.067) );
            path1.lineTo(DrawEndX +290+(int)(400*value/limitMax), DrawStartY + 85+(int)(i*sfvDraw.getWidth()*0.067) );
            path1.lineTo(DrawEndX + 290, DrawStartY + 85+(int)(i*sfvDraw.getWidth()*0.067) );

            canvas.drawPath(path1, mPaint_water);
        }
        canvas.drawText("洗手-"+decimalFormat2.format(sumValueByStyle("hand"))+"L", DrawEndX+50,DrawEndY-((int)(6/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("煮饭-"+decimalFormat2.format(sumValueByStyle("food"))+"L", DrawEndX+50,DrawEndY-((int)(5/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("洗澡-"+decimalFormat2.format(sumValueByStyle("bath"))+"L", DrawEndX+50,DrawEndY-((int)(4/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("马桶-"+decimalFormat2.format(sumValueByStyle("flush"))+"L", DrawEndX+50,DrawEndY-((int)(3/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("洗衣-"+decimalFormat2.format(sumValueByStyle("cloth"))+"L", DrawEndX+50,DrawEndY-((int)(2/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("清洁-"+decimalFormat2.format(sumValueByStyle("clean"))+"L", DrawEndX+50,DrawEndY-((int)(1/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本
        canvas.drawText("洗车-"+decimalFormat2.format(sumValueByStyle("car"))+"L", DrawEndX+50,DrawEndY-((int)(0/7.0*(DrawEndY-DrawStartY)))+10,mPaint_text);// 画文字文本


        sfvHolder.unlockCanvasAndPost(canvas);
    }


    private void postData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String params = sumValues();
                Log.e("POST_RESULT", params);
                String post_result = null;
                try {
                    post_result = HttpUtils.submitPostData(params, "utf-8");
                    Log.i("POST_RESULT", post_result);
                } catch (MalformedURLException e) {
                    Log.i("POST_RESULT", "fail");
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //求和所有
    public String sumValues() {
        String str = "";
        str = str + "{  \"ID\": 2,  \"hand\": ";
        str = str + sumValueByStyle("hand") + ",  ";
        str = str + "\"bath\" : ";
        str = str + sumValueByStyle("bath") + ",  ";
        str = str + "\"cloth\" : ";
        str = str + sumValueByStyle("cloth") + ",  ";
        str = str + "\"food\" : ";
        str = str + sumValueByStyle("food") + ",  ";
        str = str + "\"clean\" : ";
        str = str + sumValueByStyle("clean") + ",  ";
        str = str + "\"flush\" : ";
        str = str + sumValueByStyle("flush") + ",  ";
        str = str + "\"car\" : ";
        str = str + sumValueByStyle("car") + ",  \"name\" : \"admin\" " +  ",  \"password\" : \"admin\"}";
        return str;
    }

    public float sumFloatValues() {
        float sum = 0;
        sum = sum + sumValueByStyle("hand");
        sum = sum + sumValueByStyle("bath");
        sum = sum + sumValueByStyle("cloth");
        sum = sum + sumValueByStyle("food");
        sum = sum + sumValueByStyle("clean");
        sum = sum + sumValueByStyle("flush");
        sum = sum + sumValueByStyle("car");
        return sum;
    }

    //求和单独
    public float sumValueByStyle(String style) {

        Cursor cursorResults = dbHelper.queryDate(0, "#$%", style, -1);

        List<String> strValues = dbHelper.convertCursorToListString(cursorResults, 4);
//        Log.i("POST_RESULT", style+strValues.size()+"");
        float sum = 0;
        for (int i = 0; i < strValues.size(); i++) {
            sum = sum + Float.parseFloat(strValues.get(i));
        }
        return sum;
    }

    //列表按键处理
    private ImageButton.OnClickListener myButtonListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            View myItemView = (View) v.getParent();
            //按键处理
            String content = ((TextView) myItemView.findViewById(R.id.tvContent)).getText().toString();
        }
    };

    //列表按键处理
    private RelativeLayout.OnTouchListener myTouchListener = new RelativeLayout.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View myItemView = (View) v.getParent().getParent();
//                    int id = baseId.get(position);
//                    MyDatabaseHelper dbHelper = new MyDatabaseHelper(context, "myRecord.db3", 1);
                  int deleteId=Integer.parseInt(((TextView)myItemView.findViewById(R.id.tvId)).getText().toString());
                    dbHelper.deleteDate(deleteId, "", "", 0);
                    Toast.makeText(MainActivity.this, "已删除", Toast.LENGTH_SHORT).show();
//                    dbHelper.close();
//                    titles.remove(position);
//                    contents.remove(position);
//                    imageId.remove(position);
//                    baseId.remove(position);
//
////                    Toast.makeText(context, "已删除" + titles.size() + contents.size() + imageId.size() + baseId.size(), Toast.LENGTH_SHORT).show();
//                    mAdapter.notifyDataSetChanged();
                currentUsed=UpdateListView();
                currentLeft=limitMax-currentUsed;
                if(currentLeft<0) {
                    currentLeft=0;
                    Toast.makeText(MainActivity.this,"您今日用水量已超！",Toast.LENGTH_SHORT).show();
                }
                RealTimeDraw();
            }
            return false;
        }
    };

    //更新表格
    private float UpdateListView() {
        //列表
        Cursor cursorResults = dbHelper.queryAllDate();
        baseId = dbHelper.convertCursorToListInteger(cursorResults, 0);
        Collections.reverse(baseId);
        titles = dbHelper.convertCursorToListString(cursorResults, 1);
        Collections.reverse(titles);
        contents = dbHelper.convertCursorToListString(cursorResults, 2);
        Collections.reverse(contents);
        imageId = dbHelper.convertCursorToListInteger(cursorResults, 3);
        Collections.reverse(imageId);
        ListViewAdapter myAdapter = new ListViewAdapter(titles, contents, imageId, baseId, this);
        myAdapter.setItemImageButtonListener(myButtonListener);
        myAdapter.setItemRelativeLayoutListener(myTouchListener);
        lvRecord.setAdapter(myAdapter);
        return sumFloatValues();
    }


    void setListener() {
        ibtnHand.setOnClickListener(clickListener);
        ibtnFood.setOnClickListener(clickListener);
        ibtnBath.setOnClickListener(clickListener);
        ibtnFlush.setOnClickListener(clickListener);
        ibtnCloth.setOnClickListener(clickListener);
        ibtnClean.setOnClickListener(clickListener);
        ibtnCar.setOnClickListener(clickListener);
        ibtnHand.setOnLongClickListener(longClickListener);
        ibtnFood.setOnLongClickListener(longClickListener);
        ibtnBath.setOnLongClickListener(longClickListener);
        ibtnFlush.setOnLongClickListener(longClickListener);
        ibtnCloth.setOnLongClickListener(longClickListener);
        ibtnClean.setOnLongClickListener(longClickListener);
        ibtnCar.setOnLongClickListener(longClickListener);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelperValueSet.updateDate(currentId, currentStyle, currentValue + "", 0);
                lnlSet.setVisibility(View.GONE);
            }
        });

        skbValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentValue = progress * currentMAX / skbValue.getMax();
                tvCurrentValue.setText(decimalFormat.format(currentValue) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    ;

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());
            str_content = formatter.format(curDate);
            switch (v.getId()) {
                case R.id.ibtnHand:
                    str_title = getString(R.string.str_washhand) + "用水   " + decimalFormat.format(getSetValue("hand")) + "升";
                    imageIndex = R.drawable.hand;
                    dbHelper.insertDate(str_title, str_content, imageIndex, getSetValue("hand"), "hand");
                    break;
                case R.id.ibtnFood:
                    str_title = getString(R.string.str_food) + "用水   " + decimalFormat.format(getSetValue("food")) + "升";
                    imageIndex = R.drawable.food;
                    dbHelper.insertDate(str_title, str_content, imageIndex, getSetValue("food"), "food");
                    break;
                case R.id.ibtnBath:
                    float value = 0;
                    if (flagBath == false) {
                        str_title = "当前正在" + getString(R.string.str_bath) + "……";
                        valueBath = getSetValue("bath");
                        str_contentStart = str_content;
                        ibtnBath.setBackgroundColor(getResources().getColor(R.color.color_buttontouch));
                        handler.postDelayed(runnable, 1000);
                        flagBath = true;
                    } else {
                        double val = valueBath * seconds / 60.0;
                        value = (float) val;
                        str_title = getString(R.string.str_bath) + "用水   " + decimalFormat.format(value) + "升" + "   用时" + str_time;
                        str_contentEnd = str_content;
                        str_content = str_contentStart + " - " + str_contentEnd;
                        ibtnBath.setBackgroundColor(getResources().getColor(R.color.color_buttonback));
                        Cursor cursorResults = dbHelper.queryDate(-1, "当前正在" + getString(R.string.str_bath) + "……", "@#$", -1);
                        List<Integer> ids = dbHelper.convertCursorToListInteger(cursorResults, 0);
                        dbHelper.deleteDate(ids.get(0), "", "", 0);
                        handler.removeCallbacks(runnable);
                        flagBath = false;
                    }
                    imageIndex = R.drawable.bath;
                    dbHelper.insertDate(str_title, str_content, imageIndex, value, "bath");
                    break;
                case R.id.ibtnFlush:
                    str_title = getString(R.string.str_flush) + "用水   " + decimalFormat.format(getSetValue("flush")) + "升";
                    imageIndex = R.drawable.flush;
                    dbHelper.insertDate(str_title, str_content, imageIndex, getSetValue("flush"), "flush");
                    break;
                case R.id.ibtnCloth:
                    str_title = getString(R.string.str_cloth) + "用水   " + decimalFormat.format(getSetValue("cloth")) + "升";
                    imageIndex = R.drawable.cloth;
                    dbHelper.insertDate(str_title, str_content, imageIndex, getSetValue("cloth"), "cloth");
                    break;
                case R.id.ibtnClean:
                    str_title = getString(R.string.str_clean) + "用水   " + decimalFormat.format(getSetValue("clean")) + "升";
                    imageIndex = R.drawable.clean;
                    dbHelper.insertDate(str_title, str_content, imageIndex, getSetValue("clean"), "clean");
                    break;
                case R.id.ibtnCar:
                    float value2 = 0;
                    if (flagCar == false) {
                        str_title = "当前正在" + getString(R.string.str_car) + "……";
                        valueCar = getSetValue("car");
                        str_contentStart = str_content;
                        ibtnCar.setBackgroundColor(getResources().getColor(R.color.color_buttontouch));
                        handler.postDelayed(runnable, 1000);
                        flagCar = true;
                    } else {
                        double val = valueCar * seconds / 60.0;
                        value2 = (float) val;
                        str_title = getString(R.string.str_car) + "用水   " + decimalFormat.format(value2) + "升" + "   用时" + str_time;
                        str_contentEnd = str_content;
                        str_content = str_contentStart + " - " + str_contentEnd;
                        ibtnCar.setBackgroundColor(getResources().getColor(R.color.color_buttonback));
                        Cursor cursorResults = dbHelper.queryDate(-1, "当前正在" + getString(R.string.str_car) + "……", "@#$", -1);
                        List<Integer> ids = dbHelper.convertCursorToListInteger(cursorResults, 0);
                        dbHelper.deleteDate(ids.get(0), "", "", 0);
                        handler.removeCallbacks(runnable);
                        flagCar = false;
                    }
                    imageIndex = R.drawable.car;
                    dbHelper.insertDate(str_title, str_content, imageIndex, value2, "car");
                    break;
                default:
                    break;
            }
            currentUsed=UpdateListView();
            currentLeft=limitMax-currentUsed;
            if(currentLeft<0) {
                currentLeft=0;
                Toast.makeText(MainActivity.this,"您今日用水量已超！",Toast.LENGTH_SHORT).show();
            }
            RealTimeDraw();
        }
    };

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            lnlSet.setVisibility(View.VISIBLE);
            switch (v.getId()) {
                case R.id.ibtnHand:
                    currentMAX = Float.parseFloat(getString(R.string.handmax));
                    tvSet.setText(getString(R.string.str_settitle1));
                    InitialSeekBar("hand");
                    break;
                case R.id.ibtnFood:
                    currentMAX = Float.parseFloat(getString(R.string.foodmax));
                    tvSet.setText(getString(R.string.str_settitle1));
                    InitialSeekBar("food");
                    break;
                case R.id.ibtnBath:
                    if (flagBath == false) {
                        currentMAX = Float.parseFloat(getString(R.string.bathmax));
                        tvSet.setText(getString(R.string.str_settitle2));
                        InitialSeekBar("bath");
                    }
                    break;
                case R.id.ibtnFlush:
                    currentMAX = Float.parseFloat(getString(R.string.flushmax));
                    tvSet.setText(getString(R.string.str_settitle1));
                    InitialSeekBar("flush");
                    break;
                case R.id.ibtnCloth:
                    currentMAX = Float.parseFloat(getString(R.string.clothmax));
                    tvSet.setText(getString(R.string.str_settitle1));
                    InitialSeekBar("cloth");
                    break;
                case R.id.ibtnClean:
                    currentMAX = Float.parseFloat(getString(R.string.cleanmax));
                    tvSet.setText(getString(R.string.str_settitle1));
                    InitialSeekBar("clean");
                    break;
                case R.id.ibtnCar:
                    if (flagCar == false) {
                        currentMAX = Float.parseFloat(getString(R.string.carmax));
                        tvSet.setText(getString(R.string.str_settitle2));
                        InitialSeekBar("car");
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    //计时线程
    android.os.Handler handler = new android.os.Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seconds++;
            long min = seconds / 60;
            long sec = seconds % 60;
            str_time = "";
            if (min < 10) str_time = str_time + "0" + min + ":";
            else str_time = str_time + min + ":";
            if (sec < 10) str_time = str_time + "0" + sec;
            else str_time = str_time + sec;
            //1秒调用一次，计时
            handler.postDelayed(this, 1000);
        }
    };

    //计时线程2
    android.os.Handler handler2 = new android.os.Handler();
    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            seconds++;
            long min = seconds / 60;
            long sec = seconds % 60;
            str_time = "";
            if (min < 10) str_time = str_time + "0" + min + ":";
            else str_time = str_time + min + ":";
            if (sec < 10) str_time = str_time + "0" + sec;
            else str_time = str_time + sec;
            //1秒调用一次，计时
            RealTimeDraw();
            handler2.removeCallbacks(runnable2);
        }
    };

    //获取设置参数
    public float getSetValue(String title) {
        Cursor cursorResults = dbHelperValueSet.queryDate(-1, title, "@#$", -1);
        List<String> values = dbHelperValueSet.convertCursorToListString(cursorResults, 2);
        return Float.parseFloat(values.get(0));
    }

    ;

    //初始化seekbar
    public void InitialSeekBar(String title) {
        Cursor cursorResults = dbHelperValueSet.queryDate(-1, title, "@#$", -1);
        setId = dbHelperValueSet.convertCursorToListInteger(cursorResults, 0);
        setValue = dbHelperValueSet.convertCursorToListString(cursorResults, 2);
        if (setId.size() == 0) {
            //新建字段,初始设在四分之一处
            currentValue = currentMAX / 4;
            dbHelperValueSet.insertDate(title, currentValue + "", 1, 1, "");
            Cursor cursorResults2 = dbHelperValueSet.queryDate(-1, title, "@#$", -1);
            setId = dbHelperValueSet.convertCursorToListInteger(cursorResults2, 0);
            setValue = dbHelperValueSet.convertCursorToListString(cursorResults2, 2);
        }
        currentValue = Float.parseFloat(setValue.get(0));
        currentId = setId.get(0);
        currentStyle = title;
        tvCommand.setText(currentId + "");
        skbValue.setProgress(Math.round(currentValue / currentMAX * skbValue.getMax()));
    }

    //重写onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            //关闭Helper中的SQLitebase
            dbHelper.close();
        }
        if (dbHelperValueSet != null) {
            //关闭Helper中的SQLitebase
            dbHelperValueSet.close();
        }
    }
}
