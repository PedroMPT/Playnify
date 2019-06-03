package pt.ismai.pedro.sisproject.Activities;

import android.animation.ArgbEvaluator;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.CollectionReference;
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

import pt.ismai.pedro.sisproject.Models.Adapter;
import pt.ismai.pedro.sisproject.Models.Football;
import pt.ismai.pedro.sisproject.Models.TypeOfGame;
import pt.ismai.pedro.sisproject.Models.User;
import pt.ismai.pedro.sisproject.R;

public class ScheduleGameActivity extends AppCompatActivity {

    TextView input_name;
    Button saveGame;
    ImageView date, hour;
    LinearDatePickerDialog dialog;
    LinearTimePickerDialog timePickerDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private String gameDate;
    private String gameHour;
    private GeoPoint geoPoint;
    private Football game;
    private String userID;
    User captain;

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

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        input_name = findViewById(R.id.input_name);
        date = findViewById(R.id.date);
        hour = findViewById(R.id.hour);
        saveGame = findViewById(R.id.saveGame);

        typeOfGames = new ArrayList<>();
        typeOfGames.add(new TypeOfGame(R.drawable.football_img,"Football"));
        typeOfGames.add(new TypeOfGame(R.drawable.basketball,"Basketball"));
        typeOfGames.add(new TypeOfGame(R.drawable.tennis,"Tennis"));
        typeOfGames.add(new TypeOfGame(R.drawable.running,"Running"));

        adapter = new Adapter(typeOfGames,this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130,0,130,0);
        Integer[] colors_temp = {
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color3),
                getResources().getColor(R.color.color4),
        };

        colors = colors_temp;

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

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });


        dialog = LinearDatePickerDialog.Builder.with(this)
                .setYear(2019)
                .setButtonCallback(new LinearDatePickerDialog.ButtonCallback() {
                    @Override
                    public void onPositive(DialogInterface dialog, int year, int month, int day) {
                        toastMessage("" + year + "" + month + "" + day);
                        gameDate = day + "-" + month + "-" + year;
                    }

                    @Override
                    public void onNegative(DialogInterface dialog) {

                    }
                })
                .build();

        timePickerDialog = LinearTimePickerDialog.Builder.with(this).setButtonCallback(new LinearTimePickerDialog.ButtonCallback() {
            @Override
            public void onPositive(DialogInterface dialog, int hour, int minutes) {

                gameHour = hour + ":" + minutes;
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
                getUserDetails();

            }
        });

    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            input_name.setText("Olá " + user.getDisplayName() + " onde vamos jogar :)");
        }
    }

    private void setupPlaceAutoComplete() {

        placesFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        placesFragment.setPlaceFields(placeFields);
        placesFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Geocoder geocoder = new Geocoder(ScheduleGameActivity.this);
                List<Address> addressList = new ArrayList<>();

                try {
                    addressList = geocoder.getFromLocationName(place.getName(),1);
                } catch (IOException e) {
                    toastMessage( e.getMessage());
                }

                if (addressList.size() > 0){
                    Address address = addressList.get(0);

                    geoPoint = new GeoPoint(address.getLatitude(),address.getLongitude());
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                toastMessage(status.getStatusMessage());
            }
        });
    }

    private void toastMessage(String message) {

        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void getUserDetails(){


        DocumentReference userRef = mDB
                .collection(getString(R.string.collection_users))
                .document(userID);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){
                    captain = task.getResult().toObject(User.class);
                    game = new Football(gameDate,gameHour,captain,geoPoint);
                    game.addPlayers(captain);
                    game.setTimestamp(null);
                    saveGame();
                }
                else toastMessage(task.getException().getMessage());
            }
        });
    }

    private void saveGame(){
        CollectionReference gameRefCollection = mDB.collection(getString(R.string.collection_games));
        gameRefCollection.add(game).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()){
                    toastMessage("sucess");
                }else toastMessage(task.getException().getMessage());
            }
        });

    }
}
