package pt.ismai.pedro.sisproject.Models;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class GameLocation {

    private GeoPoint geoPoint;
    private @ServerTimestamp Date timestamp;
    private Game game;

    public GameLocation() {

    }

    public GameLocation(GeoPoint geoPoint, Date timestamp, Game game) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.game = game;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Game getGame() {
        return game;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public String toString() {
        return "GameLocation{" +
                "geoPoint=" + geoPoint +
                ", timestamp=" + timestamp +
                ", game=" + game +
                '}';
    }
}
