package pt.ismai.pedro.sisproject.Activities;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import net.steamcrafted.lineartimepicker.dialog.LinearDatePickerDialog;
import net.steamcrafted.lineartimepicker.dialog.LinearTimePickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pt.ismai.pedro.sisproject.Models.Football;
import pt.ismai.pedro.sisproject.Models.GameLocation;
import pt.ismai.pedro.sisproject.Models.User;
import pt.ismai.pedro.sisproject.Models.UserSingleton;
import pt.ismai.pedro.sisproject.R;

public class ScheduleGameActivity extends AppCompatActivity {

    TextView input_name;
    Button date, hour, saveGame;
    LinearDatePickerDialog dialog;
    LinearTimePickerDialog timePickerDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private String gameDate;
    private String gameHour;
    private int gameYear,gameMonth,gameDay,gameMinute;
    private GeoPoint geoPoint;
    private Football game;
    private String userID;
    User captain;


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
