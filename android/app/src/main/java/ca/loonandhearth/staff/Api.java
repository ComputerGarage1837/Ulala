package ca.loonandhearth.staff;
import android.content.Context;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
final class Api {
 static final class Failure extends IOException {final int code;Failure(int c,String s){super(s);code=c;}}
 static String site(Context c){return c.getSharedPreferences("staff",0).getString("site","https://loonandhearth.ca");}
 static JSONObject call(Context ctx,String path,JSONObject body)throws Exception{
  URL url=new URL(site(ctx)+"/wp-json/lh-staff/v1"+path);HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setRequestProperty("Accept","application/json");c.setRequestProperty("Cache-Control","no-cache");String token=Vault.read(ctx);if(!token.isEmpty())c.setRequestProperty("Authorization","Bearer "+token);
  try{if(body!=null){c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
  int status=c.getResponseCode();InputStream in=status>=400?c.getErrorStream():c.getInputStream();String text;try(in){text=in==null?"{}":new String(Updater.read(in,2000000),StandardCharsets.UTF_8);}JSONObject data;try{data=new JSONObject(text);}catch(Exception e){throw new Failure(status,"The website returned an unexpected response.");}if(status<200||status>=300)throw new Failure(status,data.optString("message","Request failed ("+status+")."));return data;}finally{c.disconnect();}
 }
}
