package com.example.surroundings_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };


    private Double latlugar;
    private Double longlugar;
    private String name;
    private LocationManager locationManager;
    private LocationListener locationListener;
    int jatemlat = 0;
    int jatemlong = 0;
    int jatemnome = 0;

    int jatemrating = 0;



    public Double latitude, longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Objeto responsável por gerenciar a localização do usuário
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Localizacao", "onLocationChanged: " + location.toString());

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                mMap.clear();
                LatLng localUsuario = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(localUsuario).title("Meu local"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario, 15));


                Log.d("LATLNG", "Latitude " + latitude + " Longitude " + longitude);
                String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyATnax_UVJYV5JgrfptiKU7lDGRiidqTqY"
                        + "&location=" + latitude + "," + longitude + "&radius=5000&type=restaurant";
                Log.d("URL", urlString);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Coloque sua operação de rede ou outra tarefa demorada aqui

                        // Exemplo de chamada de rede
                        try {
                            URL url = new URL(urlString);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");

                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;


                            int rating = 0;
                            while ((line = reader.readLine()) != null) {
                                if(line.contains("\"lat: \"") && jatemlat == 0 ){
                                    String regex = "lat: (-?\\d+\\.\\d+),";
                                    Pattern pattern = Pattern.compile(regex);
                                    Matcher matcher = pattern.matcher(line);

                                    String numberString = matcher.group(1);

                                    latlugar = Double.parseDouble(numberString);
                                    jatemlat =1;

                                }else if(line.contains("\"lng: \"") && jatemlong == 0) {
                                    String regex = "lng: (-?\\d+\\.\\d+),";
                                    Pattern pattern = Pattern.compile(regex);
                                    Matcher matcher = pattern.matcher(line);

                                    String numberString = matcher.group(1);

                                    longlugar = Double.parseDouble(numberString);
                                    jatemlong = 1;
                                }else if(line.contains("\"name: \"") && jatemnome == 0){
                                    name = line.substring(line.indexOf(":") + 1).trim();
                                    jatemnome = 1;

                                }
                                else if(line.contains("\"geometry: \"")){
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(latlugar, longlugar)).title(name).icon(getMarkerIcon("#FFFF00")));

                                    jatemnome = 0;
                                    jatemlong = 0;
                                    jatemlat = 0;
                                    jatemrating = 0;
                                }
                            }


                            reader.close();

                            // Processar a resposta da rede

                            // Você não deve atualizar a interface do usuário diretamente aqui, use um Handler se for necessário
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }



            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        /*
         * 1) Provedor da localização
         * 2) Tempo mínimo entre atualizacões de localização (milesegundos)
         * 3) Distancia mínima entre atualizacões de localização (metros)
         * 4) Location listener (para recebermos as atualizações)
         * */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        }

    }

    private BitmapDescriptor getMarkerIcon(String color) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(color));
        Bitmap base = Bitmap.createBitmap(50, 70, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(base);
        canvas.drawRect(0, 0, 50, 70, paint);
        return BitmapDescriptorFactory.fromBitmap(base);
    }






@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {

            //permission denied (negada)
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                //Alerta
                alertaValidacaoPermissao();
            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {
                //Recuperar localizacao do usuario

                /*
                 * 1) Provedor da localização
                 * 2) Tempo mínimo entre atualizacões de localização (milesegundos)
                 * 3) Distancia mínima entre atualizacões de localização (metros)
                 * 4) Location listener (para recebermos as atualizações)
                 * */
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            locationListener
                    );
                }

            }
        }

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    public void onSwitchBusca(View view){
        setContentView(R.layout.activity_busca);
    }


    public void onSwitchPrincipal(View view){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    }
}
