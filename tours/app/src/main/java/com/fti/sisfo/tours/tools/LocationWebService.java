package com.fti.sisfo.tours.tools;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fti.sisfo.tours.MainActivity;
import com.fti.sisfo.tours.MapActivity;
import com.fti.sisfo.tours.R;
import com.fti.sisfo.tours.picasso.GridItem;
import com.fti.sisfo.tours.signin.SignInActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocationWebService extends AsyncTask<String, String, Integer> {

    Context context;
    public LocationWebService(Context context) {
        this.context = context;
        // TODO Auto-generated constructor stub
    }


    @Override
    protected Integer doInBackground(String... params) {
        try {
            // Create Apache HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            // 200 represents HTTP OK
            if (statusCode == 200) {
                //result = 1; // Successful
                Log.e("location web service","http OK");
                String response = streamToString(httpResponse.getEntity().getContent());
                parseResult(response);
            } else {
                //result = 0; //"Failed
                Log.e("failed","Failed to fetch data!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    /*@Override
    protected void onPostExecute(Integer result) {

        if (result == 1) {

        } else {
            Toast.makeText(new MainActivity().getApplicationContext(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
        }

    }*/


    String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }

    private void parseResult(String result) {
        //try {
        Log.e("err", "gagal parse result");
            //JSONObject posts = new JSONObject(result);

            //JSONArray posts = response.optJSONArray("posts");
            //GridItem item;

           /* Intent intent = new Intent(context, SignInActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
            Notification n  = new Notification.Builder(context)
                    .setContentTitle("New mail from " + "test@gmail.com")
                    .setContentText(result)
                    .setSmallIcon(R.drawable.splash_img)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .build();
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, n);
            */
            /*for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                String title = post.optString("name");
                item = new GridItem();
                item.setTitle(title);
                if (post.getString("image") != null) {
                    item.setImage(SERVER_IMG + post.getString("image"));
                }
                /*JSONArray attachments = post.getJSONArray("attachments");
                if (null != attachments && attachments.length() > 0) {
                    JSONObject attachment = attachments.getJSONObject(0);
                    if (attachment != null)
                        item.setImage(attachment.getString("url"));
                }
                mGridData.add(item);
            }*/
        /*} catch (Exception e) {
            Log.e("err", "gagal parse result");
        }*/
    }
}
