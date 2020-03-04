package com.example.cityreport;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    TextView textoVersion;
    Button botonWeb;
    Button botonGithub;
    Button botonWordpress;
    Button botonBug;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //OBTENER REFERENCIAS DE COMPONENTES
        textoVersion = findViewById(R.id.textoVersion);
        botonWeb = findViewById(R.id.boton_web);
        botonGithub = findViewById(R.id.boton_github);
        botonWordpress = findViewById(R.id.boton_wordpress);
        botonBug = findViewById(R.id.boton_bug);

        textoVersion.setText("CityReport "+BuildConfig.VERSION_NAME); //Poner la version de la app en el texto
        

    }
}
