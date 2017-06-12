package com.pasa.remoteclient;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import com.pasa.remoteclient.servicebindingtool.BaseLocalBinder;

import java.util.ArrayList;
import java.util.List;

public class SimpleService extends Service {
    private static final String TAG = "SimpleService";
    private final IBinder mBinder = new BaseLocalBinder<>(SimpleService.this);
    private final List<ServiceStateListener> mServiceStateListeners = new ArrayList<>();
    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;
    private Handler mMainHandler;
    private boolean mIsStarted;
    private int mLastStartId;

    public interface ServiceStateListener {
        void onServiceStateChanged(boolean isStarted);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
        mMainHandler = new Handler(getMainLooper());
        startWorkerThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(intent=" + intent + ", flags=" + flags + ", startId=" + startId + ")");
        mLastStartId = startId;

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        mWorkerThread.quitSafely();
        mWorkerHandler = null;

        super.onDestroy();
    }

    public void start(@NonNull final Bundle extraParams) {
        Log.d(TAG, "start()");
        startService(new Intent(this, getServiceClass()));
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mIsStarted) {
                    onStart(extraParams);
                    setServiceState(true);
                }
            }
        });
    }

    public void stop() {
        Log.d(TAG, "stop()");
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mIsStarted) {
                    onStop();
                    stopSelf(mLastStartId);
                    setServiceState(false);
                }
            }
        });
    }

    private void setServiceState(boolean isStarted) {
        boolean isServiceStateChanged = false;
        synchronized (this) {
            if (mIsStarted != isStarted) {
                mIsStarted = isStarted;
                isServiceStateChanged = true;
            }
        }

        if (isServiceStateChanged) {
            notifyServiceStateChanged(isStarted);
        }
    }

    private void notifyServiceStateChanged(final boolean isStarted) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                List<ServiceStateListener> listenersCopy = new ArrayList<>(mServiceStateListeners);
                for (ServiceStateListener listener : listenersCopy) {
                    listener.onServiceStateChanged(isStarted);
                }
            }
        });
    }

    public synchronized boolean isStarted() {
        return mIsStarted;
    }

    @UiThread
    public void addServiceStateListener(@NonNull ServiceStateListener listener) {
        if(isStarted()) {
            listener.onServiceStateChanged(true);
        }
        synchronized (this) {
            mServiceStateListeners.add(listener);
        }
    }

    @UiThread
    public void removeServiceStateListener(@NonNull ServiceStateListener listener) {
        synchronized (this) {
            mServiceStateListeners.remove(listener);
        }
    }

    protected Class<? extends SimpleService> getServiceClass() {
        return SimpleService.class;
    }

    private void startWorkerThread() {
        mWorkerThread = new HandlerThread("SimpleService.thread");
        mWorkerThread.start();
        mWorkerHandler = new Handler(mWorkerThread.getLooper());
    }

    public void runOnServiceSide() {
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "runOnServiceSide: ");
            }
        });
    }

    protected void onStart(@NonNull Bundle extraParams) {
    }

    protected void onStop() {
    }
}

