package com.example.cityreport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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

    public void onClick(View view)
    {
        String url;
        Intent i = new Intent(Intent.ACTION_VIEW);
        switch (view.getId()){
            case R.id.boton_web:
                url = "https://www.cityreport.ga";
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.boton_github:
                url = "https://github.com/CityReportCUSL";
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.boton_wordpress:
                url = "https://cityreport.news.blog";
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.boton_bug:
                i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "cityreportcusl@outlook.com", null));
                startActivity(i);
                break;
        }

    }
}
