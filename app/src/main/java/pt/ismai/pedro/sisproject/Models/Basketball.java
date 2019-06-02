package pt.ismai.pedro.sisproject.Models;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Basketball extends Game{

    public Basketball(String gameDate, String hour, User captain, GeoPoint geoPoint) {
        super(gameDate, hour, captain, geoPoint);
    }

    @Override
    public void addPlayers(User player) {
        super.addPlayers(player);
    }

    @Override
    public int givenValue() {
        return 10;
    }
}