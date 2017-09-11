package net.analizer.rxbus;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import net.analizer.rxbuslib.RxBus;
import net.analizer.rxbuslib.annotations.Subscribe;
import net.analizer.rxbuslib.annotations.SubscribeReplay;
import net.analizer.rxbuslib.annotations.SubscribeTag;
import net.analizer.rxbuslib.threads.EventThread;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Subscribe(
            observeOn = EventThread.MAIN_THREAD,
            subscribeOn = EventThread.MAIN_THREAD,
            tags = {@SubscribeTag("test")}
    )
    public void testSubscibe(String msg) {
        Log.e(TAG, String.format("msg: %s -> %s", msg, Thread.currentThread()));
    }

    @SubscribeReplay(
            tags = {@SubscribeTag("test")}
    )
    public void testSubscibeReplay(String msg) {
        Log.e(TAG, String.format("msg replay: %s -> %s", msg, Thread.currentThread()));
    }

    RxBus bus = new RxBus();
    static int cnt = 0;

    Object obj1 = null;
    Object obj2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button subscribeMainActivity = findViewById(R.id.subscribeMainActivity);
        Button subscribeObj1 = findViewById(R.id.subscribeObj1);
        Button subscribeObj2 = findViewById(R.id.subscribeObj2);

        Button unsubscribeMainActivity = findViewById(R.id.unsubscribeMainActivity);
        Button unsubscribeObj1 = findViewById(R.id.unsubscribeObj1);
        Button unsubscribeObj2 = findViewById(R.id.unsubscribeObj2);

        subscribeMainActivity.setOnClickListener(v -> {
            bus.register(MainActivity.this);
        });

        subscribeObj1.setOnClickListener(v -> {
            if (obj1 == null) {
                obj1 = new Object() {
                    @SubscribeReplay(
                            tags = {@SubscribeTag("test")}
                    )
                    public void testSubscibeReplay(String msg) {
                        Log.e(TAG, String.format("obj1 replay: %s -> %s", msg, Thread.currentThread()));
                    }
                };
            }

            bus.register(obj1);
        });

        subscribeObj2.setOnClickListener(v -> {
            if (obj2 == null) {
                obj2 = new Object() {
                    @SubscribeReplay(
                            tags = {@SubscribeTag("test")}
                    )
                    public void testSubscibeReplay(String msg) {
                        Log.e(TAG, String.format("obj2 replay: %s -> %s", msg, Thread.currentThread()));
                    }
                };
            }

            bus.register(obj2);
        });

        unsubscribeMainActivity.setOnClickListener(v -> {
            bus.unRegister(MainActivity.this);
        });

        unsubscribeObj1.setOnClickListener(v -> {
            if (obj1 != null) {
                bus.unRegister(obj1);
            }
        });

        unsubscribeObj2.setOnClickListener(v -> {
            if (obj2 != null) {
                bus.unRegister(obj2);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            bus.postPublish(String.format("test %s", cnt), "test");
            bus.postReplay(String.format("test %s", cnt++), "test");
        });
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
        if (obj1 != null) {
            bus.unRegister(obj1);
        }
        if (obj2 != null) {
            bus.unRegister(obj2);
        }
    }
}
