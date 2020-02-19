package com.example.cityreport;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class LoginActivity extends AppCompatActivity {
    EditText email;
    EditText passwd;
    Button login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email=findViewById(R.id.textoEmail);

        passwd=findViewById(R.id.textoPassword);

        login=findViewById(R.id.boton_login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.getText().toString().isEmpty()&&!passwd.getText().toString().isEmpty()){
                    try {
                        validar_login();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    private void validar_login() throws IOException, NoSuchAlgorithmException, URISyntaxException {
        String mail=email.getText().toString();

        String password=passwd.getText().toString();

        String pass_digest="";



            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            pass_digest=hexString.toString();

      /*  String link ="https://www.cityreport.ga/funcionesphp/validar.php?email="+mail+"&password="+pass_digest;

        URL url = new URL(link);
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        request.setURI(new URI(link));
        HttpResponse response = client.execute(request);
        BufferedReader in = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        StringBuffer sb = new StringBuffer("");
        String line="";

        while ((line = in.readLine()) != null) {
            sb.append(line);
            break;
        }





        Toast.makeText(LoginActivity.this, sb.toString(),Toast.LENGTH_LONG).show();*/





    }
}
