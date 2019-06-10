package pt.ismai.pedro.sisproject.Models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Game implements IGame {

    private String gameID;
    private ArrayList<User> players;
    private int numberOfPlayers;
    private int capacity;
    private String gameDate;
    private String hour;
    private User captain;
    private GeoPoint geoPoint;
    private int typeOfGame;
    private @ServerTimestamp Date timestamp;

    public Game(){}

    public Game(String gameDate, String hour, User captain, GeoPoint geoPoint, int typeOfGame) {
        this.capacity = gamePlayers();
        this.players = new ArrayList<>();
        this.gameDate = gameDate;
        this.hour = hour;
        this.captain = captain;
        this.geoPoint = geoPoint;
        this.typeOfGame = typeOfGame;
        this.numberOfPlayers = 0;
    }

    @Override
    public String getGameID() {
        return gameID;
    }

    @Override
    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    @Override
    public ArrayList<User> getPlayers() {
        return players;
    }

    @Override
    public void setPlayers(ArrayList<User> players) {
        this.players = players;
    }

    @Override
    public String getGameDate() {
        return gameDate;
    }

    @Override
    public String getHour() {
        return hour;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    @Override
    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public int getTypeOfGame() {
        return typeOfGame;
    }

    @Override
    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    @Override
    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public User getCaptain() {
        return captain;
    }

    @Override
    public void setCaptain(User captain) {
        this.captain = captain;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void setGameDate(String gameDate) {
        this.gameDate = gameDate;
    }

    @Override
    public void setHour(String hour) {
        this.hour = hour;
    }

    @Override
    public void setTypeOfGame(int typeOfGame) {
        this.typeOfGame = typeOfGame;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameID='" + gameID + '\'' +
                ", players=" + players +
                ", numberOfPlayers=" + numberOfPlayers +
                ", capacity=" + capacity +
                ", gameDate='" + gameDate + '\'' +
                ", hour='" + hour + '\'' +
                ", captain=" + captain +
                ", geoPoint=" + geoPoint +
                ", typeOfGame=" + typeOfGame +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int gamePlayers() {

        int gameCapacity;

        switch (typeOfGame){
            case 0:
                gameCapacity = 11;
                break;
            case 1:
                gameCapacity = 9;
                break;
            case 2:
                gameCapacity = 3;
                break;
            case 3:
                gameCapacity = 3;
                break;
            case 4:
                gameCapacity = 3;
                break;
            case 5:
                gameCapacity = 3;
                break;
            default:
                gameCapacity = 3;
                break;
        }
        return gameCapacity;
    }

    @Override
    public void addPlayers(User player) {
       players.add(player);
       numberOfPlayers++;

    }
}


