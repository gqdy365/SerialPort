/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.serialport.apis;

import android.util.Log;

public class SerialPort {

	private static final String TAG = SerialPort.class.getSimpleName();
	

	private int mFd=0;

	private SerialPortCallBack mCallback;
	private ReadThread mReadThread;
	private int nReadBufferSize=0;
	private boolean bOpenPort=false;
	private int mBaudrate=0;
	
	public interface SerialPortCallBack{
		void onDataReceived(byte[] buffer, int size);
	}
	
	public boolean isOpen(){
		return bOpenPort;
	}
	
	/*
	 * 返回值：-1表示打开串口出错；0表示串口已打开；1表示正确打开串口
	 */
	public int OpenPort( int baudrate,int bits,char event,int stop, int flags,int readBufferSize,SerialPortCallBack serialPortCallBack) {
		if(isOpen())return 0;
		
		mFd = NativeFunc.open(baudrate, bits, event, stop, flags);

		log("mFd="+mFd);
		if (mFd <=0) {
			logE("open serial port throw IOException");
			return -1;
		}
		
		mBaudrate=baudrate;
		nReadBufferSize=readBufferSize;
		bOpenPort=true;
		mCallback=serialPortCallBack;
		
		return 1;
	}
	
	/*
	 * 返回值：-1表示打开串口出错；0表示串口已打开；1表示正确打开串口
	 */
	public int OpenPortWithPath(String path, int baudrate,int bits,char event,int stop, int flags,int readBufferSize,SerialPortCallBack serialPortCallBack) {
		if(isOpen())return 0;
		
		mFd = NativeFunc.openWithPath(path, baudrate, bits, event, stop, flags);

		log("mFd="+mFd);
		if (mFd <=0) {
			logE("open serial port throw IOException");
			return -1;
		}
		mBaudrate=baudrate;
		nReadBufferSize=readBufferSize;
		bOpenPort=true;
		mCallback=serialPortCallBack;
		
		return 1;
	}
	
	public void ClosePort() {
		stopRead();
		
		if(bOpenPort){
			NativeFunc.close(mFd);
			log("close serial port");
		}
		
		bOpenPort=false;
		mFd=0;
		mCallback=null;
	}
	
	public void startRead(){
		
		stopRead();
		
		mReadThread=new ReadThread(); 
		if(mReadThread!=null) 
		{
			mReadThread.start();
			log("start ReadThread");
		}
	}
	
	public void stopRead(){
		if(mReadThread!=null){
			mReadThread.interrupt();
			log("stop ReadThread");
		}
		mReadThread=null;
	}
	
	//buffer：格式为{(byte) 0x91, (byte) 0x81, (byte) 0x8C}
	//返回值：-1表示出错；>=0表示写入成功
	public int writeData(byte[] buffer){
		if(!isOpen()){
			logE("Serial port is not open");
			return -1;
		}
		if(buffer==null || buffer.length==0){
			logE("writeData buffer== null");
			return -1;
		}
		log("writeData buffer[]="+buffer);
		return NativeFunc.write(mFd, buffer, buffer.length);
	}
	
	//返回值：-1表示出错；>=0表示写入成功
	public int writeData(String buffer){
		if(!isOpen()){
			logE("Serial port is not open");
			return -1;
		}
		if(buffer==null){
			logE("writeData buffer== null");
			return -1;
		}
		
		byte[] buf=HexString2Bytes(buffer);
		int res= NativeFunc.write(mFd, buf, buf.length);
		
		log("writeData buffer="+buffer+",res="+res);
		
		return res;
	}

	private byte[] HexString2Bytes(String src){ 
		
		byte[] tmp = src.getBytes(); 
		int bytes=tmp.length/2;
		
		byte[] ret = new byte[bytes]; 
		
		log("leng="+bytes);
		
		for(int i=0; i<bytes; i++){ 
			ret[i] = uniteBytes(tmp[i*2], tmp[i*2+1]); 
		} 
		return ret; 
	} 
	
	private byte uniteBytes(byte src0, byte src1) { 
		byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue(); 
		_b0 = (byte)(_b0 << 4); 
		byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue(); 
		byte ret = (byte)(_b0 ^ _b1); 
		return ret; 
	} 
	
	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			log("ReadThread run");
			byte[] buffer = new byte[nReadBufferSize];
			while(!isInterrupted()) {
				
				log("-------------------");
				int size =NativeFunc.read(mFd,buffer,buffer.length,mBaudrate);
				
				if (size > 0) {
					if(mCallback!=null){
						mCallback.onDataReceived(buffer, size);
						buffer = new byte[nReadBufferSize];
					}
				}
			}
			log("ReadThread run end");
		}
	}
	
//	public void watchDogCtrl(int type){
//		WatchDogCtrl(type);
//	}
	
	
	
	private void log(String msg){
		Log.d(TAG,this.toString()+"=>"+msg);
	}
	private void logE(String msg){
		Log.e(TAG,this.toString()+"=>"+msg);
	}
}
