package at.htlbraunau.fcmtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "FCMTest";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging
                .getInstance()
                .getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()){
                            Log.d(TAG,"Fetching FCM registration token failed");
                        }

                        String token = task.getResult();

                        Log.d(TAG,"FCM registration token:" + token);

                        new Thread(() -> {
                            HttpURLConnection huc = null;
                            BufferedWriter bw = null;

                            try
                            {

                                URL url = new URL("https://waser.htl-braunau.at/notifications/api/RegistrationTokens");
                                huc = (HttpURLConnection) url.openConnection();
                                huc.setRequestMethod("POST");
                                huc.setDoOutput(true);
                                huc.setRequestProperty("Content-Type", "application/json");
                                bw = new BufferedWriter(new OutputStreamWriter(huc.getOutputStream()));
                                JSONObject jObj = new JSONObject();
                                jObj.put("LoginName", "Simon.Schwaiger");
                                jObj.put("RegistrationToken", token);
                                bw.write(jObj.toString());
                                bw.flush();
                                int rcode = huc.getResponseCode();
                                Log.d(TAG, huc.getResponseMessage());
                                Log.d(TAG, Integer.toString(rcode));
                                if(rcode == HttpURLConnection.HTTP_CREATED) {
                                    Log.d(TAG, "Responecode: OK => sended");
                                } else {
                                    Log.d(TAG, "Responsecode: BadRequest");
                                }
                            }
                            catch(Exception ex)
                            {
                                System.out.println(ex.getMessage());
                            }
                            finally
                            {
                                if(bw != null)
                                {
                                    try {
                                        bw.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if(huc != null)
                                {
                                    huc.disconnect();
                                }
                            }

                        }).start();
                    }
                });

        FirebaseMessaging
                .getInstance()
                .subscribeToTopic("htlbraunau_news")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"Topic subscription successful");
                        } else {
                            Log.d(TAG,"Topic subscription failed");
                        }
                    }
                });
    }
}