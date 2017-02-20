package com.example.user.preconsumerapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    TextView verResult,encryptedEncodedJson,encryptedEncodedKey,batchID;
    Button btn,btnScan,btnPost;
    RequestQueue queue;
    JsonObjectRequest postRequest;
    String message;
    String encryptedData,encryptedKey,filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verResult = (TextView)findViewById(R.id.verifyResult);
        encryptedEncodedJson = (TextView)findViewById(R.id.encryptedEncodedJson);
        encryptedEncodedKey = (TextView)findViewById(R.id.encryptedEncodedKey);
        btn = (Button)findViewById(R.id.btn);


        batchID = (TextView)findViewById(R.id.batchid);
        btnScan = (Button)findViewById(R.id.scan);
        btnPost = (Button)findViewById(R.id.post);

        queue = Volley.newRequestQueue(getApplicationContext());

        DownloadFile dl = new DownloadFile();
        dl.execute();
        Log.d("filepath?", filePath);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queue = Volley.newRequestQueue(getApplicationContext());

                final String url  = "http://192.168.212.90:7080/";

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String)null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       // Log.d("response", response.toString());
                        try{
                            Log.d("Obj1",response.getString("encodedEncryptedJson"));
                            Log.d("Obj2",response.getString("encodedEncryptedKey"));
                            encryptedData = response.getString("encodedEncryptedJson");
                            encryptedKey = response.getString("encodedEncryptedKey");
                            encryptedEncodedJson.setText("Encrypted Json: " + response.getString("encodedEncryptedJson"));
                            encryptedEncodedKey.setText("Encrypted Key: " +response.getString("encodedEncryptedKey"));
                            verResult.setText("Verified: ");
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("aaaa",error.toString());

                    }
                });

//                StringRequest getRequest = new StringRequest(Request.Method.GET, url,
//                        new Response.Listener<String>()
//                        {
//                            @Override
//                            public void onResponse(String response) {
//                                // display response
//                                Log.d("Response", response.toString());
//                               // eL.setText(response);
//                            }
//                        },
//                        new Response.ErrorListener()
//                        {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                Log.d("Error.Response", error.toString());
//                            }
//                        }
//                );
                queue.add(getRequest);
            }
        });


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan(); // intent to open external qr app
            }
        });


        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                message = batchID.getText().toString();
                String link = "http://192.168.0.104:6876/nxt?requestType=sendMessage&secretPhrase=appear%20morning%20crap%20became%20fire%20liquid%20probably%20tease%20rare%20swear%20shut%20grief&recipient=NXT-2N9Y-MQ6D-WAAS-G88VH&message=" +  message+",encryptedlocation"+"&deadline=60&feeNQT=100000000";  // nxt api call for sending message

                try{
                    URL url = new URL(link);  // convert string to proper url
                    postRequest = new JsonObjectRequest(Request.Method.POST, url.toString(),(String)null,
                            new Response.Listener<JSONObject>()
                            {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // response
                                    try{
                                        Log.d("Response", response.getString("transaction"));
                                    }catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }

                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    Log.d("Error.Response", error.toString());
                                }
                            }
                    );

                }catch (MalformedURLException e){
                    e.printStackTrace();
                }
                queue.add(postRequest);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Log.d("result",scanResult.toString());
            batchID.setText(scanResult.toString());
        }
    }

    class DownloadFile extends AsyncTask<String,String,String>
    {
        ProgressDialog loading;
        String FILE_URL="https://atos-x509certs-jiahong96.c9users.io/cacert.pem";
        String FILE_Name="cacert";
        String temp;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            loading = ProgressDialog.show(getApplicationContext(),"Loading...","Wait...", true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            filePath=result;
            loading.dismiss();
        }

        @Override
        protected String doInBackground(String... file_path) {
            int readBytes;

            String path;

//                temp= Environment.getExternalStorageDirectory().getPath()+"/"+FILE_Name+".pem";
//                temp.replaceAll("\\s"," ");
//                File f = new File(temp);
//                Log.d("bba",f.toString());
//                if(f.exists())
//                {
//                    Log.d("bb","bb");
//                    path=f.toString();
//                    return path;
//                }
//
//                else{
                    try{
                        URL url = new URL(FILE_URL);
                        URLConnection connection=url.openConnection();
                        long fileLength=connection.getContentLength();

                        InputStream input = new BufferedInputStream(url.openStream(),10*1024);
                        OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/"+FILE_Name+".pem");
                        byte data[]=new byte[1024];
                        long totalBytes = 0;

                        while((readBytes=input.read(data))!=-1)
                        {
                            totalBytes=totalBytes+readBytes;
                            Long percentage = (totalBytes*100)/fileLength;
                            publishProgress(String.valueOf(percentage));
                            output.write(data,0,readBytes);
                        }

                        output.flush();
                        output.close();
                        input.close();
                        path = Environment.getExternalStorageDirectory().toString()+"/"+FILE_Name+".pem";
                        Log.d("got path?",path);
                        return path;
                    }catch(Exception e){
                        Log.d("Error",e.getMessage());
                    }
//                }
            return null;
        }
    }

}
