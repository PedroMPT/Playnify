package pt.ismai.pedro.sisproject.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pt.ismai.pedro.sisproject.Models.ClusterMarker;
import pt.ismai.pedro.sisproject.Models.Football;
import pt.ismai.pedro.sisproject.Models.Game;
import pt.ismai.pedro.sisproject.R;
import pt.ismai.pedro.sisproject.util.MyClusterManagerRenderer;

import static pt.ismai.pedro.sisproject.Constants.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private boolean mLocationPermissionGranted = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 14.5f;
    private GoogleMap myMap;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
    AutocompleteSupportFragment placesFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    FloatingActionButton locationButton;
    CircleImageView profile_photo;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private ArrayList<Football> games = new ArrayList<>();
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationButton = findViewById(R.id.ic_gps);
        profile_photo = findViewById(R.id.profilePhoto);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        getLocationPermission();
        loadUserInfo();
        getGamesLocation();

        profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                executeActivity(ProfileActivity.class);
            }
        });
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){
            if (user.getPhotoUrl() != null){
                Glide.with(this ).load(user.getPhotoUrl().toString()).into(profile_photo);
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        if (mLocationPermissionGranted) {
            getDevicelocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myMap.setMyLocationEnabled(true);
            myMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();

            initPlaces();
            setupPlaceAutoComplete();
            addMapMarkers();
        }
    }

    private void setupPlaceAutoComplete() {

        placesFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        placesFragment.setPlaceFields(placeFields);
        placesFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Geocoder geocoder = new Geocoder(MainActivity.this);
                List<Address> addressList = new ArrayList<>();

                try {
                    addressList = geocoder.getFromLocationName(place.getName(),1);
                } catch (IOException e) {
                    toastMessage( e.getMessage());
                }

                if (addressList.size() > 0){
                    Address address = addressList.get(0);
                    moveCamera( new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                toastMessage(status.getStatusMessage());
            }
        });
    }

    private void initPlaces() {

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_api_key));
        }
        placesClient = Places.createClient(this);

    }
    private void init(){

        setupPlaceAutoComplete();

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDevicelocation();
            }
        });
        hideSoftKeyboard();
    }

    private void getDevicelocation(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if (mLocationPermissionGranted){
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()),DEFAULT_ZOOM,"My Location");
                        }else{
                            toastMessage("Unable to get current location");
                        }
                    }
                });
            }

        }catch (SecurityException e){
            toastMessage(e.getMessage());
        }

    }

    private void moveCamera(LatLng latLng, float zoom, String title){

        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        if (!title.equals("My Location")){
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            myMap.addMarker(markerOptions);
        }
        hideSoftKeyboard();
    }

    private void initMap(){

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);
    }

    private void getLocationPermission(){

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        // Improve this if Statement
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();

            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
        else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getGamesLocation(){

        final CollectionReference gameLocationRef = mDB
                .collection(getString(R.string.collection_games));
        gameLocationRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){

                    for(DocumentSnapshot document:task.getResult()){
                        Football football = document.toObject(Football.class);
                        games.add(football);
                    }
                }
            }
        });

    }

    private void addMapMarkers(){

        if(myMap != null){
            if (mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(),myMap);
            }
            if (mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getApplicationContext(),
                        myMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for (Game game : games){

                try{
                    String snippet = "";
                    if (game.getCaptain().getUser_id().equals(userID)){

                        snippet = "This is you";
                    }
                    else{
                        snippet = "Determine route to " + game.getCaptain().getUsername() + "?";
                    }

                    int avatar = R.drawable.football_mini;
                    try{
                        avatar = Integer.parseInt(game.getCaptain().getAvatar());
                    }catch (NumberFormatException e){
                        toastMessage("O Avatar não deu");
                    }
                    ClusterMarker myNewClusterMarker = new ClusterMarker(
                            new LatLng(game.getGeoPoint().getLatitude(),game.getGeoPoint().getLongitude()),
                            game.getCaptain().getUsername(),
                            snippet,
                            avatar
                    );
                    mClusterManager.addItem(myNewClusterMarker);
                    mClusterMarkers.add(myNewClusterMarker);

                }catch (NullPointerException e){
                    toastMessage("Tudo falhou");
                }
            }
            mClusterManager.cluster();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                //Loop through all possible results??
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;

                    //initialize our map
                    initMap();
                }
            }
        }
    }
    private void toastMessage(String message) {

        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void hideSoftKeyboard(){

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void executeActivity(Class<?> subActivity){
        FirebaseUser user = mAuth.getCurrentUser();
        Intent intent = new Intent(this,subActivity);
        intent.putExtra("userId", user.getUid());
        startActivity(intent);

    }

}
