package pt.ismai.pedro.sisproject.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.Document;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import pt.ismai.pedro.sisproject.Models.ClusterMarker;
import pt.ismai.pedro.sisproject.Models.Game;
import pt.ismai.pedro.sisproject.Models.PolylineData;
import pt.ismai.pedro.sisproject.Models.User;
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
    CircleImageView profile_photo,profile;
    ImageButton btn_reset;
    TextView username,capacity,playersLeft,date,time;
    Button btn_directions,btn_reserve_spot;
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
    private User userExists;

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
        username = bottomSheetDialogView.findViewById(R.id.username);
        capacity = bottomSheetDialogView.findViewById(R.id.capacity);
        playersLeft = bottomSheetDialogView.findViewById(R.id.playersLeft);
        date = bottomSheetDialogView.findViewById(R.id.date);
        time = bottomSheetDialogView.findViewById(R.id.time);
        btn_directions = bottomSheetDialogView.findViewById(R.id.btn_directions);
        btn_reserve_spot = bottomSheetDialogView.findViewById(R.id.btn_enter_game);
        profile = bottomSheetDialog.findViewById(R.id.profilePhoto_BottomSheet);

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
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
        //We're getting the authenticated user name and profile photo and setting in the activity
        //For loading the image we use a library call "Glide"
        //Glide is a fast and efficient open source media management and image loading framework for Android
        if (user != null){
            if (user.getPhotoUrl() != null){
                Glide.with(this ).load(user.getPhotoUrl().toString()).into(profile_photo);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mLocationPermissionGranted) {
            //Setting the device location if the permission is granted
            getDevicelocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //Getting the map ready with device location
            myMap = googleMap;
            myMap.setMyLocationEnabled(true);
            myMap.getUiSettings().setMyLocationButtonEnabled(false);
            //Calling the places and markers methods
            addMapMarkers();
            initPlaces();
            setupPlaceAutoComplete();
            myMap.setOnInfoWindowClickListener(this);
            myMap.setOnPolylineClickListener(this);
        }
    }

    private void setupPlaceAutoComplete() {

        //Places API fragment for place search
        placesFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        if (placesFragment != null) {
            //Set the fragment with list of values: Place ID, Place Name and Place Name
            placesFragment.setPlaceFields(placeFields);
            placesFragment.setCountry("PRT");
            // When the user clicks the place searched we want to send him to the correct location
            placesFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    //Geocoding is the process of transforming a street address
                    // or other description of a location into a (latitude, longitude) coordinate.
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    //address instructs the Places service to return only geocoding results with a precise address.
                    List<Address> addressList = new ArrayList<>();

                    try {
                        // We want a address from the place ID
                        addressList = geocoder.getFromLocationName(place.getAddress(),1);
                    } catch (IOException e) {
                        toastMessage( e.getMessage());
                    }

                    if (addressList.size() > 0){
                        //Creating an address from the address list first position
                        Address address = addressList.get(0);
                        //Zoom the camera to that specific location
                        moveCamera( new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    toastMessage(status.getStatusMessage());
                }
            });
        }
    }

    private void initPlaces() {
        //Initiating places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_api_key));
        }
        placesClient = Places.createClient(this);
    }

    private void getDevicelocation(){

        //After the permission was granted, we want to get the device location
        //Fused Location is actually a location service which combines GPS location
        //and network location to achieve balance between battery consumption and accuracy.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if (mLocationPermissionGranted){
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            //Getting the current device location
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                //Creating a new Latitude e Longitude object with the current location
                                mLocation = new LatLng(currentLocation.getLatitude(),
                                        currentLocation.getLongitude());
                                //Move the camera to that specific point on the map
                                moveCamera(new LatLng(currentLocation.getLatitude(),
                                        currentLocation.getLongitude()),
                                        DEFAULT_ZOOM,"My Location");
                                //Call game markers
                                addMapMarkers();
                            }
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
        //Simple method to zoom the camera to a specific point on the map
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        if (!title.equals("My Location")){
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            myMap.addMarker(markerOptions);
            hideSoftKeyboard();
        }
    }

    private void initMap(){

        //Initialize the Google Maps fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(MainActivity.this);
        }
        if (mGeoAPIContext == null){
            mGeoAPIContext = new GeoApiContext
                    .Builder().
                    apiKey(getString(R.string.google_directions_api_key))
                    .build();
        }
    }

    private void addPolylinesToMap(final DirectionsResult result){
        //This method is to show the polylines of a specific direction
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Initially we've created a list (mPolylinesData) with all the polylines of a designated direction

                if (mPolylinesData.size() > 0){
                    //Everytime a new result is calculated we want to clear our list and create a new list of polylines
                    for (PolylineData polylineData : mPolylinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>();
                }
                double duration = 9999999;

                //We want to draw a route between two points, so we want to iterate all possible
                // routes to that calculated result
                for(DirectionsRoute route: result.routes){
                    //Get the encoded path of the polylines
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

                    //Add the polylines to the map
                    Polyline polyline = myMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.iron));
                    polyline.setClickable(true);
                    mPolylinesData.add(new PolylineData(polyline,route.legs[0]));

                    //Highlight the polyline with the fastest duration
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

        //Calculate the directions to a specific marker
        //Create a map position(Latitude, Longitude) to the destination marker

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );

        //Call a new directions request to the devices location
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoAPIContext);
        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLocation.latitude, mLocation.longitude
                )
        );

        //Draw the polylines to that destination
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d("ActivityTeste", "OnResult: routes: " + result.routes[0].toString());
                Log.d("ActivityTeste", "OnResult: geocodeWayPoints: " + result.geocodedWaypoints[0].toString());
                //Call the polyline method with the calculated result
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.d("Failed",e.getMessage());
            }
        });
    }

    private void getLocationPermission(){

        //Ask user permission for Coarse and Fine Location
        //The Fine location provides better and accurate locations.
        //The Coarse location provides less accurate locations.
        //The network provider determines the location of the users using cell towers,wifi access points etc.
        //distance between towers and user’s position are considered in the case of cell towers.
        //The GPS provider determines the location of the users using satellites. For this, the GPS coordinates
        // are obtained and used for positioning. The GPS receiver in the smartphone receives the signals from satellites.

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ){

            mLocationPermissionGranted = true;
            initMap();
            addMapMarkers();
        }
        else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getGamesLocation(){

        //Save all the FirebaseFirestore game collections into a list
        CollectionReference gameLocationRef = mDB
                .collection(getString(R.string.collection_games));
        gameLocationRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())){
                        Game game = document.toObject(Game.class);
                        games.add(game);
                    }
                }
            }
        });
    }

    private void addMapMarkers(){

        //This is the method to add the game markers to the map
        //First we've created two classes: Cluster Marker with all
        // the details about that marker (Position (LatLng), Title, Snippet and icon picture)
        //Then a Cluster Renderer to render all the details in the map marker

        //To use the marker clustering utility, you will need to add markers as ClusterItem objects to the ClusterManager.
        // The ClusterManager passes the markers to the Algorithm, which transforms them into a set of clusters.
        // The ClusterRenderer takes care of the rendering, by adding and removing clusters and individual markers.
        // The ClusterRenderer and Algorithm are pluggable and can be customized.

        if(myMap != null){
            //call reset map
            resetMap();
            if (mClusterManager == null){
                //Create a new manager algorithm  to transform the data into a set of clusters
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(),myMap);
            }
            if (mClusterManagerRenderer == null){
                //Create a new Renderer to render the details
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this,
                        myMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            //We want to add all the available game into the map, so we need to iterate the game list
            for (Game game : games){
                try{
                    String snippet = "";
                    int avatar = R.drawable.football_mini;

                    //We have a type of game variable in our game collection
                    //That variable comes handy to check which image and game name we need to render
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
                    //Now we add those details to our Cluster Marker
                    ClusterMarker myNewClusterMarker = new ClusterMarker(
                            new LatLng(game.getGeoPoint().getLatitude(),game.getGeoPoint().getLongitude()),
                            game.getCaptain().getUsername(),
                            snippet,
                            avatar
                    );

                    //and order the manager to transform that into a marker
                    mClusterManager.addItem(myNewClusterMarker);
                    //populate the markers list
                    mClusterMarkers.add(myNewClusterMarker);

                }catch (NullPointerException e){
                    toastMessage("Something did go wrong!");
                }
            }
            //Finally we cluster our marker
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

        //This is a builder that is able to create a minimum bound based on a set of LatLng points.
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        //Iterate the list of routes
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        //Zoom map to the route
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
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    //If location is granted initialize the map and set the game markers
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
        if (user != null) {
            intent.putExtra("userId", user.getUid());
        }
        startActivity(intent);

    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        //When the user clicks the info window marker we want to call our bottom sheet fragment with the game detaisl

        //We create a new LatLng with the game destination
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );

        //Iterate the game list
        for (Game game : games){
            //Retrieve the list LatLng which is equal to the Firebase Game GeoPoint
            if (game.getGeoPoint().getLatitude() == destination.lat
                    && game.getGeoPoint().getLongitude() == destination.lng){

                //Variable to see how many players are left to fill the game
                int playersLeftToGame = game.getCapacity() - game.getNumberOfPlayers();

                //Set the correct game information to the bottom sheet
                username.setText("Game Captain: " +  game.getCaptain().getUsername());
                capacity.setText(String.valueOf(game.getCapacity()) );
                playersLeft.setText(String.valueOf(playersLeftToGame));
                date.setText(game.getGameDate());
                time.setText(game.getHour());
                Glide.with(this ).load(game.getCaptain().getAvatar()).into(profile);

                //If the user clicks to join the game
                btn_reserve_spot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //We're getting the firestore reference to the authenticated user to later add him into the game
                        DocumentReference userRef = mDB.collection(getString(R.string.collection_users)).document(Objects.requireNonNull(mAuth.getUid()));
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    //Get the firestore user results to the User Class
                                    User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                                    //Check if there is room for more players and prevent the captain to enter the game he created
                                    if (game.getNumberOfPlayers() < game.getCapacity()){
                                        if (!game.getCaptain().getUser_id().equals(mAuth.getCurrentUser().getUid())
                                                && seeIfPlayerIsInGame(user)){
                                            //Add the new player to that game list
                                            game.addPlayers(user);

                                            //Update the firestore game reference to that game
                                            //By increasing the number of players and players list
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
                                                }
                                            });
                                        }else{
                                            toastMessage("You want to play two positions at the same time? :P");
                                        }

                                    }
                                    else {
                                        toastMessage("The game is full ");
                                    }
                                }
                            }
                        });

                    }
                });
            }

            //We have a variable to check the selected marker (mSelectedMarker)
            //If the user clicks the directions button, we want to calculate the directions to that selection
            btn_directions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetSelectedMarker();
                    mSelectedMarker = marker;
                    calculateDirections(marker);
                    bottomSheetDialog.hide();
                }
            });

        }
        //Show the bottom sheet
        bottomSheetDialog.show();

    }

    private boolean seeIfPlayerIsInGame(User user){
        //Method to check if a certain user already accepted a game
        //Prevent duplication
        CollectionReference gameLocationRef = mDB
                .collection(getString(R.string.collection_games));
        Query query = gameLocationRef.whereEqualTo("players",user);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    userExists = user;
                }
            }
        });

        return userExists == null;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        //Method to highlight the selected polyline and the duration
        int index = 0;
        for(PolylineData polylineData: mPolylinesData){
            //Iterate our polyline list
            index++;
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                //Retrieve the correct polyline (the one selected)
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.primary_dark));
                polylineData.getPolyline().setZIndex(1);

                //Create a variable to store the end location of that direction Latlng
                LatLng endLocation  = new LatLng(polylineData.getLeg().endLocation.lat
                        ,polylineData.getLeg().endLocation.lng);
                //Add a marker with duration info
                Marker marker = myMap.addMarker( new MarkerOptions().position(endLocation)
                .title("Trip: 0" + index).snippet("Duration: " + polylineData.getLeg().duration));

                marker.showInfoWindow();
                //Add marker to our trip markers list
                mTripMarkers.add(marker);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.iron));
                polylineData.getPolyline().setZIndex(0);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mLocationPermissionGranted){
            loadUserInfo();
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

}