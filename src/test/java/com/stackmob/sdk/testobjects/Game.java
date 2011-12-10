/**
 * Copyright 2011 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.sdk.testobjects;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Game extends StackMobObject {

    //public for the benefit of Gson
    public List<String> players;
    public String game_id;
    public Long createddate;
    public Long lastmoddate;
    public String name;
    public List<String> moderators;

    public static final Type ListTypeToken = new TypeToken<List<Game>>() {}.getType();

    public Game(List<String> players, String gameId, long createdDate, long lastModDate, String name) {
        this(players, name);
        this.game_id = gameId;
        this.createddate = createdDate;
        this.lastmoddate = lastModDate;
        this.moderators = new ArrayList<String>();
    }

    public Game(List<String> players, String name) {
        this.players = players;
        this.name = name;
    }

    @Override public String getIdField() { return game_id; }
    @Override public String getIdFieldName() { return "game_id"; }
    @Override public String getName() { return "game"; }
}
