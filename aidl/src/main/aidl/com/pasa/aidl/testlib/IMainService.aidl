package com.pasa.aidl.testlib;

interface IMainService {
    void readInputFileDescriptor(in ParcelFileDescriptor input);
    void exit();
}
