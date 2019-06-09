package pt.ismai.pedro.sisproject.Models;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Tennis extends Game {

    public Tennis() {
    }

    public Tennis(String gameDate, String hour, User captain, GeoPoint geoPoint, int typeOfGame) {
        super(gameDate, hour, captain, geoPoint, typeOfGame);
    }

    @Override
    public void addPlayers(User player) {
        setNumberOfPlayers(3);
        super.addPlayers(player);
    }

}
