package com.example.cityreport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    EditText email;
    EditText passwd;
    Button login;
    Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email=findViewById(R.id.textoEmail);
        passwd=findViewById(R.id.textoPassword);
        login=findViewById(R.id.boton_login);
        register=findViewById(R.id.botonRegister);



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

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                //intent.putExtra("id_user",s.toString()); comprobar si tiene arroba y ponerlo en email
                startActivity(intent); //Iniciar la actividad principal

            }

        });

        try {
            cargarCredenciales(); //Intenta cargar credenciales guardados
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private void validar_login() throws IOException, NoSuchAlgorithmException, URISyntaxException {
        final String mail=email.getText().toString();
        final String password=passwd.getText().toString();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

        final String pass_digest=hexString.toString();
        //Toast.makeText(LoginActivity.this, pass_digest,Toast.LENGTH_LONG).show();

        String link ="https://www.cityreport.ga/funcionesphp/validar.php";

        final ProgressDialog loading = ProgressDialog.show(this, "Validando...", "Espere por favor...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Descartar el diálogo de progreso
                        loading.hide();

                        if (s.trim().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Error: datos inválidos!" ,Toast.LENGTH_LONG).show();
                        }

                        else {
                            guardarCredenciales(mail,password); //Guarda las preferencias de inicio de sesión
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("id_user",s.toString());
                            startActivity(intent); //Iniciar la actividad principal
                            finish(); //Finalizar esta actividad

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Descartar el diálogo de progreso
                        loading.hide();

                        //Showing toast
                        Toast.makeText(LoginActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                //Creación de parámetros
                Map<String, String> params = new HashMap<>();

                //Agregando de parámetros
                params.put("email", mail);
                params.put("password",pass_digest);

                //Parámetros de retorno
                return params;

            }
        };

        //Creación de una cola de solicitudes
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Agregar solicitud a la cola
        requestQueue.add(stringRequest);

    }

    private void guardarCredenciales(String usuario, String pass) {
        SharedPreferences preferencias = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();

        editor.putString("usuario",usuario);
        editor.putString("pass",pass);

        editor.commit();
    }

    private void cargarCredenciales() throws NoSuchAlgorithmException, IOException, URISyntaxException {
        SharedPreferences preferencias = getSharedPreferences("credenciales", Context.MODE_PRIVATE);

        String usuario = preferencias.getString("usuario","null"); //Devuelve "null" si no es valido
        String pass = preferencias.getString("pass","null");

        if(!usuario.equals("null") && !pass.equals("null") && preferencias.contains("usuario") && preferencias.contains("pass"))
        {
            email.setText(usuario);
            passwd.setText(pass);
            validar_login();
        }



    }

}
