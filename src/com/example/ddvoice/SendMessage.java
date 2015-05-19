package com.example.ddvoice;

import java.util.List;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

public class SendMessage {
	private String mPerson=null;
    private String number=null;
    private String mcontent=null;
    MainActivity mActivity;
    
    
    public SendMessage(String person,String code,String content,MainActivity activity){
    	 mPerson = person;
    	 number=code;
    	 mcontent=content;
 	    mActivity=activity;
 	    
    }
    
    public void start(){
    	if((number==null)||(number.equals(""))){
    		if ((mPerson == null) || (mPerson.equals("")))
    	    {
    			 mActivity.speak("至少告诉我名字或者号码吧？", false);
    	    }else{
    	    	 mPerson=mPerson.trim();
    	    	number=getNumberByName(mPerson,mActivity);
    	    	 if(number == null)
    	         {
    	           mActivity.speak("通讯录没有找到"+mPerson, false);
    	         }else{	    
    	        	 //发短信
    	        	 SmsManager smsManager = SmsManager.getDefault();
    	        	 if(mcontent.length() > 70) {
                         List<String> contents = smsManager.divideMessage(mcontent);
                         for(String sms : contents) {
                             smsManager.sendTextMessage(number, null, sms, null, null);
                             insertDB(number,sms);
                         }
                     } else {
                      smsManager.sendTextMessage(number, null, mcontent, null, null);
                      insertDB(number,mcontent);
                     }
    	        	
    	        }
    	    }
    	}
    	else{
    		if((mcontent==null)||(mcontent.equals(""))){
    			mActivity.serviceFlag=true;
    			mActivity.speak("你要发送什么内容呢？", false);
    			//mActivity.startSpeenchRecognition();
    			//
    			Thread mThread= new Thread(){
    				public void run(){
    					while((mActivity.SRResult==null)||(mActivity.SRResult.equals(""))){
    						//空转
    					}
    					//mActivity.speak("线程中", false);
    					mcontent=mActivity.SRResult;
            			SmsManager smsManager = SmsManager.getDefault();
        	        	 if(mcontent.length() > 70) {
                            List<String> contents = smsManager.divideMessage(mcontent);
                            for(String sms : contents) {
                                smsManager.sendTextMessage(number, null, sms, null, null);
                                insertDB(number,sms);
                            }
                        } else {
                         smsManager.sendTextMessage(number, null, mcontent, null, null);
                         insertDB(number,mcontent);
                        }
        	        	 mActivity.serviceFlag=false;
    				}
    			};
    			mThread.start();
    			//mThread.destroy();
    		}
    		else{
    			 SmsManager smsManager = SmsManager.getDefault();
	        	 if(mcontent.length() > 70) {
                     List<String> contents = smsManager.divideMessage(mcontent);
                     for(String sms : contents) {
                         smsManager.sendTextMessage(number, null, sms, null, null);
                         insertDB(number,sms);
                     }
                 } else {
                  smsManager.sendTextMessage(number, null, mcontent, null, null);
                  insertDB(number,mcontent);
                 }
	        	
    		}
    		
    	}
    	
    }
    
    private void insertDB(String number,String content){//将发送的短信插入系统数据库中，使其在短信界面显示 
    	//////////////////////会抛出null的异常---已解决--- mActivity.getContentResolver()才可以
    	try{
	    	ContentValues values = new ContentValues();
	    	values.put("date", System.currentTimeMillis());
	    	 //阅读状态              
	        values.put("read", 0);             
	         //1为收 2为发             
	       values.put("type", 2);           
	         //送达号码              
	      // values.put("status", -1);
	       values.put("address",number);             
	         //送达内容            
	       values.put("body", content);             
	         //插入短信库    
	      // getContentResolver
	       mActivity.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
	       mActivity.speak("短信发送成功",false);
    	}catch (Exception e) { 
            Log.d("dd", "插入数据库问题："+e.getMessage()); 
    	  }
    }
    
    private  String getNumberByName(String name, Context context)
	  {
		 Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, name);
		  ContentResolver  resolver  = context.getContentResolver();
		  Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts._ID}, null, null, null);  
		  if((cursor!=null)&&(cursor.moveToFirst())){
			  int idCoulmn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			  long id = cursor.getLong(idCoulmn);
		      cursor.close();
		      cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  new String[]{"data1"}, "contact_id = ?",  new String[]{Long.toString(id)}, null);
		      if ((cursor != null) && (cursor.moveToFirst()))
		      {
		        int m = cursor.getColumnIndex("data1");
		        String num = cursor.getString(m);
		        cursor.close();
		       return num;
		      }	      
		  }
		  return null;
	  }
}
