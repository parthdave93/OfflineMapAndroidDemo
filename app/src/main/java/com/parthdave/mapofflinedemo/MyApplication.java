package com.parthdave.mapofflinedemo;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

/**
 * Created by Parth Dave on 3/6/17.
 * Spaceo Technologies Pvt Ltd.
 * parthd.spaceo@gmail.com
 */

public class MyApplication  extends MultiDexApplication{
	
	private static MyApplication app;
	
	@Override public void onCreate() {
		super.onCreate();
		app = this;
	}
	
	public static MyApplication getAppContext(){
		return app;
	}
}
