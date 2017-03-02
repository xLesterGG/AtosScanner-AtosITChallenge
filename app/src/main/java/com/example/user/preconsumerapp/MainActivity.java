package com.example.user.preconsumerapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.PublicKey;

public class MainActivity extends AppCompatActivity {

    TextView verResult,encodedEncryptedHash,originalData,batchIDv;
    Button btn,btnScan,btnPost;
    RequestQueue queue;
    JsonObjectRequest postRequest;
    JSONObject responseData;
    String message;
    String link1,link2,link3,link4;
    String encryptedHashData,encryptedHash1,encryptedHash2,encryptedHash3,original,filePath,temp,nxtAccNum,batchID,productName;
    Boolean verified;
    Spinner spinner;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(MainActivity.this);

        verResult = (TextView)findViewById(R.id.verifyResult);
        encodedEncryptedHash = (TextView)findViewById(R.id.encodedEncryptedHash);
        originalData = (TextView)findViewById(R.id.originalData);
        btn = (Button)findViewById(R.id.btn);


        batchIDv = (TextView)findViewById(R.id.batchid);
        btnScan = (Button)findViewById(R.id.scan);
        btnPost = (Button)findViewById(R.id.post);
        spinner = (Spinner)findViewById(R.id.spinner);

        queue = Volley.newRequestQueue(getApplicationContext());

        ArrayAdapter<CharSequence> adapter =  ArrayAdapter.createFromResource(this,R.array.action_array,R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item );
        spinner.setAdapter(adapter);

        //Log.d("filepath?", filePath);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queue = Volley.newRequestQueue(getApplicationContext());

