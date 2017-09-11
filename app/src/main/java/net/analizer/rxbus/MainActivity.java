package net.analizer.rxbus;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.analizer.rxbuslib.RxBus;
import net.analizer.rxbuslib.annotations.Subscribe;
import net.analizer.rxbuslib.annotations.SubscribeReplay;
import net.analizer.rxbuslib.annotations.Tag;
import net.analizer.rxbuslib.threads.EventThread;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Subscribe(
            observeOn = EventThread.MAIN_THREAD,
            subscribeOn = EventThread.MAIN_THREAD,
            tags = {@Tag("test")}
    )
    public void testSubscibe(String msg) {
        Log.e(TAG, String.format("msg: %s -> %s", msg, Thread.currentThread()));
    }

    @SubscribeReplay(
            tags = {@Tag("test")}
    )
    public void testSubscibeReplay(String msg) {
        Log.e(TAG, String.format("msg replay: %s -> %s", msg, Thread.currentThread()));
    }

    RxBus bus = new RxBus();
    static int cnt = 0;

    Object obj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Snackbar
                    .make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
            bus.postPublish(String.format("test %s", cnt), "test");
            bus.postReplay(String.format("test %s", cnt++), "test");
            if (cnt >= 5) {
                if (obj == null) {
                    obj = new Object() {
                        @SubscribeReplay(
                                tags = {@Tag("test")}
                        )
                        public void testSubscibeReplay(String msg) {
                            Log.e(TAG, String.format("obj replay: %s -> %s", msg, Thread.currentThread()));
                        }
                    };
                    bus.register(obj);
                }
            }
        });

        bus.register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unRegister(this);
        if (obj != null) {
            bus.unRegister(obj);
        }
    }
}
