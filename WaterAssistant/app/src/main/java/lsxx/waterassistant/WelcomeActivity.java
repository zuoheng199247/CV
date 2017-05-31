package lsxx.waterassistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        handler2.postDelayed(runnable2,2000);

    }

    //计时线程2
    android.os.Handler handler2 = new android.os.Handler();
    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {

            Intent intent=new Intent(WelcomeActivity.this,LoginActivity.class);
            startActivity(intent);
            handler2.removeCallbacks(runnable2);
//            handler2.postDelayed(this,  400);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
