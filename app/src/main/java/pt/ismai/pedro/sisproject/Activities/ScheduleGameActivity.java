package pt.ismai.pedro.sisproject.Activities;

import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import net.steamcrafted.lineartimepicker.dialog.LinearDatePickerDialog;
import net.steamcrafted.lineartimepicker.dialog.LinearTimePickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import pt.ismai.pedro.sisproject.Models.Adapter;
import pt.ismai.pedro.sisproject.Models.Game;
import pt.ismai.pedro.sisproject.Models.TypeOfGame;
import pt.ismai.pedro.sisproject.Models.User;
import pt.ismai.pedro.sisproject.R;

public class ScheduleGameActivity extends AppCompatActivity {

    TextView input_name,date_text,time_text;
    Button saveGame;
    ImageView date, hour;
    LinearDatePickerDialog dialog;
    LinearTimePickerDialog timePickerDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private String gameDate;
    private String gameHour;
    private GeoPoint geoPoint;
    private Game game;
    private String userID;
    User captain;
    private int valueFortypeOfGame;

    ViewPager viewPager;
    Adapter adapter;
    List<TypeOfGame> typeOfGames;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
    AutocompleteSupportFragment placesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_game);

        //Set action bar e title
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        input_name = findViewById(R.id.input_name);
        date = findViewById(R.id.date);
        hour = findViewById(R.id.hour);
        saveGame = findViewById(R.id.saveGame);
        date_text = findViewById(R.id.date_text);
        time_text = findViewById(R.id.time_text);

        //New ArrayList with Type of Games Class values
        typeOfGames = new ArrayList<>();
        typeOfGames.add(new TypeOfGame(R.drawable.football_img,"Football"));
        typeOfGames.add(new TypeOfGame(R.drawable.basketball,"Basketball"));
        typeOfGames.add(new TypeOfGame(R.drawable.tennis,"Tennis"));
        typeOfGames.add(new TypeOfGame(R.drawable.running,"Running"));
        typeOfGames.add(new TypeOfGame(R.drawable.golf,"Golf"));
        typeOfGames.add(new TypeOfGame(R.drawable.padle,"Padle"));

        // Creating a new adapter with the Type Of Games array list
        adapter = new Adapter(typeOfGames,this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130,0,130,0);
        //Everytime we scroll the type of game, we want to change the background color of the view pager
        colors = new Integer[]{
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color3),
                getResources().getColor(R.color.color4),
                getResources().getColor(R.color.color5),
                getResources().getColor(R.color.color6),
        };

        //This page listener is to achieve the background color on type of game scroll
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                if ( i < (adapter.getCount() - 1) && i < (colors.length - 1)){
                    viewPager
                            .setBackgroundColor((Integer) argbEvaluator
                            .evaluate(v,
                                    colors[i],
                                    colors[i] + 1));
                }else{
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int i) {
                //We need to save the (int) value 'cause later on we use this to load a image marker for each game
                valueFortypeOfGame = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        //Linear picker library to date selection
        dialog = LinearDatePickerDialog.Builder.with(this)
                .setYear(2019)
                .setDialogBackgroundColor(getResources().getColor(R.color.primary_dark))
                .setPickerBackgroundColor(getResources().getColor(R.color.color1))
                .setButtonCallback(new LinearDatePickerDialog.ButtonCallback() {
                    @Override
                    public void onPositive(DialogInterface dialog, int year, int month, int day) {
                        gameDate = day + "-" + month + "-" + year;
                        // change the text with selected date
                        date_text.setText(gameDate);
                    }
                    @Override
                    public void onNegative(DialogInterface dialog) {
                    }
                })
                .build();

        //Linear picker library to time selection
        timePickerDialog = LinearTimePickerDialog.Builder.with(this)
                .setDialogBackgroundColor(getResources().getColor(R.color.primary_dark))
                .setPickerBackgroundColor(getResources().getColor(R.color.color1))
                .setButtonCallback(new LinearTimePickerDialog.ButtonCallback() {

            @SuppressLint("DefaultLocale")
            @Override
            public void onPositive(DialogInterface dialog, int hour, int minutes) {
                gameHour = String.format("%02d:%02d",hour,minutes);
                // change the text with selected time
                time_text.setText(gameHour);
            }

            @Override
            public void onNegative(DialogInterface dialog) {
            }
        }).build();

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

            }
        });
        hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show();
            }
        });

        loadUserInfo();
        setupPlaceAutoComplete();

        saveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGame();
            }
        });

    }

    private void loadUserInfo() {
        //We're getting the authenticated user name and profile photo and setting in the activity
        //For loading the image we use a library call "Glide"
        //Glide is a fast and efficient open source media management and image loading framework for Android
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            String[] names = Objects.requireNonNull(user.getDisplayName()).split(" ");
            String firstName = names[0];
            input_name.setText("Olá " + firstName + " onde vamos jogar :)");
        }
    }

    private void setupPlaceAutoComplete() {

        //Places API fragment for place search
        placesFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);

        //Set the fragment with list of values: Place ID, Place Name and Place Name
        if (placesFragment != null) {
            placesFragment.setPlaceFields(placeFields);
            // When the user clicks the place searched we want to send him to the correct location
            placesFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    //Geocoding is the process of transforming a street address
                    // or other description of a location into a (latitude, longitude) coordinate.
                    Geocoder geocoder = new Geocoder(ScheduleGameActivity.this);
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
                        //This variable will be useful to later save the game location in firebase
                        geoPoint = new GeoPoint(address.getLatitude(),address.getLongitude());
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    toastMessage(status.getStatusMessage());
                }
            });
        }
    }

    private void toastMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void saveGame(){
        //Firebase document reference for user collection with the specific document to the authenticated user
        DocumentReference userRef = mDB
                .collection(getString(R.string.collection_users))
                .document(userID);

        //Check the user inputs for null values
        if (gameDate != null && gameHour != null && geoPoint != null){
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        //Firebase document reference for game collection to save the new game reference
                        DocumentReference gameRefCollection = mDB.collection(getString(R.string.collection_games)).document();
                        //// Create a new game instance and set the details
                        captain = Objects.requireNonNull(task.getResult()).toObject(User.class);
                        game = new Game(gameDate,gameHour,captain,geoPoint,valueFortypeOfGame);
                        game.setGameID(gameRefCollection.getId());
                        game.setCapacity(game.gamePlayers());
                        game.addPlayers(captain);
                        game.setNumberOfPlayers(1);
                        game.setTimestamp(null);

                        //Save the new game into firestore
                        gameRefCollection.set(game).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    toastMessage("Game Saved!");
                                }
                                else{
                                    toastMessage(Objects.requireNonNull(task.getException()).getMessage());
                                }
                            }
                        });
                    }
                    else toastMessage(Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        }else{
            toastMessage("Please check for empty fields: Date, Hour or Place Search");
        }

    }

    private void executeActivity(Class<?> subActivity){
        FirebaseUser user = mAuth.getCurrentUser();
        Intent intent = new Intent(this,subActivity);
        if (user != null) {
            intent.putExtra("objectId", user.getUid());
        }
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        executeActivity(ProfileActivity.class);
        return true;
    }
}
