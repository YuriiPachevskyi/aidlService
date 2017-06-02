package com.pasa.aidl.testlib;

interface IMainService {
    void readInputFileDescriptor(in ParcelFileDescriptor input);
    void writeOutputFileDescriptor(in ParcelFileDescriptor output);
    void exit();
}
