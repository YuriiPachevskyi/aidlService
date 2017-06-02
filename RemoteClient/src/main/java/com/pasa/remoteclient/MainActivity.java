package com.pasa.remoteclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import com.pasa.aidl.testlib.IMainService;
import com.pasa.aidl.testlib.IThreadListener;
import com.pasa.aidl.testlib.ParcelFileDescriptorUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivityzzz";
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

            passFileDescriptorForReadToService();
            passFileDescriptorForWriteToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mLog.append("Service disconnected.\n");
        }
    };

    private void passFileDescriptorForReadToService() {
        InputStream is = new ByteArrayInputStream("Hello remote service from client side".getBytes());

        try {
            ParcelFileDescriptor input = ParcelFileDescriptorUtil.pipeFrom(is,
                    new IThreadListener() {
                        @Override
                        public void onThreadFinished(Thread thread) {
                            Log.d(TAG, "Copy to service finished");
                        }
                    });

            mService.readInputFileDescriptor(input);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to pipeFrom intup stream " + e);
        }
        catch (RemoteException e) {
            Log.e(TAG, "Failed to read input stream " + e);
        }
    }

    private void passFileDescriptorForWriteToService() {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        Log.e(TAG, "passFileDescriptorForWriteToService: ");
        try {
            ParcelFileDescriptor output = ParcelFileDescriptorUtil.pipeTo(os,
                    new IThreadListener() {
                        @Override
                        public void onThreadFinished(Thread thread) {
                            Log.e(TAG, "Service wrote result strLen = " + os.toByteArray().length + " result = " + os.toString());
                            mLog.append(os.toString());
                        }
                    });

            mService.writeOutputFileDescriptor(output);
            output.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to pipeFrom intup stream " + e);
        }
        catch (RemoteException e) {
            Log.e(TAG, "Failed to write input stream " + e);
        }
    }
}
