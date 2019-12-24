package com.example.cityreport;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

public class MainActivity extends AppCompatActivity {
    private static final int MULTIPLE_PERMISSION_REQUEST_CODE = 123;
    //DECLARACION DE VARIABLES
    MapView vistaMapa;      //Previsualización del mapa
    ImageView vistaImagen;  //Previsualización de la imagen
    Button btnSelect;       //Botón para seleccionar imagen
    Button btnSubir;        //Botón para subir el reporte
    EditText textoDesc;     //Texto de la descripción del reporte
    boolean primerclick = false; //Verdadero cuando se rellene por primera vez la descripción
    private MapController controladorMapa;
    private MyLocationNewOverlay mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //esconder teclado


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1000);
        }


        vistaMapa = findViewById(R.id.mapview);
        vistaImagen = findViewById(R.id.imageView);

        btnSelect = findViewById(R.id.botonSelect);
        btnSubir = findViewById(R.id.boton_subir);

        textoDesc = findViewById(R.id.editText);

        //imageView = (ImageView) findViewById(R.id.imageView);

        comprobarPermisos();

        //Eliminar el texto cuando clique en el área del texto de la descripción
        textoDesc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!primerclick) {
                    textoDesc.setText("");
                    primerclick = true;
                }
            }
        });

    }

    private void comprobarPermisos() {
        int internetPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int networkStatePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE);

        int writeExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int coarseLocationPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int fineLocationPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int wifiStatePermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE);

        if (internetPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                networkStatePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                writeExternalStoragePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                wifiStatePermissionCheck == PackageManager.PERMISSION_GRANTED) {

            setupMap();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE},
                    MULTIPLE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean somePermissionWasDenied = false;
                    for (int result : grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            somePermissionWasDenied = true;
                        }
                    }
                    if (somePermissionWasDenied) {
                        Toast.makeText(this, "No se puede cargar el mapa si no otorga los permisos necesarios!", Toast.LENGTH_SHORT).show();
                    } else {
                        setupMap();

                    }
                } else {
                    Toast.makeText(this, "No se puede cargar el mapa si no otorga los permisos necesarios!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    private void setupMap() {
        Toast.makeText(this, "Cargando mapa...", Toast.LENGTH_SHORT).show();
        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.APPLICATION_ID);
        provider.setOsmdroidBasePath(getStorage());
        provider.setOsmdroidTileCache(getStorage());


        vistaMapa.setBuiltInZoomControls(true);
        vistaMapa.setClickable(true);
        vistaMapa.setMultiTouchControls(true);


        controladorMapa = (MapController) vistaMapa.getController();
        //vistaMapa.setTileSource(TileSourceFactory.MAPNIK);
        vistaMapa.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        controladorMapa.setZoom(20);


        GpsMyLocationProvider provider2 = new GpsMyLocationProvider(getApplicationContext());
        provider2.addLocationSource(LocationManager.NETWORK_PROVIDER); //network funciona mejor que gps
        mLastLocation = new MyLocationNewOverlay(provider2, vistaMapa);
        mLastLocation.enableMyLocation();
        mLastLocation.enableFollowLocation();
        vistaMapa.getOverlays().add(mLastLocation);


        vistaMapa.getController().animateTo(mLastLocation.getMyLocation());//centrar el mapa en mi localizacion

        //Retrasar zoom para que cargue mas rapido el mapa
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                controladorMapa.setZoom(18);
            }
        }, 2000);


        handler.postDelayed(new Runnable() {
            public void run() {
                controladorMapa.setZoom(20);
            }
        }, 4000);


    }

    public void onResume() {
        super.onResume();
        vistaMapa.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        vistaMapa.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

}
