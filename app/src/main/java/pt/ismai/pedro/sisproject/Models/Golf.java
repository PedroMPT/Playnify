package pt.ismai.pedro.sisproject.Models;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Golf extends Game {

    public Golf() {
    }

    public Golf(String gameDate, String hour, User captain, GeoPoint geoPoint, int typeOfGame) {
        super(gameDate, hour, captain, geoPoint, typeOfGame);
    }

    @Override
    public void addPlayers(User player) {
        setNumberOfPlayers(3);
        super.addPlayers(player);
    }

}
