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

package com.stackmob.sdk;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.api.StackMobQueryWithField;
import com.stackmob.sdk.concurrencyutils.MultiThreadAsserter;
import com.stackmob.sdk.testobjects.*;
import com.stackmob.sdk.util.Pair;
import org.junit.*;

import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.exception.StackMobException;
import static org.junit.Assert.*;
import static com.stackmob.sdk.concurrencyutils.CountDownLatchUtils.*;

public class StackMobTests extends StackMobTestCommon {

    @Test public void login() throws Exception {
        final String username = getRandomString();
        final String password = getRandomString();
        final User user = new User(username, password);
        final StackMobObjectOnServer<User> objectOnServer = createOnServer(user, User.class);
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", user.username);
        params.put("password", user.password);

        final CountDownLatch latch = latchOne();

        stackmob.login(params, new StackMobCallback() {
            @Override public void success(String responseBody) {
                asserter.markNotNull(responseBody);
                asserter.markNotJsonError(responseBody);
                latch.countDown();
            }

            @Override public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
        objectOnServer.delete();
    }

    @Test public void loginShouldFail() throws Exception {
        final String username = getRandomString();
        final String password = getRandomString();

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);

        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.login(params, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markJsonError(responseBody);
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                fail("login was supposed to fail with a 200 but a JSON error");
            }
        });
        asserter.assertLatchFinished(latch);
    }

    @Test public void logout() throws Exception {
        final String username = getRandomString();
        final String password = getRandomString();

        final User user = new User(username, password);
        final StackMobObjectOnServer<User> objectOnServer = createOnServer(user, User.class);

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", user.username);
        params.put("password", user.password);

        final CountDownLatch loginLatch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.login(params, new StackMobCallback() {
            @Override public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                final CountDownLatch logoutLatch = latchOne();

                stackmob.logout(new StackMobCallback() {
                    @Override public void success(String responseBody2) {
                        asserter.markNotNull(responseBody2);
                        asserter.markNotJsonError(responseBody2);
                        logoutLatch.countDown();
                    }
                    @Override public void failure(StackMobException e) {
                        asserter.markException(e);
                    }
                });

                try {
                    asserter.markLatchFinished(logoutLatch);
                    loginLatch.countDown();
                }
                catch(InterruptedException e) {
                    asserter.markFailure("logout did not complete");
                }
            }
            @Override public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });

        asserter.assertLatchFinished(loginLatch, new Pair<Long, TimeUnit>(4L, TimeUnit.SECONDS));
        objectOnServer.delete();
    }

    @Test public void startSession() throws InterruptedException, StackMobException {
        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.startSession(new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotNull(responseBody);
                asserter.markNotJsonError(responseBody);
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
    }

    @Test public void getWithoutArguments() throws Exception {
        final Game game = new Game(Arrays.asList("one", "two"), "one");
        final StackMobObjectOnServer<Game> objectOnServer = createOnServer(game, Game.class);
        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.get("game", new StackMobCallback() {
            @Override public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                List<Game> games = gson.fromJson(responseBody, Game.ListTypeToken);
                asserter.markNotNull(games);
                asserter.markFalse(games.isEmpty());
                latch.countDown();
            }

            @Override public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
        objectOnServer.delete();
    }

    @Test public void getWithArguments() throws Exception {
        final Game game = new Game(Arrays.asList("one", "two"), "one");
        final StackMobObjectOnServer<Game> objectOnServer = createOnServer(game, Game.class);

        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("name", game.name);
        stackmob.get("game", arguments, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                List<Game> games = gson.fromJson(responseBody, Game.ListTypeToken);
                asserter.markNotNull(games);
                asserter.markTrue(games.size() >= 1);
                asserter.markEquals("one", games.get(0).name);
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);

        objectOnServer.delete();
    }

    @Test
    public void getWithQuery() throws InterruptedException, StackMobException {
        final Game g = new Game(Arrays.asList("seven", "six"), "woot");
        final StackMobObjectOnServer<Game> objectOnServer = createOnServer(g, Game.class);

        StackMobQuery query = new StackMobQuery("game").fieldIsGreaterThanOrEqualTo("name", "sup");
        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.get(query, new StackMobCallback() {
            @Override public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                List<Game> games = gson.fromJson(responseBody, Game.ListTypeToken);
                asserter.markNotNull(games);
                asserter.markTrue(games.size() >= 1);
                asserter.markEquals("woot", games.get(0).name);
                latch.countDown();
            }
            @Override public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
        objectOnServer.delete();
    }

    @Test public void getWithQueryWithField() throws Exception {
        final Game g = new Game(Arrays.asList("seven", "six"), "woot");
        final StackMobObjectOnServer<Game> objectOnServer = createOnServer(g, Game.class);

        StackMobQuery q = new StackMobQuery("game");
        StackMobQueryWithField qWithField = new StackMobQueryWithField("name", q).isGreaterThanOrEqualTo("sup");
        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.get(qWithField, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                List<Game> games = gson.fromJson(responseBody, Game.ListTypeToken);
                asserter.markNotNull(games);
                asserter.markTrue(games.size() >= 1);
                asserter.markEquals("woot", games.get(0).name);
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
        objectOnServer.delete();
    }

    @Test public void getWithOrderBy() throws Exception {
        List<String> players = Arrays.asList("one", "two");
        final Game g1 = new Game(players, "gamea");
        final Game g2 = new Game(players, "gameb");
        final StackMobObjectOnServer<Game> g1OnServer = createOnServer(g1, Game.class);
        final StackMobObjectOnServer<Game> g2OnServer = createOnServer(g2, Game.class);

        StackMobQuery q = StackMobQuery.objects("game").field("name").isOrderedBy(StackMobQuery.Ordering.ASCENDING).getQuery();

        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.get(q, new StackMobCallback() {
            @Override public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                List<Game> gamesFromServer = gson.fromJson(responseBody, Game.ListTypeToken);
                Game prevGame = null;
                for(Game g: gamesFromServer) {
                    if(prevGame != null) {
                        asserter.markTrue(g.getName().compareTo(prevGame.getName()) >= 0);
                    }
                    prevGame = g;
                }
                latch.countDown();
            }

            @Override public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
        g1OnServer.delete();
        g2OnServer.delete();
    }

    @Ignore("Range header does not work. Can use X-StackMob-Range, but it is going to be deprecated immediately after Range works")
    @Test public void getWithRange() throws Exception {
        List<String> players = Arrays.asList("one", "two");
        final Game g1 = new Game(players, "gamea");
        final Game g2 = new Game(players, "gameb");
        final Game g3 = new Game(players, "gamec");
        final StackMobObjectOnServer<Game> g1OnServer = createOnServer(g1, Game.class);
        final StackMobObjectOnServer<Game> g2OnServer = createOnServer(g2, Game.class);
        final StackMobObjectOnServer<Game> g3OnServer = createOnServer(g3, Game.class);

        final int rangeStart = 1;
        final int rangeEnd = 2;
        StackMobQuery q = StackMobQuery.objects("game").isInRange(rangeStart, rangeEnd);

        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.get(q, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                List<Game> games = gson.fromJson(responseBody, Game.ListTypeToken);
                asserter.markEquals(rangeEnd - rangeStart + 1, games.size());
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });

        asserter.assertLatchFinished(latch);
        g1OnServer.delete();
        g2OnServer.delete();
        g3OnServer.delete();
    }

    @Test public void postWithRequestObject() throws Exception {
        final Game g = new Game(Arrays.asList("one", "two"), "newGame");
        g.name = "newGame";
        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.post("game", g, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                Game game = gson.fromJson(responseBody, Game.class);
                StackMobObjectOnServer<Game> onServer = new StackMobObjectOnServer<Game>(stackmob, game.game_id, game);
                try {
                    onServer.delete();
                } catch (StackMobException e) {
                    asserter.markException(e);
                }

                asserter.markEquals("newGame", game.name);
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);

    }

    @Test public void deleteWithId() throws Exception {
        final Game game = new Game(new ArrayList<String>(), "gameToDelete");
        final StackMobObjectOnServer<Game> objectOnServer = createOnServer(game, Game.class);
        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.delete("game", objectOnServer.getObjectId(), new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
    }

    @Test public void put() throws Exception {
        final String oldName = "oldGameName";
        final String newName = "newGameName";

        final Game game = new Game(Arrays.asList("one", "two"), oldName);
        final StackMobObjectOnServer<Game> objectOnServer = createOnServer(game, Game.class);
        final String objectId = objectOnServer.getObjectId();

        game.name = newName;
        final Game updatedGame = new Game(Arrays.asList("modified", "modified2"), "modified_game");
        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.put(game.getName(), objectId, updatedGame, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                Game jsonGame = gson.fromJson(responseBody, Game.class);
                asserter.markNotNull(jsonGame);
                asserter.markEquals(updatedGame.name, jsonGame.name);
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
        objectOnServer.delete();
    }

    @Test public void registerToken() throws Exception {
        final String username = getRandomString();
        final String password = getRandomString();
        final String token = getRandomString();

        User user = new User(username, password);
        final StackMobObjectOnServer<User> objectOnServer = createOnServer(user, User.class);
        final String objectId = objectOnServer.getObjectId();

        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.registerForPushWithUser(objectId, token, new StackMobCallback() {
            @Override public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                latch.countDown();
            }

            @Override public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
        objectOnServer.delete();
    }

    @Test public void getTokensForUsers() throws Exception {
        final String username = getRandomString();
        final List<String> usernames = new ArrayList<String>();
        usernames.add(username);
        final CountDownLatch latch = latchOne();
        final MultiThreadAsserter asserter = new MultiThreadAsserter();

        stackmob.getTokensForUsers(usernames, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                asserter.markNotJsonError(responseBody);
                latch.countDown();
            }

            @Override
            public void failure(StackMobException e) {
                asserter.markException(e);
            }
        });
        asserter.assertLatchFinished(latch);
    }
}