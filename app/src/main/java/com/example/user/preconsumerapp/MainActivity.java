package com.example.user.preconsumerapp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;

public class MainActivity extends AppCompatActivity {

    TextView verResult,encodedEncryptedHash,originalData,batchID;
    Button btn,btnScan,btnPost;
    RequestQueue queue;
    JsonObjectRequest postRequest;
    JSONObject responseData;
    String message;
    String encryptedHashData,original,filePath,temp;
    Boolean verified;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verResult = (TextView)findViewById(R.id.verifyResult);
        encodedEncryptedHash = (TextView)findViewById(R.id.encodedEncryptedHash);
        originalData = (TextView)findViewById(R.id.originalData);
        btn = (Button)findViewById(R.id.btn);


        batchID = (TextView)findViewById(R.id.batchid);
        btnScan = (Button)findViewById(R.id.scan);
        btnPost = (Button)findViewById(R.id.post);

        queue = Volley.newRequestQueue(getApplicationContext());

        //Log.d("filepath?", filePath);
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
                            //get json Object
                            responseData = response;

                            //get strings from json
                            encryptedHashData = response.getString("encodedEncryptedHash");
                            original = response.getString("originalData");
                            Log.d("Obj1",response.getString("encodedEncryptedHash"));
                            Log.d("Obj2",response.getString("originalData"));

                            VerifyHash vh = new VerifyHash();
                            temp= Environment.getExternalStorageDirectory().getPath()+"/cacert.pem";
                            temp.replaceAll("\\s"," ");
                            File f = new File(temp);
                            if(f.exists())
                            {
                                filePath=f.toString();
                                PublicKey key = vh.ReadPemFile(filePath);
                                String decryptedhash = vh.DecryptHash(key,encryptedHashData);
                                String rehash = vh.hashStringWithSHA(original);
                                verified = vh.CompareHash(decryptedhash,rehash);
                                Log.d("rehash", rehash);
                                Log.d("decryptedhash", decryptedhash);
                                verResult.setText("Verify Result: "+verified);
                            }
                            //DownloadFile dl = new DownloadFile();
                            //dl.execute();

                            //setText

                            originalData.setText("Original Data: " +response.getString("originalData"));
                            verResult.setText("Verified: "+verified);
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
                JSONObject test = new JSONObject();
                try {
                    test.put("abc","aa");
                    test.put("abcd","aa");
                    test.put("abce","aa");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                message = batchID.getText().toString();
                String link = "http://174.140.168.136:6876/nxt?requestType=sendMessage&secretPhrase=appear%20morning%20crap%20became%20fire%20liquid%20probably%20tease%20rare%20swear%20shut%20grief&recipient=NXT-2N9Y-MQ6D-WAAS-G88VH&message=" + test +"&deadline=60&feeNQT=100000000";  // nxt api call for sending message

                try{
                    URL url = new URL(link);  // convert string to proper url
                    postRequest = new JsonObjectRequest(Request.Method.POST, url.toString(),(String)null,
                            new Response.Listener<JSONObject>()
                            {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // response
                                    try{
                                        Log.d("Response", response.toString(4));
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

//    class DownloadFile extends AsyncTask<String,String,String>
//    {
//        ProgressDialog loading;
//        String FILE_URL="https://upload.wikimedia.org/wikipedia/wikimania2014/thumb/e/e2/Ask-Logo-Small.jpg/250px-Ask-Logo-Small.jpg";
//        String FILE_Name="cacert";
//        String temp;
//
//        @Override
//        protected void onPreExecute() {
//
//            super.onPreExecute();
//            loading = ProgressDialog.show(MainActivity.this,"Loading...","Wait...", true, true);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            filePath=result;
//            loading.dismiss();
//        }
//
//        @Override
//        protected String doInBackground(String... file_path) {
//            int readBytes;
//
//            String path;
//
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
//                    try{
//                        URL url = new URL(FILE_URL);
//                        URLConnection connection=url.openConnection();
//                        long fileLength=connection.getContentLength();
//
//                        InputStream input = new BufferedInputStream(url.openStream(),10*1024);
//                        OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/"+FILE_Name+".jpg");
//                        byte data[]=new byte[1024];
//                        long totalBytes = 0;
//
//                        while((readBytes=input.read(data))!=-1)
//                        {
//                            totalBytes=totalBytes+readBytes;
//                            Long percentage = (totalBytes*100)/fileLength;
//                            publishProgress(String.valueOf(percentage));
//                            output.write(data,0,readBytes);
//                        }
//
//                        output.flush();
//                        output.close();
//                        input.close();
//                        path = Environment.getExternalStorageDirectory().toString()+"/"+FILE_Name+".pem";
//                        Log.d("got path?",path);
//                        return path;
//                    }catch(Exception e){
//                        Log.d("Error",e.getMessage());
//                    }
////                }
//            return null;
//        }
//    }

}
