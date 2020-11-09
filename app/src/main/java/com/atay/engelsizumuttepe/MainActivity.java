package com.atay.engelsizumuttepe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private static MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private static final double KUTLE = 80, HIZ = 2;

    static LatLng baslangic, bitis;
    static Marker baslangicMarker, bitisMarker;
    AutoCompleteTextView edtBaslangic, edtBitis;
    HashMap<String, LatLng> adresler = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baslangic = new LatLng();
        bitis = new LatLng();
        Mapbox.getInstance(this, getString(R.string.mapbox_token));
        setContentView(R.layout.activity_main);
        edtBaslangic = findViewById(R.id.autoBaslangic);
        edtBitis = findViewById(R.id.autoBitis);
        adresleriAyarla();
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        ImageButton locButton = findViewById(R.id.btnLocation);
        locButton.setOnClickListener(v -> {
            @SuppressLint("MissingPermission") Location loc = mapboxMap.getLocationComponent().getLastKnownLocation();
            if (loc != null)
                setBaslangic(new LatLng(loc.getLatitude(), loc.getLongitude()), getString(R.string.konumum));
            else
                Toast.makeText(this, R.string.no_location, Toast.LENGTH_SHORT).show();
        });

        edtBaslangic.setOnFocusChangeListener((v, hasFocus) -> {
            AutoCompleteTextView edt = (AutoCompleteTextView) v;
            if(hasFocus){
                edt.setHint(edt.getText());
                edt.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
                }
            }
            else if(edt.getText().length() == 0)
                edt.setText(edt.getHint());
        });

        edtBitis.setOnFocusChangeListener(edtBaslangic.getOnFocusChangeListener());

        edtBaslangic.setOnItemClickListener((parent, view, position, id) -> {
            String adres = edtBaslangic.getAdapter().getItem(position).toString();
            setBaslangic(adresler.get(adres), adres);
        });

        edtBitis.setOnItemClickListener((parent, view, position, id) -> {
            String adres = edtBitis.getAdapter().getItem(position).toString();
            setBitis(adresler.get(adres), adres);
        });
    }

    private void adresleriAyarla() {
        adresler.put("A Kapısı",new LatLng(40.82467,29.92163));
        adresler.put("B Kapısı",new LatLng(40.82333,29.92530));
        adresler.put("Helikopter Pisti",new LatLng(40.82264,29.91907));
        adresler.put("Köroğlu Eczanesi",new LatLng(40.82311,29.92691));
        adresler.put("Kampüs Büfe",new LatLng(40.82306,29.92709));
        adresler.put("Kampüs Park Kız Öğrenci Yurdu",new LatLng(40.82304,29.92697));
        adresler.put("Şok",new LatLng(40.82158,29.92774));
        adresler.put("Mühendislik Fakültesi B",new LatLng(40.82189,29.92359));
        adresler.put("Eğitim Fakültesi",new LatLng(40.82193,29.92441));
        adresler.put("Hukuk Fakültesi",new LatLng(40.82198,29.92611));
        adresler.put("Fen Edebiyat Fakültesi B Blok",new LatLng(40.82198,29.92611));
        adresler.put("Fen Edebiyat Fakültesi A Blok",new LatLng(40.82060,29.92075));
        adresler.put("Umut Kafe",new LatLng(40.82246,29.92490));
        adresler.put("İktisadi ve İdari Bilimler Fakültesi",new LatLng(40.82237,29.92392));
        adresler.put("İletişim Fakültesi",new LatLng(40.82293,29.92317));
        adresler.put("Tepe Kafe",new LatLng(40.82350,29.92263));
        adresler.put("KOÜ Kütüphane",new LatLng(40.82186,29.92166));
        adresler.put("Mekstar Kafe",new LatLng(40.82149,29.92073));
        adresler.put("Rektörlük",new LatLng(40.82275,29.92219));
        adresler.put("Teknoloji Fakültesi",new LatLng(40.82155,29.92029));
        adresler.put("Sosyal Tesisler, Yemekhane",new LatLng(40.82150,29.92791));
        adresler.put("Genetik Araştırma | KÖGEM | Morfoloji",new LatLng(40.82357,29.91760));
        adresler.put("Ziyaretçi Otoparkı",new LatLng(40.82295,29.91928));
        adresler.put("Umuttepe Camii",new LatLng(40.82389,29.92602));
        adresler.put("Kampüs Mescit",new LatLng(40.81985,29.92250));
        adresler.put("KYK Samiha Ayverdi Kız Yurdu",new LatLng(40.82050,29.92349));
        adresler.put("Spor Bilimleri Fakültesi",new LatLng(40.81685,29.91971));
        adresler.put("Olimpik Yüzme Havuzu",new LatLng(40.81407,29.92107));
        adresler.put("Yabancı Diller Yüksekokulu",new LatLng(40.81267,29.92041));
        adresler.put("Fen Edebiyat Fakültesi Dekanlık",new LatLng(40.82060,29.92075));
        adresler.put("KOÜ Stadyum",new LatLng(40.81426,29.92021));
        adresler.put("Bilgi İşlem Daire Başkanlığı",new LatLng(40.82227,29.92134));
        adresler.put("Sağlık Kültür ve Spor Mediko Sosyal",new LatLng(40.82183,29.92780));
        adresler.put("Sağlık Yüksekokulu",new LatLng(40.82403,29.91714));
        adresler.put("Sağlık Hizmetleri Meslek Yüksekokulu",new LatLng(40.82394,29.91665));
        adresler.put("Tıp Fakültesi",new LatLng(40.82422,29.91842));
        adresler.put("Tıp Fakültesi Hastanesi",new LatLng(40.82436,29.91857));
        adresler.put("Prof. Dr Baki Komsuoğlu Kültür ve Kongre Merkezi",new LatLng(40.82220,29.92124));
        adresler.put("Gazanfer Bilge Spor Tesisleri",new LatLng(40.81601,29.91954));
        adresler.put("Mühendislik Fakültesi A",new LatLng(40.82059,29.92194));
        adresler.put("Mühendislik Fakültesi F",new LatLng(40.82157,29.92225));

        String[] degerler = new String[adresler.size()];
        adresler.keySet().toArray(degerler);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, degerler);

        edtBaslangic.setAdapter(adapter);
        edtBitis.setAdapter(adapter);
    }

    void setBaslangic(LatLng point, String text){
        hideKeyboard();
        baslangic.setLatitude(point.getLatitude());
        baslangic.setLongitude(point.getLongitude());
        if(text != null)
            edtBaslangic.setText(text);
        else
            edtBaslangic.setText(String.format("%s, %s", ((int) (100000 * baslangic.getLatitude())) / 100000.0,
                    ((int) (100000 * baslangic.getLongitude())) / 100000.0));

        if (baslangicMarker == null)
            baslangicMarker = mapboxMap.addMarker(new MarkerOptions().position(point).setTitle("Başlangıç"));
        else
            baslangicMarker.setPosition(point);

        if(bitis.getLatitude() != 0 && bitis.getLongitude() != 0) {
            if (adresler.containsValue(bitis))
                setBitis(bitis, String.valueOf(edtBitis.getText()));
            else
                setBitis(bitis, null);
        }
    }

    void setBitis(LatLng point, String text){
        hideKeyboard();
        if(baslangic.getLatitude() == 0 && baslangic.getLongitude() == 0){
            @SuppressLint("MissingPermission") Location loc = mapboxMap.getLocationComponent().getLastKnownLocation();
            if(loc == null){
                Toast.makeText(this, R.string.set_location, Toast.LENGTH_LONG).show();
                return;
            }
            setBaslangic(new LatLng(loc.getLatitude(),loc.getLongitude()), getString(R.string.konumum));
        }
        bitis.setLatitude(point.getLatitude());
        bitis.setLongitude(point.getLongitude());
        edtBitis.setText(R.string.aranıyor);
        new RouteTask(this, text).execute();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        MainActivity.mapboxMap = mapboxMap;
        enableLocationComponent();
        mapboxMap.setStyleUrl("mapbox://styles/mapbox/satellite-streets-v9");
        Objects.requireNonNull(mapboxMap.getLayer("poi-label")).setProperties(textField("{name}"));
        mapboxMap.setMaxZoomPreference(18);
        mapboxMap.setMinZoomPreference(14);
        mapboxMap.setLatLngBoundsForCameraTarget(new LatLngBounds.Builder()
                .include(new LatLng(40.826776, 29.913583))
                .include(new LatLng(40.811974, 29.930727)).build());

        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(40.821645, 29.923239)).zoom(15).build());

        mapboxMap.addOnMapLongClickListener(point -> setBaslangic(point,null));
        mapboxMap.addOnMapClickListener(point -> setBitis(point, null));
        mapboxMap.addOnCameraMoveStartedListener(r -> hideKeyboard());


    }

    void hideKeyboard(){
        mapView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        }
    }
    @NonNull
    public static JSONObject getJSONObjectFromURL(String urlString, JSONObject body) throws IOException, JSONException {
        HttpURLConnection urlConnection;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        urlConnection.setRequestProperty("Accept","application/json");
        urlConnection.setRequestProperty("Authorization",getString(R.string.openroute_token));
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        Log.i("JSONN", body.toString());
        DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
        //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
        os.writeBytes(body.toString());

        os.flush();
        os.close();

        Log.i("STATUSS", String.valueOf(urlConnection.getResponseCode()));
        Log.i("MSGG" , urlConnection.getResponseMessage());


        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();

        String jsonString = sb.toString();
        Log.i("RSPP" , jsonString);

        urlConnection.disconnect();

        return new JSONObject(jsonString);
    }

    @SuppressLint("MissingPermission")
    void enableLocationComponent(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            locationComponent.activateLocationComponent(this);
            locationComponent.setLocationComponentEnabled(true);

            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private static class RouteTask extends AsyncTask<Void,Void,List<PolylineOptions>>{

        private WeakReference<MainActivity> activityReference;
        private String text;
        private List<LatLng> rota;
        private double guc = 0;

        RouteTask(MainActivity context, String text) {
            activityReference = new WeakReference<>(context);
            this.text = text;
            rota = new ArrayList<>();
        }

        static List<LatLng> decodePolyline(String encoded) {
            /*  Licensed to GraphHopper GmbH under one or more contributor
             *  license agreements.
             *  http://www.apache.org/licenses/LICENSE-2.0
             */
            List<LatLng> poly = new ArrayList<>();
            int index = 0;
            int len = encoded.length();
            int lat = 0, lng = 0, ele = 0;
            while (index < len) {
                // latitude
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int deltaLatitude = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += deltaLatitude;

                // longitute
                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int deltaLongitude = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += deltaLongitude;

                // elevation
                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int deltaElevation = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                ele += deltaElevation;
                poly.add(new LatLng((double) lat / 1e5, (double) lng / 1e5, (double) ele / 100));
            }
            return poly;
        }

        @Override
        protected List<PolylineOptions> doInBackground(Void... args) {
            rota.clear();
            try {
                JSONObject jsonParam = new JSONObject();
                JSONArray coordinates = new JSONArray();
                JSONArray start = new JSONArray();
                start.put(baslangic.getLongitude());
                start.put(baslangic.getLatitude());
                JSONArray end = new JSONArray();
                end.put(bitis.getLongitude());
                end.put(bitis.getLatitude());
                coordinates.put(start);
                coordinates.put(end);
                jsonParam.put("coordinates", coordinates);
                jsonParam.put("preference", "shortest");
                jsonParam.put("instructions", "false");
                jsonParam.put("elevation", "true");
                JSONObject options = new JSONObject();
                options.put("avoid_features", new JSONArray().put("steps"));
                JSONArray avoid = new JSONArray();
                avoid.put(new JSONArray().put(29.923366).put(40.822546));
                avoid.put(new JSONArray().put(29.923451).put(40.822594));
                avoid.put(new JSONArray().put(29.923719).put(40.822383));
                avoid.put(new JSONArray().put(29.923644).put(40.822326));
                avoid.put(new JSONArray().put(29.923366).put(40.822546));
                JSONObject avoidPolygons = new JSONObject().put("type", "MultiPolygon");
                avoidPolygons.put("coordinates", new JSONArray().put(new JSONArray().put(avoid)));
                options.put("avoid_polygons", avoidPolygons);
                jsonParam.put("options", options);
                jsonParam.put("id", "Engelsiz Umuttepe");

                JSONObject response = getJSONObjectFromURL("https://api.openrouteservice.org/v2/directions/foot-walking", jsonParam);
                List<LatLng> polyline = decodePolyline(response.getJSONArray("routes").getJSONObject(0).getString("geometry"));
                Log.i("RSPP", polyline.toString());
                for (int i = 0; i < polyline.size(); i++) {
                    rota.add(polyline.get(i));
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            List<PolylineOptions> polylines = new ArrayList<>();
            for (int i = 0; i < rota.size()-1; i++) {
                double h = rota.get(i+1).getAltitude() - rota.get(i).getAltitude();
                double mesafe = rota.get(i).distanceTo(rota.get(i+1));


                if(h <= 0){
                    h = 0;
                    polylines.add(new PolylineOptions().add(rota.get(i), rota.get(i+1)).width(2).color(Color.rgb(0,255,0)));
                }
                else
                    polylines.add(new PolylineOptions().add(rota.get(i), rota.get(i+1)).width(2).color(Color.rgb(255,255-(int)(3*255*h/mesafe),0)));

                guc += (KUTLE*HIZ*(HIZ*HIZ + 20*h))/(2*mesafe);


            }
            guc = ((int)(guc*100))/100.0;
            return polylines;
        }

        @Override
        protected void onPostExecute(List<PolylineOptions> polyList) {
            super.onPostExecute(polyList);

            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            AutoCompleteTextView edtBitis = activity.findViewById(R.id.autoBitis);

            for (Polyline p : mapboxMap.getPolylines())
                mapboxMap.removePolyline(p);
            if(bitisMarker != null)
                bitisMarker.setPosition(new LatLng(0,0));

            if(polyList.size() > 0){
                mapboxMap.addPolylines(polyList);
                if (bitisMarker == null)
                    bitisMarker = mapboxMap.addMarker(new MarkerOptions().position(rota.get(rota.size()-1)).setTitle(activity.getString(R.string.bitiş)));
                else
                    bitisMarker.setPosition(rota.get(rota.size()-1));
                mapboxMap.selectMarker(bitisMarker);
                if(text != null)
                    edtBitis.setText(text);
                else
                    edtBitis.setText(String.format("%s, %s", ((int) (bitis.getLatitude() * 100000)) / 100000.0,
                            ((int) (bitis.getLongitude() * 100000)) / 100000.0));
                Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.power) + ": " + guc + " watt", Toast.LENGTH_LONG).show();
            }
            else{
                edtBitis.setText("");
                Toast.makeText(activity.getApplicationContext(), R.string.rota_yok, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.itemDown){
            hideKeyboard();
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar.getVisibility() == View.GONE)
                toolbar.setVisibility(View.VISIBLE);
            else
                toolbar.setVisibility(View.GONE);
        }
        else if(item.getItemId() == R.id.itemAbout){
            hideKeyboard();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.hakkinda);
            String mesaj = getString(R.string.prepared)+":\n160201106\tRemzi Atay\n120202050\tSinan Can Şahin\n"+getString(R.string.thx)+
                    ":\nYard. Doç. Dr. Alev Mutlu\nYard. Doç. Dr. Cankut Dağdal İnce";

            builder.setMessage(mesaj)
                    .setCancelable(false)
                    .setNegativeButton(R.string.Tamam, (dialog, id) -> dialog.cancel());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Rota tavsiyesi sunabilmemiz için konum bilgisi gereklidir.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
        } else {
            Toast.makeText(this, "İzin alınamadı", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
