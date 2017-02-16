package com.example.user.preconsumerapp;

import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView verResult,publicKeyString,signatureString,batchID;
    Button btn,btnScan,btnPost;
    RequestQueue queue;
    JsonObjectRequest postRequest;
    String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verResult = (TextView)findViewById(R.id.verifyResult);
        publicKeyString = (TextView)findViewById(R.id.pubKey);
        signatureString = (TextView)findViewById(R.id.signature);
        btn = (Button)findViewById(R.id.btn);


        batchID = (TextView)findViewById(R.id.batchid);
        btnScan = (Button)findViewById(R.id.scan);
        btnPost = (Button)findViewById(R.id.post);

        queue = Volley.newRequestQueue(getApplicationContext());


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queue = Volley.newRequestQueue(getApplicationContext());

                final String url  = "http://192.168.56.1:8080/";

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String)null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       // Log.d("response", response.toString());
                        try{
                           Log.d("response1",response.getString("publicKey"));
                            Log.d("response2",response.getString("encryptedLocation"));
                            String pubKey = response.getString("publicKey");
                            String signature = response.getString("encryptedLocation");
                            VerSig verify = new VerSig();
                            Boolean verified = verify.verifySignature(pubKey,signature);
                            if(verified){
                                publicKeyString.setText("PublicKey: "+pubKey);
                                signatureString.setText("Signature: "+signature);
                                verResult.setText("Verified: "+verified);
                            }else {
                                publicKeyString.setText("PublicKey: "+pubKey);
                                signatureString.setText("Signature: "+signature);
                                verResult.setText("Verified: "+verified);
                            }
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

}
