package com.example.cityreport;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

public class MainActivity extends AppCompatActivity {
    private static final int MULTIPLE_PERMISSION_REQUEST_CODE = 123;
    //DECLARACION DE VARIABLES
    MapView vistaMapa;      //Previsualización del mapa
    ImageView vistaImagen;  //Previsualización de la imagen
    Button btnSelect;       //Botón para seleccionar imagen
    Button btnSubir;        //Botón para subir el reporte
    EditText textoDesc;     //Texto de la descripción del reporte
    boolean primerclick = false; //Verdadero cuando se clique por primera vez la descripción
    private String KEY_IMAGEN = "foto";
    private String KEY_NOMBRE = "nombre";
    private String KEY_LON = "Longitud";
    private String KEY_LAT = "Latitud";
    private String UPLOAD_URL = "http://52.47.189.194/funcionesphp/subirfoto.php";


    private Bitmap bitmap = null;// Variable para guardar la imagen en un bitmap
    private Uri filePath;// variable para guardar el path donde se guarda la foto
    private int PICK_IMAGE_REQUEST = 1;// flag para invocar a la app de cámara


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

        comprobarPermisos();


        //Eliminar el texto cuando clique en el área del texto de la descripción
        textoDesc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    if (!primerclick) {
                        textoDesc.setText("");
                        primerclick = true;
                    }
                }
                return false;

            }

        });

        //Botón para tomar o seleccionar foto
        btnSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showFileChooser();
            }
        });

        //
        btnSubir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textoDesc.getText().toString().equals("")||textoDesc.getText().toString().equals("Describa la incidencia...")) {

                    Toast.makeText(MainActivity.this,"Debe introducir descripción!", Toast.LENGTH_LONG).show();
                }else{
                    uploadImage();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                //Cómo obtener el mapa de bits de la Galería
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Configuración del mapa de bits en ImageView
                vistaImagen.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {



            vistaImagen.setImageURI(photoURI);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

        }
    }

    public void onResume() {
        super.onResume();
        vistaMapa.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        vistaMapa.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }


    String currentPhotoPath;
    ///para crear el archivo de la foto
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 2;
    Uri photoURI;


    ///para tomar la foto con la app de la cámara
    private void tomarfoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
               photoURI = FileProvider.getUriForFile(this, "com.example.cityreport.provider", photoFile);
               takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    ///////////////////////////////////////////////

    //Función para seleccionar archivo de la foto o invocar a la función que crea y toma la foto con la cámara
    private void showFileChooser() { //Menu seleccionar imagen

        final CharSequence[] opciones = {"Tomar Foto", "Seleccionar Imagen", "Cancelar"};
        final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(MainActivity.this);
        alertOpciones.setTitle("Seleccione una Opción");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar Foto")) {
                    tomarfoto();
                } else if (opciones[i].equals("Seleccionar Imagen")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Imagen"), PICK_IMAGE_REQUEST);

                } else {
                    dialogInterface.dismiss();
                }
            }

        });
        alertOpciones.show();


    }


    ///// subir reporte/////////////////////////////////////////
    private void uploadImage() {
        //Mostrar el diálogo de progreso
        if (bitmap == null) {
            UPLOAD_URL = "http://52.47.189.194/funcionesphp/subirNoImage.php"; //funcion servidor para subir a la base de datos sin foto
        } else {
            UPLOAD_URL = "http://52.47.189.194/funcionesphp/subirfoto.php";
        }
        final ProgressDialog loading = ProgressDialog.show(this, "Subiendo...", "Espere por favor...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Descartar el diálogo de progreso
                        loading.hide();

                        if (s.trim().equalsIgnoreCase("ok")) {
                            limpiar(); //se restablecen los campos por defecto
                        }

                        else
                            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();

                        //Mostrando el mensaje de la respuesta

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Descartar el diálogo de progreso
                        loading.hide();

                        //Showing toast
                        Toast.makeText(MainActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Convertir bits a cadena
                if (bitmap != null) {
                    String imagen = getStringImagen(bitmap);

                    //Obtener el nombre de la imagen
                    String nombre = textoDesc.getText().toString().trim();



                    //Creación de parámetros
                    Map<String, String> params = new HashMap<>();

                    //Agregando de parámetros

                    params.put(KEY_IMAGEN, imagen);
                    params.put(KEY_NOMBRE, nombre);

                    params.put(KEY_LAT,mLastLocation.getMyLocation().getLatitude()+"");
                    params.put(KEY_LON,mLastLocation.getMyLocation().getLongitude()+"");



                    //Parámetros de retorno
                    return params;
                } else {

                    //Obtener el nombre de la imagen
                    String nombre = textoDesc.getText().toString().trim();



                    //Creación de parámetros
                    Map<String, String> params = new HashMap<>();

                    //Agregando de parámetros

                    params.put(KEY_NOMBRE, nombre);

                    params.put(KEY_LAT,mLastLocation.getMyLocation().getLatitude()+"");
                    params.put(KEY_LON,mLastLocation.getMyLocation().getLongitude()+"");



                    //Parámetros de retorno
                    return params;
                }
            }
        };

        //Creación de una cola de solicitudes
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Agregar solicitud a la cola
        requestQueue.add(stringRequest);


    }

    public void limpiar()
    {
        LayoutInflater imagenDialogo = LayoutInflater.from(MainActivity.this);
        final View vista = imagenDialogo.inflate(R.layout.okenviado,null);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Reporte enviado correctamente")
                .setView(vista)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                restablecer(); //Muestra ventana de OK y reestablece los campos

                            }})
                .create();

        AlertDialog dialogo = builder.show();
        dialogo.setCanceledOnTouchOutside(false);

        //centrar el boton OK
        final Button positiveButton = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.width = ViewGroup.LayoutParams.MATCH_PARENT;
        positiveButton.setLayoutParams(positiveButtonLL);
    }

    public void restablecer()
    {
        vistaImagen.setImageResource(R.drawable.no_image);
        textoDesc.setText("");
        bitmap=null;
    }
    public String getStringImagen(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

}