                final String url  = "http://192.168.43.61:7080/";

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String)null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       // Log.d("response", response.toString());
                        try{
                            //get json Object
                            responseData = response;

                            //get strings from json
                            encryptedHash1 = response.getString("encryptedHash1");
                            encryptedHash2 = response.getString("encryptedHash2");
                            encryptedHash3 = response.getString("encryptedHash3");
                            original = response.getString("unhashedData");
                            Log.d("Str1",response.getString("encryptedHash1"));
                            Log.d("Str2",response.getString("encryptedHash2"));
                            Log.d("Original Json",response.getString("unhashedData"));

                            encryptedHashData = encryptedHash1 + encryptedHash2 + encryptedHash3;


                            VerifyHash vh = new VerifyHash();
                            temp= Environment.getExternalStorageDirectory().getPath()+"/cacert.pem";
                            temp.replaceAll("\\s"," ");
                            File f = new File(temp);
                            if(f.exists())
                            {
                                filePath=f.toString();
                                PublicKey key = vh.ReadPemFile(filePath);
                                String decryptedhash = vh.DecryptHash(key,encryptedHashData);

                                //String decryptedhash = vh.DecryptHash(key,response.getString("encryptedHash"));
                                String rehash = vh.hashStringWithSHA(original);
                                verified = vh.CompareHash(decryptedhash,rehash);
                                Log.d("rehash", rehash);
                                Log.d("decryptedhash", decryptedhash);


                                verResult.setText("Verify Result: "+verified);
                            }
                            //DownloadFile dl = new DownloadFile();
                            //dl.execute();
                            //setText

                            originalData.setText("Original Data: " +response.getString("unhashedData"));
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
                //String test1 = "{\"batchID\":\"b001\",\"movement\":\"end\",\"unhashedData\":{\"currentDateTime\":\"Mon, 2017-February-27 13:07:21\",\"location\":\"SWE2222\"}}";
                //String test2 = "{\"batchID\":\"b001\",\"encryptedHash1\":\"70B8148C84309D1220EF60C9EAC5D81E788A40E591BE7C8E6BDC52A661A35FB6E52B5834D8E963E6D0DA4F08EDB1BA9DAFD98119A0D56E124C6A89FC7345A743\"}";
                //String test3 = "{\"batchID\":\"b001\",\"encryptedHash2\":\"160CCFF0811361CE5591A13711D68B91C5B82A31217D934D2711A1AA343CF79DDC7F53834D2CA1009CA2F723FFFB9ED3A21520E06B8496549B54CD5736F132B2\"}";

                String nxtFrontLink = "http://174.140.168.136:6876/nxt?requestType=sendMessage&secretPhrase=appear%20morning%20crap%20became%20fire%20liquid%20probably%20tease%20rare%20swear%20shut%20grief&recipient=NXT-2N9Y-MQ6D-WAAS-G88VH&message=";
                String nxtEndLink = "&deadline=60&feeNQT=0";

                JSONObject toPost1 = new JSONObject();
                JSONObject toPost2 = new JSONObject();
                JSONObject toPost3 = new JSONObject();
                JSONObject toPost4 = new JSONObject();

                //try{
                    //toPost.put("encryptedHash",responseData.getString("encryptedHash"));
                    //toPost.put("encryptedHash","5E674BB98239F4B9BBDD3CF545023FAE421BC0B8C5D0B111111111111111111111111111111111111111111111111111111111111111");
                    //toPost.put("batchID",batchID);
                    // toPost.put("unhashedData",responseData.getString("unhashedData"));
                    //toPost.put("movement",spinner.getSelectedItem().toString().toLowerCase());


                    //Log.d("LOGGG", toPost.toString());

                    //message = batchIDv.getText().toString();

                    //String secret = "bridge twice ash force birth pause trickle sharp tender disappear spoken kid";
                    //secret = secret.replaceAll(" ","%20");
                //}catch (Exception e){
                //   e.printStackTrace();
                //}
                /* link = "http://174.140.168.136:6876/nxt?requestType=sendMessage&secretPhrase="+ secret +"&recipient="+ nxtAccNum +"&message=" + toPost +"&deadline=60&feeNQT=0";  // nxt api call for sending message
                    Log.d("asdf",link);*/

                if(responseData== null){
                    Log.d("NULL","NULLLLLLLLL");
                    Toast.makeText(getApplicationContext(),"Please ensure that you are connected to a network with a working server",Toast.LENGTH_LONG).show();
                }
                else if(nxtAccNum == null){
                    Log.d("null laaaa","aaaaaaaaaaa");
                    Toast.makeText(getApplicationContext(),"Please scan a valid QR before trying to make a transaction",Toast.LENGTH_LONG).show();
                }
                else{
                    Log.d("NULL","not nullllllll");
                    try{
                        //first post data
                        toPost1.put("batchID",batchID);
                        toPost1.put("movement",spinner.getSelectedItem().toString().toLowerCase());
                        toPost1.put("unhashedData",responseData.getString("unhashedData"));
                        link1= nxtFrontLink+toPost1.toString()+nxtEndLink;

                        //second post data
                        toPost2.put("batchID",batchID);
                        toPost2.put("encryptedHash1",responseData.getString("encryptedHash1"));
                        link2= nxtFrontLink+toPost2.toString()+nxtEndLink;

                        //third post data
                        toPost3.put("batchID",batchID);
                        toPost3.put("encryptedHash1",responseData.getString("encryptedHash2"));
                        link3= nxtFrontLink+toPost3.toString()+nxtEndLink;

                        //fourth post data
                        toPost4.put("batchID",batchID);
                        toPost4.put("encryptedHash1",responseData.getString("encryptedHash3"));
                        link4= nxtFrontLink+toPost4.toString()+nxtEndLink;

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    firstPost(link1);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Log.d("result",scanResult.toString());

            try{
                JSONObject qrData = new JSONObject(scanResult.getContents());

                if(qrData.has("nxtAccNum") && qrData.has("batchID") && qrData.has("productName")){
                    Toast.makeText(getApplicationContext(),"Valid FoodChain™ QR detected",Toast.LENGTH_LONG).show();
                    nxtAccNum = qrData.getString("nxtAccNum");
                    batchID = qrData.getString("batchID");        // format of qr data
                    productName = qrData.getString("productName");

                    batchIDv.setText(nxtAccNum + batchID + productName);
                }else{
                    Toast.makeText(getApplicationContext(),"Not a Valid FoodChain™ QR , please try again",Toast.LENGTH_LONG).show();
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public static void verifyStoragePermissions(Activity activity) { // for marshmallow permissions
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    public void firstPost(String urlString) {

        try{
            URL url = new URL(urlString);  // convert string to proper url
            Log.d("url",url.toString());
            postRequest = new JsonObjectRequest(Request.Method.POST, urlString,(String)null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            // response
                            try{
                                Log.d("Response", response.toString(4));
                                Log.d("response",response.toString());

                                secondPost(link2);

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

    public void secondPost(String urlString) {

        try{
            URL url = new URL(urlString);  // convert string to proper url
            Log.d("url",url.toString());
            postRequest = new JsonObjectRequest(Request.Method.POST, urlString,(String)null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            // response
                            try{
                                Log.d("Response", response.toString(4));
                                Log.d("response",response.toString());

                                thirdPost(link3);
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

    public void thirdPost(String urlString) {

        try{
            final URL url = new URL(urlString);  // convert string to proper url
            Log.d("url",url.toString());
            postRequest = new JsonObjectRequest(Request.Method.POST, urlString,(String)null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            // response
                            try{
                                Log.d("Response", response.toString(4));
                                Log.d("response",response.toString());
                                fourthPost(link4);
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

    public void fourthPost(String urlString) {

        try{
            URL url = new URL(urlString);  // convert string to proper url
            Log.d("url",url.toString());
            postRequest = new JsonObjectRequest(Request.Method.POST, urlString,(String)null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            // response
                            try{
                                Log.d("Response", response.toString(4));
                                Log.d("response",response.toString());

                                Toast.makeText(getApplicationContext(),"Successfully posted",Toast.LENGTH_LONG).show();

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
