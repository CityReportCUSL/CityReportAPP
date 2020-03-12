package com.example.cityreport;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {
    private Intent intent;   //Intent que lanza esta actividad
    private String user_id;
    RequestQueue colaSolicitud;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        intent = this.getIntent();
        colaSolicitud = Volley.newRequestQueue(this);
        user_id = intent.getStringExtra("user_id"); //Obtener el id del usuario que envía la activiy Main

        if(!user_id.isEmpty())
        {
            TableLayout tablaReportes = findViewById(R.id.tablaReportes);

           // TableRow[] rows = new TableRow[productsList.length()];
            getReportes(user_id);

        }
        else
        {
            Toast.makeText(ReportsActivity.this,"Error al obtener el usuario!",Toast.LENGTH_LONG);
        }
    }


private void getReportes(String user_id)
{
    String url = "https://cityreport.ga/funcionesphp/getReportes.php?user_id="+user_id;


    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
        new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("reportes");
                    Toast.makeText(ReportsActivity.this,jsonArray.length()+" reportes obtenidos",Toast.LENGTH_SHORT).show();
                    for (int i=0; i<jsonArray.length(); i++) //Por cada reporte del usuario
                    {
                        JSONObject reporte = jsonArray.getJSONObject(i);
                        int id = reporte.getInt("id");
                        String foto = reporte.getString("foto");
                        String descripcion = reporte.getString("descripcion");
                        String estado = reporte.getString("estado");

                        Toast.makeText(ReportsActivity.this,"REPORTE "+id+" - "+descripcion+" - "+ estado,Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ReportsActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
            Toast.makeText(ReportsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
        }
    });

    colaSolicitud.add(request); //Añadimos la solicitud a la cola
    }
}
