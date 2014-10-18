package com.serialport.apis;

import android.content.Context;
import android.util.Log;

import com.serialport.apis.SerialPort;
import com.serialport.apis.SerialPort.SerialPortCallBack;
import com.serialport.apis.SerialPortFinder;


public class PortControl {

	private static final String TAG = SerialPort.class.getSimpleName();
	
	private SerialPort mSerialPort;
	private SerialPortFinder mSerialPortFinder;
	
	private SerialPortCallBack mSerialPortCallBack;
	private PortAnalyse mPortAnalyse;
	
	
	public PortControl(Context ctx){
		mPortAnalyse=new PortAnalyse(ctx);
		
		mSerialPortCallBack=new SerialPortCallBack(){

			public void onDataReceived(byte[] buffer, int size) {
				// TODO Auto-generated method stub
				mPortAnalyse.AnalyseRecvData(buffer,size);
			}
			
		};
		
		mSerialPortFinder=new SerialPortFinder();
		mSerialPort=new SerialPort();
	}
	
	public void freePortControl(){
		mSerialPortFinder=null;
		
		if(mSerialPort!=null){
			mSerialPort.ClosePort();
			mSerialPort=null;
		}
		
		if(mPortAnalyse!=null){
			mPortAnalyse.freePortAnalyse();
			mPortAnalyse=null;
		}
		
		mSerialPortCallBack=null;
	}
	
	public SerialPort getSPort(){
		return mSerialPort;
	}
	
	public SerialPortFinder getSPortFinder(){
		return mSerialPortFinder;
	}
	
	public SerialPortCallBack getCallBack(){
		return mSerialPortCallBack;
	}
	
	
	
}
