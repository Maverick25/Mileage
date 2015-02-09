package net.workbook.mileage.support;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class LoginClient 
{
	private static HttpClient client;
	private static HttpContext localContext;
	private static CookieStore cookieStore;
    
    public static HttpResponse execute(HttpHost host,HttpUriRequest request)
    {
    	if (client == null)
        {
             client = new DefaultHttpClient();
             // Create a local instance of cookie store
             cookieStore = new BasicCookieStore();
             
             // Create local HTTP context
             localContext = new BasicHttpContext();
             
             // Bind custom cookie store to the local context
             localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        }
    	
    	try
    	{
    		return client.execute(host,request, localContext);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    }
    
    public static HttpResponse execute(HttpUriRequest request)
    {
    	if (client == null)
        {
             client = new DefaultHttpClient();
             // Create a local instance of cookie store
             cookieStore = new BasicCookieStore();
             
             // Create local HTTP context
             localContext = new BasicHttpContext();
             
             // Bind custom cookie store to the local context
             localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        }
    	
    	try
    	{
    		return client.execute(request, localContext);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    }
    
    public static void clearCookies()
    {
    	client = null;
    }
    
    public static CookieStore getCookieStore()
    {
    	return cookieStore; 
    }
    
}
