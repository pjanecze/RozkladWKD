package pj.rozkladWKD;


/***
 Copyright (c) 2009 
 Author: Stefan Klumpp <stefan.klumpp@gmail.com>
 Web: http://stefanklumpp.com

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class HttpClient {
 private static final String TAG = "HttpClient";

 public static JSONObject SendHttpPost(List<NameValuePair> nameValuePairs) throws JSONException, ClientProtocolException, IOException {

  

	// Create a new HttpClient and Post Header  
	    DefaultHttpClient httpclient = new DefaultHttpClient();  
	    HttpPost httppost = new HttpPost("http://www.mmaj.nazwa.pl/rozkladwkd/listener2.php");  
	    nameValuePairs.add(new BasicNameValuePair("app_ver", RozkladWKD.APP_VERSION));
	 
        // Add your data  
    	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF_8"));  
  
        // Execute HTTP Post Request  
        HttpResponse response = httpclient.execute(httppost);  
          
        HttpEntity entity = response.getEntity();

        if (entity != null) {
         // Read the content stream
     	  
         InputStream instream = entity.getContent();
         Header contentEncoding = response.getFirstHeader("Content-Encoding");
         if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
          instream = new GZIPInputStream(instream);
         }

         // convert content stream to a String
         String resultString= convertStreamToString(instream);
         
         instream.close();
         //resultString = resultString.substring(1,resultString.length()-1); // remove wrapping "[" and "]"

         // Transform the String into a JSONObject
         if(RozkladWKD.DEBUG_LOG) {
        	 Log.i(TAG,"result: " + resultString);
         }
         JSONObject jsonObjRecv = new JSONObject(resultString);
         // Raw DEBUG output of our received JSON object:
         if(RozkladWKD.DEBUG_LOG) {
        	 Log.i(TAG,"<jsonobject>\n"+jsonObjRecv.toString()+"\n</jsonobject>");
         }

         return jsonObjRecv;
        } 
	  

 
  return null;
 }


 private static String convertStreamToString(InputStream is) {
  /*
   * To convert the InputStream to String we use the BufferedReader.readLine()
   * method. We iterate until the BufferedReader return null which means
   * there's no more data to read. Each line will appended to a StringBuilder
   * and returned as String.
   * 
   * (c) public domain: http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/
   */
  BufferedReader reader = new BufferedReader(new InputStreamReader(is));
  StringBuilder sb = new StringBuilder();

  String line = null;
  try {
   while ((line = reader.readLine()) != null) {
    sb.append(line + "\n");
   }
  } catch (IOException e) {
   e.printStackTrace();
  } finally {
   try {
    is.close();
   } catch (IOException e) {
    e.printStackTrace();
   }
  }
  return sb.toString();
 }

}

