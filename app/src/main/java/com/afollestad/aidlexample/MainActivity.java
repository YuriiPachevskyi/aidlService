package com.afollestad.aidlexample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.pasa.aidl.testlib.IMainService;

public class MainActivity extends AppCompatActivity {

    private IMainService mService;
    private TextView mLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLog = (TextView) findViewById(R.id.log);

        Intent serviceIntent = new Intent()
                .setComponent(new ComponentName(
                        "com.pasa.remoteservice",
                        "com.pasa.remoteservice.MainService"));
        mLog.setText("Starting service…\n");
        startService(serviceIntent);
        mLog.append("Binding service…\n");
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mLog.append("Service binded!\n");
            mService = IMainService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mLog.append("Service disconnected.\n");
        }
    };
}
