package com.example.user.preconsumerapp;

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

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView verResult,publicKeyString,signatureString;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        verResult = (TextView)findViewById(R.id.verifyResult);
        publicKeyString = (TextView)findViewById(R.id.pubKey);
        signatureString = (TextView)findViewById(R.id.signature);
        btn = (Button)findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                final String url  = "http://192.168.56.1:8080/";
                //new JsonObjectRequest()

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

    }
}
