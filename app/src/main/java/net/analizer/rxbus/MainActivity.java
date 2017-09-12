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
import net.analizer.rxbuslib.annotations.SubscribeBehavior;
import net.analizer.rxbuslib.annotations.SubscribeReplay;
import net.analizer.rxbuslib.annotations.SubscribeTag;
import net.analizer.rxbuslib.threads.EventThread;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static int cnt = 0;
    private RxBus bus = new RxBus();

    private Object anonymousSubscriber1 = null;
    private Object anonymousSubscriber2 = null;

    @Subscribe(
            observeOn = EventThread.MAIN_THREAD,
            subscribeOn = EventThread.MAIN_THREAD
    )
    public void testSubscibePublishDefault(String msg) {
        print(String.format("msg: %s -> %s", msg, Thread.currentThread()));
    }

    @Subscribe(
            // As of version 0.1, the consequent observeOn and subscribeOn are ignored.
            observeOn = EventThread.MAIN_THREAD,
            subscribeOn = EventThread.MAIN_THREAD,
            tags = {@SubscribeTag("test")}
    )
    public void testSubscibePublishWithTag(String msg) {
        print(String.format("msg: %s -> %s", msg, Thread.currentThread()));
    }

    @SubscribeReplay(
            tags = {@SubscribeTag("test")}
    )
    public void testSubscibeReplay(String msg) {
        print(String.format("msg replay: %s -> %s", msg, Thread.currentThread()));
    }

    @SubscribeBehavior(
            tags = {@SubscribeTag("test")}
    )
    public void testSubscibeBehavior(String msg) {
        print(String.format("msg replay: %s -> %s", msg, Thread.currentThread()));
    }

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
            if (anonymousSubscriber1 == null) {
                anonymousSubscriber1 = new Object() {
                    @SubscribeReplay(
                            tags = {@SubscribeTag("test")}
                    )
                    public void testSubscibeReplay(String msg) {
                        Log.e(TAG, String.format("anonymousSubscriber1 replay: %s -> %s", msg, Thread.currentThread()));
                    }
                };
            }

            bus.register(anonymousSubscriber1);
        });

        subscribeObj2.setOnClickListener(v -> {
            if (anonymousSubscriber2 == null) {
                anonymousSubscriber2 = new Object() {
                    @SubscribeReplay(
                            tags = {@SubscribeTag("test")}
                    )
                    public void testSubscibeReplay(String msg) {
                        Log.e(TAG, String.format("anonymousSubscriber2 replay: %s -> %s", msg, Thread.currentThread()));
                    }
                };
            }

            bus.register(anonymousSubscriber2);
        });

        unsubscribeMainActivity.setOnClickListener(v -> {
            bus.unRegister(MainActivity.this);
        });

        unsubscribeObj1.setOnClickListener(v -> {
            if (anonymousSubscriber1 != null) {
                bus.unRegister(anonymousSubscriber1);
            }
        });

        unsubscribeObj2.setOnClickListener(v -> {
            if (anonymousSubscriber2 != null) {
                bus.unRegister(anonymousSubscriber2);
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
        if (anonymousSubscriber1 != null) {
            bus.unRegister(anonymousSubscriber1);
        }
        if (anonymousSubscriber2 != null) {
            bus.unRegister(anonymousSubscriber2);
        }
    }

    private void print(String msg) {

    }
}
