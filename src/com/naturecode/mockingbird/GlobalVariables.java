package com.naturecode.mockingbird;

import android.app.Application;

public class GlobalVariables extends Application {
    private String keep_path;
    
    @Override
    public void onCreate() {
//    	reinitialize variable
    }

	public String getKeep_path() {
		return keep_path;
	}

	public void setKeep_path(String keep_path) {
		this.keep_path = keep_path;
	}
}