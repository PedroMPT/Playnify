package pt.ismai.pedro.sisproject.Models;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Running extends Game {

    public Running(String gameDate, String hour, User captain, GeoPoint geoPoint) {
        super(gameDate, hour, captain, geoPoint);
    }

    @Override
    public void addPlayers(User player) {
        setNumberOfPlayers(3);
        super.addPlayers(player);
    }

    @Override
    public int givenValue() {
        return 4;
    }
}
