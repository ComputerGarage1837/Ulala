package ca.loonandhearth.staff;
import android.app.*;
import android.content.*;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.*;
public class PushService extends FirebaseMessagingService {
 public static void channel(Context c){NotificationManager n=c.getSystemService(NotificationManager.class);n.createNotificationChannel(new NotificationChannel("orders","New orders",NotificationManager.IMPORTANCE_HIGH));}
 @Override public void onNewToken(String token){getSharedPreferences("staff",0).edit().putString("fcm",token).apply();if(!Vault.read(this).isEmpty())new Thread(()->{try{Api.call(this,"/device",new org.json.JSONObject().put("fcm_token",token));}catch(Exception ignored){}}).start();}
 @Override public void onMessageReceived(RemoteMessage m){if(Vault.read(this).isEmpty())return;channel(this);int id;try{id=Integer.parseInt(m.getData().getOrDefault("order_id","0"));}catch(Exception e){return;}Intent i=new Intent(this,MainActivity.class).putExtra("order_id",id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);PendingIntent p=PendingIntent.getActivity(this,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);var notification=new NotificationCompat.Builder(this,"orders").setSmallIcon(ca.loonandhearth.staff.R.drawable.ic_staff).setContentTitle("New Loon & Hearth order").setContentText("An order is ready to review.").setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setContentIntent(p).setAutoCancel(true).build();try{getSystemService(NotificationManager.class).notify(id,notification);}catch(SecurityException ignored){}}
}
