package com.example.cityreport;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    EditText usuario;
    Button register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.textoEmail);
        password = findViewById(R.id.textoPassword);
        usuario = findViewById(R.id.textoUsuario);
        register = findViewById(R.id.botonRegister);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!usuario.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                    try {
                        validar_register();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
                else
                    Toast.makeText(RegisterActivity.this, "Debe rellenar los campos!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void validar_register() throws Exception {
        final String emailS = email.getText().toString();
        final String usuarioS = usuario.getText().toString();
        String passwordS = password.getText().toString();

        if(!emailS.contains("@") || !emailS.contains("."))
            throw new Exception("Error: email no válido!");

        //GENERAR CONTRASEÑA ENCRIPTADA
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(passwordS.getBytes("UTF-8"));
        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        final String pass_digest = hexString.toString();
        //Toast.makeText(LoginActivity.this, pass_digest,Toast.LENGTH_LONG).show();

        String link = "https://www.cityreport.ga/funcionesphp/registro.php";

        final ProgressDialog loading = ProgressDialog.show(this, "Validando...", "Espere por favor...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Toast.makeText(RegisterActivity.this, "recibe respuesta", Toast.LENGTH_SHORT).show();
                        //Descartar el diálogo de progreso
                        loading.hide();
                        if (!s.contains("ok")||s.isEmpty()) {
                                Toast.makeText(RegisterActivity.this, s, Toast.LENGTH_LONG).show();
                        } else {

                            Toast.makeText(RegisterActivity.this, "Registrado correctamente", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(LoginActivity.this, "Login valido", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.putExtra("id_user",s.substring(0,s.length()-2)); //Quitamos el "ok" de la respuesta
                            startActivity(intent); //Iniciar la actividad principal

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Descartar el diálogo de progreso
                        loading.hide();

                        //Showing toast
                        Toast.makeText(RegisterActivity.this, "Error al registrar\n"+volleyError.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Convertir bits a cadena


                //Creación de parámetros
                Map<String, String> params = new HashMap<>();

                //Agregando de parámetros

                params.put("email", emailS);
                params.put("password", pass_digest);
                params.put("username", usuarioS);


                //Parámetros de retorno
                return params;

            }
        };

        //Creación de una cola de solicitudes
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Agregar solicitud a la cola
        requestQueue.add(stringRequest);
    }
}