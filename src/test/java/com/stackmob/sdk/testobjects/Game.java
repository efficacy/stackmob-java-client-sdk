package com.stackmob.sdk.testobjects;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Game extends StackMobObject {

    //public for the benefit of Gson
    public List<String> players;
    public String game_id;
    public Long createddate;
    public Long lastmoddate;
    public String name;

    public static final Type ListTypeToken = new TypeToken<List<Game>>() {}.getType();

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
