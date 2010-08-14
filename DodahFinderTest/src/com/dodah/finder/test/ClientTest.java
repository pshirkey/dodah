package com.dodah.finder.test;

import junit.framework.Assert;

import com.dodah.service.*;

import android.test.*;

public class ClientTest extends AndroidTestCase{
	
	private final String email = "test@example.com";
	private final String password = "salsa";
	
	public ClientTest(){
		
	}
	
	public void testAuthenticate(){
		Client c = new Client();
		String access_token = c.Authenticate(email, password);
		Assert.assertTrue((access_token != null && access_token.length() > 0));
	}
	
}