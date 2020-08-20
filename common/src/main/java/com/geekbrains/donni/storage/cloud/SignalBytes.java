package com.geekbrains.donni.storage.cloud;

public class SignalBytes {
    public static final byte AUTH = 10;
    public static final byte AUTH_OK = 11;
    public static final byte AUTH_NOT_OK = 12;
    public static final byte DOWNLOAD_FILE = 20;
    public static final byte UPLOAD_FILE = 21;
    public static final byte GET_INFO_ABOUT_DIRECTORY = 30;
    public static final byte START_SENDING_INFO_ABOUT_DIRECTORY = 31;
    public static final byte STOP_SENDING_INFO_ABOUT_DIRECTORY = 32;
}
