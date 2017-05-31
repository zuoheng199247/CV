package lsxx.waterassistant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.graphics.Paint;
import android.graphics.RectF;



public class Mysurfaceview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder holder;
//    private UpdateViewThread updateViewThread;
    private boolean hasSurface;



    MysurfaceviewThread mysurfaceviewThread;
    private Canvas mCanvas; // 声明一张画布
    private boolean flag;
    private Paint p; // 声明一支画笔

    public Mysurfaceview(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        holder = getHolder();//<span style="color:#ff0000;">获取SurfaceHolder对象，同时指定callback</span>
        holder.addCallback(this);
        p = new Paint(); // 创建一个画笔对象
        setFocusable(true); // 设置焦点

    }

    public void doDraw() {
        mCanvas = holder.lockCanvas(); // 获得画布对象，开始对画布画画
        mCanvas.drawRGB(255,255, 255); // 把画布填充为黑色
        int waterhigh  = 400;

        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(20);
        p.setColor(Color.BLACK);
        // 定义一个Path对象，开口矩形。
        Path path1 = new Path();
        path1.moveTo(350, 200);
        path1.lineTo(350, 800);
        path1.lineTo(718, 800);
        path1.lineTo(718, 200);
        mCanvas.drawPath(path1, p);

        p.setStyle(Paint.Style.FILL);
        Paint p2 = new Paint(); // 定义画笔2
        p2.setAntiAlias(true);
        p2.setColor(Color.BLUE);
        RectF re3 = new RectF(350,waterhigh, 718, 800);
        mCanvas.drawRoundRect(re3, 15, 15, p2);



        holder.unlockCanvasAndPost(mCanvas); // 完成画画，把画布显示在屏幕上

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // <span style="color:#ff0000;">当SurfaceView被创建时，将画图Thread启动起来。</span>
        mysurfaceviewThread = new MysurfaceviewThread();
        flag = true;
        mysurfaceviewThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // <span style="color:#ff0000;">当SurfaceView被销毁时，释放资源。</span>
        if (mysurfaceviewThread != null) {
            mysurfaceviewThread.exit();
            flag = false;
            mysurfaceviewThread = null;
        }
    }
    /**
     * <span style="color:#ff0000;">内部类 MysurfaceviewThread,该类主要实现对canvas的具体操作</span>。
     * @author xu duzhou
     *
     */
    class MysurfaceviewThread extends Thread {
        private boolean done = false;
        public MysurfaceviewThread() {
            super();
            this.done = false;
        }

        public void exit() {
            // <span style="color:#ff0000;">将done设置为true 使线程中的while循环结束。</span>
            done = true;
            try {
                join();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        public void run() {
            while (flag) {
                doDraw(); // 调用自定义画画方法
                try {
                    Thread.sleep(2000); // 让线程休息50毫秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}