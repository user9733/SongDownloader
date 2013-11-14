package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WebPageReader {


	public static String readWebPage(String urlString){
		return readWebPageWithDelay(urlString, 0);
	}
	
	public static String readWebPageWithDelay(String urlString, int delayInMiliseconds){
		String retVal="";
		try {
			URL url = new URL(getUrl(urlString));
			URLConnection con = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			Thread.sleep(delayInMiliseconds);
			while ((line=reader.readLine())!=null){
				retVal+=line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	public static String getUrl(String urlString){
		if (!urlString.startsWith("http://")){
			return "http://"+urlString;
		}
		return urlString;
	}
}
