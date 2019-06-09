package pt.ismai.pedro.sisproject.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.DocumentCollections;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pt.ismai.pedro.sisproject.Models.ClusterMarker;
import pt.ismai.pedro.sisproject.Models.Football;
import pt.ismai.pedro.sisproject.Models.Game;
import pt.ismai.pedro.sisproject.Models.GameLocation;
import pt.ismai.pedro.sisproject.Models.PolylineData;
import pt.ismai.pedro.sisproject.Models.User;
import pt.ismai.pedro.sisproject.Models.UserSingleton;
import pt.ismai.pedro.sisproject.R;
import pt.ismai.pedro.sisproject.util.MyClusterManagerRenderer;

import static pt.ismai.pedro.sisproject.Constants.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnPolylineClickListener{


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
    ImageButton btn_reset;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private ArrayList<Game> games = new ArrayList<>();
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private GeoApiContext mGeoAPIContext = null;
    private Marker mSelectedMarker = null;
    String userID;
    private LatLng mLocation;
    private BottomSheetDialog bottomSheetDialog;
    View bottomSheetDialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialogView = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet,null);
        bottomSheetDialog.setContentView(bottomSheetDialogView);


        locationButton = findViewById(R.id.ic_gps);
        profile_photo = findViewById(R.id.profilePhoto);
        btn_reset = findViewById(R.id.btn_reset_map);
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

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDevicelocation();
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMapMarkers();
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
        if (mLocationPermissionGranted) {
            getDevicelocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myMap = googleMap;
            myMap.setMyLocationEnabled(true);
            myMap.getUiSettings().setMyLocationButtonEnabled(false);
            initPlaces();
            setupPlaceAutoComplete();
            myMap.setOnInfoWindowClickListener(this);
            myMap.setOnPolylineClickListener(this);
        }
    }

    private void setupPlaceAutoComplete() {

        placesFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        placesFragment.setPlaceFields(placeFields);
        placesFragment.setCountry("pt");
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
                            mLocation = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()),DEFAULT_ZOOM,"My Location");
                            addMapMarkers();
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

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        if (!title.equals("My Location")){
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            myMap.addMarker(markerOptions);
        }
        hideSoftKeyboard();
    }

    private void initMap(){

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);

        if (mGeoAPIContext == null){

            mGeoAPIContext = new GeoApiContext
                    .Builder().
                    apiKey(getString(R.string.google_directions_api_key))
                    .build();
        }
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if (mPolylinesData.size() > 0){
                    for (PolylineData polylineData : mPolylinesData){

                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>();
                }
                double duration = 9999999;
                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding
                            .decode(route.overviewPolyline
                                    .getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = myMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_dark));
                    polyline.setClickable(true);
                    mPolylinesData.add(new PolylineData(polyline,route.legs[0]));

                    double tempduration = route.legs[0].duration.inSeconds;

                    if (tempduration < duration){
                        duration = tempduration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                }
            }
        });
    }

    private void calculateDirections(Marker marker){

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoAPIContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLocation.latitude, mLocation.longitude
                )
        );
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {

                Log.d("ActivityTeste", "OnResult: routes: " + result.routes[0].toString());
                Log.d("ActivityTeste", "OnResult: geocodeWayPoints: " + result.geocodedWaypoints[0].toString());
               addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {

                Log.d("Failed",e.getMessage());

            }
        });
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
                addMapMarkers();

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

        CollectionReference gameLocationRef = mDB
                .collection(getString(R.string.collection_games));
        gameLocationRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){

                    for(DocumentSnapshot document:task.getResult()){
                        Game game = document.toObject(Football.class);
                        games.add(game);
                    }
                }
            }
        });
    }

    private void addMapMarkers(){

        if(myMap != null){

            resetMap();
            if (mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(),myMap);
            }
            if (mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this,
                        myMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for (Game game : games){
                try{
                    String snippet = "";
                    int avatar = R.drawable.football_mini;

                    switch (game.getTypeOfGame()) {
                        case 0:
                            avatar = R.drawable.football_icon;
                            snippet = "Football Game";
                            break;
                        case 1:
                            avatar = R.drawable.basketball_icon;
                            snippet = "Basketball Game";
                            break;
                        case 2:
                            avatar = R.drawable.tennis_icon;
                            snippet = "Tennis Game";
                            break;
                        case 3:
                            avatar = R.drawable.running_icon;
                            snippet = "Running Marathon";
                            break;
                        case 4:
                            avatar = R.drawable.golf_icon;
                            snippet = "Golf Game";
                            break;
                        case 5:
                            avatar = R.drawable.padle_icon;
                            snippet = "Padle Game";
                            break;
                        default:
                            break;
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
                    toastMessage("Something did go wrong!");
                }
            }
            mClusterManager.cluster();
        }
    }

    private void removeTripMarkers(){

        for (Marker marker : mTripMarkers){

            marker.remove();
        }
    }

    private void resetSelectedMarker(){
        if (mSelectedMarker != null){
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }

    private void resetMap(){
        if(myMap != null) {
            myMap.clear();

            if(mClusterManager != null){
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }

            if(mPolylinesData.size() > 0){
                mPolylinesData.clear();
                mPolylinesData = new ArrayList<>();
            }
        }
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (myMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        myMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
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

                    initMap();
                    addMapMarkers();
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

    @Override
    protected void onResume() {
        super.onResume();
        toastMessage("OnResume");
        if(mLocationPermissionGranted){
            getGamesLocation();
            addMapMarkers();
        }
        else{
            getLocationPermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {

        TextView username = bottomSheetDialogView.findViewById(R.id.username);
        TextView capacity = bottomSheetDialogView.findViewById(R.id.capacity);
        TextView playersLeft = bottomSheetDialogView.findViewById(R.id.playersLeft);
        TextView date = bottomSheetDialogView.findViewById(R.id.date);
        TextView time = bottomSheetDialogView.findViewById(R.id.time);
        Button btn_directions = bottomSheetDialogView.findViewById(R.id.btn_directions);
        Button btn_reserve_spot = bottomSheetDialogView.findViewById(R.id.btn_enter_game);
        CircleImageView profile = bottomSheetDialog.findViewById(R.id.profilePhoto);


        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        for (Game game : games){

            if (game.getGeoPoint().getLatitude() == destination.lat
                    && game.getGeoPoint().getLongitude() == destination.lng){

                int playersLeftToGame = game.getCapacity() - game.getNumberOfPlayers();

                username.setText("Game Captain: " +  game.getCaptain().getUsername());
                capacity.setText(String.valueOf(game.getCapacity()) );
                playersLeft.setText(String.valueOf(playersLeftToGame));
                date.setText(game.getGameDate());
                time.setText(game.getHour());

                FirebaseUser user = mAuth.getCurrentUser();

                if (game.getCaptain().getUser_id().equals(user.getUid())){

                    Glide.with(this ).load(user.getPhotoUrl().toString()).into(profile);
                }
            }

            btn_directions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetSelectedMarker();
                    mSelectedMarker = marker;
                    calculateDirections(marker);
                    bottomSheetDialog.hide();

                }
            });


            btn_reserve_spot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DocumentReference userRef = mDB.collection(getString(R.string.collection_users)).document(mAuth.getUid());

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()){
                                User user = task.getResult().toObject(User.class);

                                if (game.getNumberOfPlayers() < game.getCapacity()){
                                    game.addPlayers(user);


                                    DocumentReference gameRef = mDB.
                                            collection(getString(R.string.collection_games)).document(game.getGameID());
                                    gameRef.update("numberOfPlayers",game.getNumberOfPlayers(),
                                            "players",
                                            game.getPlayers()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                toastMessage("Welcome to the game :)");
                                            }

                                            else{

                                                toastMessage("Sorry, game is full :(");
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });

                }
            });
        }
        bottomSheetDialog.show();

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        int index = 0;
        for(PolylineData polylineData: mPolylinesData){
            index++;
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.primary_dark));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation  = new LatLng(polylineData.getLeg().endLocation.lat
                        ,polylineData.getLeg().endLocation.lng);
                Marker marker = myMap.addMarker( new MarkerOptions().position(endLocation)
                .title("Trip: 0" + index).snippet("Duration: " + polylineData.getLeg().duration));

                marker.showInfoWindow();

                mTripMarkers.add(marker);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.iron));
                polylineData.getPolyline().setZIndex(0);
            }
        }

    }

}