package pt.ismai.pedro.sisproject.Models;

import com.google.firebase.firestore.GeoPoint;

public class Football extends Game {

    public Football() {
    }

    public Football(String gameDate, String hour, User captain, GeoPoint geoPoint, int typeOfGame) {
        super(gameDate, hour, captain, geoPoint, typeOfGame);
    }

    @Override
    public void setCaptain(User captain) {
        super.setCaptain(captain);
    }

    @Override
    public User getCaptain() {
        return super.getCaptain();
    }

    @Override
    public void addPlayers(User player) {
        super.addPlayers(player);
    }

}
