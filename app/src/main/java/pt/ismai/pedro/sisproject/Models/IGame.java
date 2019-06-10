package pt.ismai.pedro.sisproject.Models;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;

public interface IGame {
    String getGameID();
    String getGameDate();
    String getHour();
    int getCapacity();
    int getNumberOfPlayers();
    GeoPoint getGeoPoint();
    Date getTimestamp();
    int getTypeOfGame();
    ArrayList<User> getPlayers();
    User getCaptain();

    void setGameID(String gameID);
    void setPlayers(ArrayList<User> players);
    void setNumberOfPlayers(int numberOfPlayers);
    void setGeoPoint(GeoPoint geoPoint);
    void setTimestamp(Date timestamp);
    void setCaptain(User captain);
    void setCapacity(int capacity);
    void setGameDate(String gameDate);
    void setHour(String hour);
    void setTypeOfGame(int typeOfGame);

    void addPlayers(User player);
    int gamePlayers();


}
