package pt.ismai.pedro.sisproject.Models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public abstract class Game implements IGame {

    private User[] players;
    private int numberOfPlayers = 0;
    private int capacity;
    private String gameDate;
    private String hour;
    private User captain;
    private GeoPoint geoPoint;
    private int typeOfGame;
    private @ServerTimestamp Date timestamp;

    public Game(){}

    public Game(String gameDate, String hour, User captain, GeoPoint geoPoint, int typeOfGame) {
        this.capacity = givenValue();
        this.players = new User[capacity];
        this.gameDate = gameDate;
        this.hour = hour;
        this.captain = captain;
        this.geoPoint = geoPoint;
        this.typeOfGame = typeOfGame;
    }

    public String getGameDate() {
        return gameDate;
    }

    public String getHour() {
        return hour;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getTypeOfGame() {
        return typeOfGame;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public User getCaptain() {
        return captain;
    }

    public void setCaptain(User captain) {
        this.captain = captain;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setGameDate(String gameDate) {
        this.gameDate = gameDate;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setTypeOfGame(int typeOfGame) {
        this.typeOfGame = typeOfGame;
    }

    public abstract int givenValue();

    @Override
    public void addPlayers(User player) {
        if(numberOfPlayers < capacity){
            players[numberOfPlayers] = player;
            numberOfPlayers++;
        }

    }


}


