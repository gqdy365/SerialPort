package com.serialport.apis;


public class NativeFunc {
	
	private volatile static NativeFunc instance = null;
    private NativeFunc(){}
    
    public static NativeFunc getInstance(){
        if(instance == null){
            synchronized (NativeFunc.class) {
                if(instance == null){
                    instance = new NativeFunc();
                }
            }
        }
        return instance;
    }

    
    public native static int openWithPath(String path, int baudrate,int bits,char event,int stop, int flags);
    public native static int open(int baudrate,int bits,char event,int stop, int flags);
    public native static int write(int fd, byte[] buf, int sizes);
    public native static int read(int fd, byte[] buf, int sizes,int baudrate);
    public native static void close(int fd);
	
	/**
	 * 
	 * @param type 0:Stop feeding the dog   1:Start to feed the dog
	 * @return
	 */
    public native static int WatchDogCtrl(int type);
	
	static {
		System.loadLibrary("SerialApi");
	}
}
