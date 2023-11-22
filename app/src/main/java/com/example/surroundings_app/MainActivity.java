package com.example.surroundings_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    private String[] permissoes = new String[]{

            Manifest.permission.ACCESS_FINE_LOCATION
    };


    private LocationManager locationManager;
    private LocationListener locationListener;


    private Double latitude, longitude;

    // Buscar
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private PlacesAdapter adapter;
    private EditText edtSearch;
    private ProgressBar progressBar;

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

        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.estilo)
            );


        } catch (Resources.NotFoundException e) {
            Log.e("MapStyle", "Arquivo de estilo do mapa não encontrado.");
        }

        //Objeto responsável por gerenciar a localização do usuário
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                LatLng localUsuario = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(localUsuario).title("Meu local"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario, 15));


                String[] listatipos = {"amusement_park","aquarium","art_gallery","bakery","bar","bowling_alley","car_rental",
                "cafe","campground","casino","movie_theater", "museum","night_club","park","restaurant",
                "shopping_mall","spa", "stadium", "tourist_attraction","travel_agency","zoo"};


                /*
                "amusement_park","bowling_alley","movie_theater", --> Laranja

                "aquarium","art_gallery", "museum","tourist_attraction","zoo","park","campground" --> Verde

                "bakery","cafe","restaurant","spa" --> Azul

                "car_rental","travel_agency" --> Amarelo

                "bar","casino","night_club" --> Rose

                "shopping_mall" --> Roxo

                "stadium" --> Ciano
                */

                Float[] listacores = {

                        BitmapDescriptorFactory.HUE_ORANGE, // laranja
                        BitmapDescriptorFactory.HUE_GREEN, // verde
                        BitmapDescriptorFactory.HUE_GREEN, // verde
                        BitmapDescriptorFactory.HUE_BLUE, // azul
                        BitmapDescriptorFactory.HUE_ROSE, //vermelho
                        BitmapDescriptorFactory.HUE_ORANGE, // laranja
                        BitmapDescriptorFactory.HUE_YELLOW, // amarelo
                        BitmapDescriptorFactory.HUE_BLUE, // azul
                        BitmapDescriptorFactory.HUE_GREEN, // verde
                        BitmapDescriptorFactory.HUE_ROSE, //vermelho
                        BitmapDescriptorFactory.HUE_ORANGE, // laranja
                        BitmapDescriptorFactory.HUE_GREEN, // verde
                        BitmapDescriptorFactory.HUE_ROSE, //vermelho
                        BitmapDescriptorFactory.HUE_GREEN, // verde
                        BitmapDescriptorFactory.HUE_BLUE, // azul
                        BitmapDescriptorFactory.HUE_VIOLET, // roxo
                        BitmapDescriptorFactory.HUE_BLUE, // azul
                        BitmapDescriptorFactory.HUE_CYAN, //ciano
                        BitmapDescriptorFactory.HUE_GREEN, // verde
                        BitmapDescriptorFactory.HUE_YELLOW, // amarelo
                        BitmapDescriptorFactory.HUE_GREEN, // verde



                };

