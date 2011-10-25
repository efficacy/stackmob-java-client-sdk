package com.stackmob.sdk.testobjects;

import java.util.List;

public class Game extends StackMobObject {

    //public for the benefit of Gson
    public List<String> players;
    public String game_id;
    public Long createddate;
    public Long lastmoddate;
    public String name;

    public Game(List<String> players, String gameId, long createdDate, long lastModDate, String name) {
        this(players, name);
        this.game_id = gameId;
        this.createddate = createdDate;
        this.lastmoddate = lastModDate;
    }

    public Game(List<String> players, String name) {
        this.players = players;
        this.name = name;
    }

    @Override public String getIdFieldName() { return "game_id"; }
    @Override public String getName() { return "game"; }
}
