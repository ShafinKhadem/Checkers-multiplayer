# Network-Checkers
Our term project of Level 1 term 2 Network checkers

# Screenshots of game

![Game-flow](screenshots/ss1.png?raw=true "Game-flow")

<p align="center">Game-flow</p>

![Sign in, sign up and waiting for opponent](screenshots/ss2.png?raw=true "Sign in, sign up and waiting for opponent")

<p align="center">Sign in, sign up and waiting for opponent</p>

![Help, surrender and track record options](screenshots/ss3.png?raw=true "Help, surrender and track record options")

<p align="center">Help, surrender and track record options</p>


# Key features

1. Multi-player checkers between different PCs when with network connection, you can play with two players at the same PC by turning off network.

2. Don’t know rules of checkers? No worries, go to Options → help. Almost every rule is covered there. It also shows all possible moves if you select a piece.

3. Have you become sure that you are going to lose this game? No need to waste time, you have option to surrender.

4. Online track record system which shows how many games you have played on-line and how many games you have won on-line.

5. If any player closes the game before finishing an on-line game, opponent wins automatically.

6. In on-line game, you see the game from you side, if you play using white, you see from white side, if you play using black, you see from black side. White and black are decided automatically from the server while pairing two players.

7. Auto select desired piece when jump is mandatory.

8. Obviously there is option to sign in and sign up to keep track record. Shows your name, opponent’s name and how many pieces have been eaten of each color.

# How to run in linux

Check java version using: `java --version`

### Using openjdk-11

- javafx sdk isn't shipped together, you have to separately install javafx sdk: `sudo apt install openjfx`.
- cd Network-Checkers
- Run server:

```
java --module-path /usr/share/openjfx/lib --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web -ea -jar out/artifacts/server/server.jar
```

- Run client:

```
java --module-path /usr/share/openjfx/lib --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web -ea -jar out/artifacts/client/client.jar
```


### Using oracle jdk 8

- Install oracle jdk 8 (available in [my onedrive](https://buetedu-my.sharepoint.com/:f:/g/personal/1605045_cse_buet_ac_bd/EnoDdp4Gt2VLqIf7P3c_M7ABXnvJ6inFjXDVFqnZsPrAWQ?e=KjEpeN))
- cd Network-Checkers
- /LOCATION_TO_JDK/bin/java -jar out/artifacts/server/server.jar
- /LOCATION_TO_JDK/bin/java -jar out/artifacts/client/client.jar

__Other versions of oracle jdk and openjdk, even openjdk-8 may not work using this method.__

# Used technologies
- Sockets
- Threads
- JavaFX

# Biggest lessons
- I should've decoupled grid (UI component) and state in GameMain, using either MVC or something like:

```java
new Thread(()->{
    while (true) {
        Platform.runLater(GameMain::displayState);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}).start();
```

- __I should've separated UI components in Controllers using Controller's initialize() and avoided scene.lookup("#id") completely.__

# Useful links
- https://www.coolmath-games.com/0-checkers
- https://github.com/MKotlik/versa-checkers