/*
                String[] listatipos = {"restaurant"};
                Float[] listacores = {BitmapDescriptorFactory.HUE_BLUE};
                */

                final Handler handler2 = new Handler();
                for (int i = 0; i < listatipos.length; i++) {
                    final int index = i;

                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addWayPoint(listatipos[index], listacores[index], mMap);
                        }
                    }, 0);

                }
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
                    500,
                    locationListener
            );
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {

            //permission denied
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

    private void alertaValidacaoPermissao() {

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


    public void onSwitchBusca(View view) {
        setContentView(R.layout.activity_busca);

        Places.initialize(getApplicationContext(), "AIzaSyATnax_UVJYV5JgrfptiKU7lDGRiidqTqY");
        placesClient = Places.createClient(this);
        sessionToken = AutocompleteSessionToken.newInstance();

        edtSearch = findViewById(R.id.edtSearch);
        progressBar = findViewById(R.id.progressBar);
        ListView listPlaces = findViewById(R.id.listPlaces);

        progressBar.setVisibility(View.GONE);
        adapter = new PlacesAdapter(this);
        listPlaces.setAdapter(adapter);

        edtSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                if (edtSearch.length() > 0) {
                    searchPlaces();
                }
            }
            return false;
        });


    }

    private void searchPlaces(){
        progressBar.setVisibility(View.VISIBLE);


        final FindAutocompletePredictionsRequest newRequest = FindAutocompletePredictionsRequest
                .builder()
                .setSessionToken(sessionToken)
                .setQuery(edtSearch.getText().toString())
                .build();

        placesClient.findAutocompletePredictions(newRequest).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse findAutocompletePredictionsResponse) {
                List<AutocompletePrediction> predictions = findAutocompletePredictionsResponse.getAutocompletePredictions();
                adapter.setPredictions(predictions);
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.e("MainActivity", "Place not found: " + apiException.getStatusCode());
                }
            }
        });
    }
    private static class PlacesAdapter extends BaseAdapter {
        private final List<AutocompletePrediction> predictions = new ArrayList<>();
        private final Context context;

        public PlacesAdapter(Context context) {
            this.context = context;
        }

        public void setPredictions(List<AutocompletePrediction> predictions){
            this.predictions.clear();
            this.predictions.addAll(predictions);
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return predictions.size();
        }

        @Override
        public Object getItem(int i) {
            return predictions.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = LayoutInflater.from(context).inflate(R.layout.list_item_place, viewGroup, false);
            TextView txtShortAddress = v.findViewById(R.id.txtShortAddress);
            TextView txtLongAddress = v.findViewById(R.id.txtLongAddress);

            txtShortAddress.setText(predictions.get(i).getPrimaryText(null));
            txtLongAddress.setText(predictions.get(i).getSecondaryText(null));
            return v;
        }
    }


    public void onSwitchPrincipal(View view) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setContentView(R.layout.activity_maps);


    }


    @SuppressLint("Wakelock")
    public void addWayPoint(String tipo, Float cor, GoogleMap mMap) {


        final Double[] latlugar = new Double[1];
        final Double[] longlugar = new Double[1];
        final Double[] rating = new Double[1];
        final String[] name = new String[1];
        final int[] jatemlat = {0};
        final int[] jatemlong = {0};
        final int[] jatemnome = {0};
        final int[] jatemrating = {0};
        final int[] primeiro = {1};
        String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyATnax_UVJYV5JgrfptiKU7lDGRiidqTqY"
                + "&location=" + latitude + "," + longitude + "&radius=5000&type=" + tipo;
        //Log.d("URL", urlString);
        Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;


                    while ((line = reader.readLine()) != null) {
                        if (line.contains("\"lat\" :") && jatemlat[0] == 0) {
                            String regex = "\"lat\" : (-?\\d+\\.\\d+)";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(line);

                            if (matcher.find()) {
                                String numberString = matcher.group(1);
                                latlugar[0] = Double.parseDouble(numberString);
                                jatemlat[0] = 1;
                            }

                        } else if (line.contains("\"lng\" :") && jatemlong[0] == 0) {
                            String regex = "\"lng\" : (-?\\d+\\.\\d+)";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(line);

                            if (matcher.find()) {
                                String numberString = matcher.group(1);
                                longlugar[0] = Double.parseDouble(numberString);
                                jatemlong[0] = 1;
                            }
                        } else if (line.contains("\"name\" :") && jatemnome[0] == 0) {
                            name[0] = line.substring(line.indexOf(":") + 1).trim();
                            jatemnome[0] = 1;

                        } else if (line.contains("\"rating\" :") && jatemrating[0] == 0) {
                            String regex = "\"rating\" : ([0-5](\\.\\d{1,2})?)";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(line);

                            if (matcher.find()) {
                                String numberString = matcher.group(1);
                                rating[0] = Double.parseDouble(numberString);
                                jatemrating[0] = 1;
                            }

                        } else if (line.contains("\"geometry\" :")) {

                            if (primeiro[0] == 1) {
                                primeiro[0] = 0;
                            } else {


                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Atualize a interface do usuário aqui
                                        mMap.addMarker(new MarkerOptions().position(new LatLng(latlugar[0], longlugar[0])).title(name[0]).icon(BitmapDescriptorFactory.defaultMarker(cor)));

                                    }
                                });

                                jatemnome[0] = 0;
                                jatemlong[0] = 0;
                                jatemlat[0] = 0;
                                jatemrating[0] = 0;
                            }
                        }

                    }


                    reader.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}