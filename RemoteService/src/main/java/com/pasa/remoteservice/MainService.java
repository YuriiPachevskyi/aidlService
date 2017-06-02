package com.pasa.remoteservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.pasa.aidl.testlib.IMainService;

public class MainService extends Service {
    private static final String TAG = "MainService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: Received start command.");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: Received binding.");
        return mBinder;
    }

    private final IMainService.Stub mBinder = new IMainService.Stub() {
        @Override
        public void exit() throws RemoteException {
            Log.e(TAG, "exit: Received exit command.");
            stopSelf();
        }
    };
}
