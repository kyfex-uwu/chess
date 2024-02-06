package ui.rendering.scene;

import chess.ChessGame;
import chess.Json;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.GameData;
import model.JoinGameData;
import ui.ArgConsumer;
import ui.Config;
import ui.Online;
import ui.PlayData;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Nineslice;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayMenuScene extends Scene{
    private ArrayList<GameData> games = new ArrayList<>();
    @Override
    public void init() {
        this.refreshGames();

        this.toRender.add(new Background());
        this.toRender.add(new Renderable(-1) {
            @Override
            public void render(Pixel[][] screen) {
                Nineslice.Style.PANEL.nineslice.render(screen,
                        2, 1, 36, PlayMenuScene.this.games.size()+2);
                Sprite.Builder.fromStr(String.join("\n",PlayMenuScene.this.games.stream().map(data->{
                    var toReturn = data.gameName;

                    ChessGame.TeamColor myTeam=null;
                    if(data.whiteUsername!=null&&!data.whiteUsername.equals(PlayData.currAuth.username())){
                        toReturn+=" vs " + data.whiteUsername;
                        myTeam= ChessGame.TeamColor.BLACK;
                    }else if(data.blackUsername!=null&&!data.blackUsername.equals(PlayData.currAuth.username())){
                        toReturn+=" vs " + data.blackUsername;
                        myTeam= ChessGame.TeamColor.WHITE;
                    }

                    if(data.game.getTeamTurn()==myTeam)
                        toReturn="* "+toReturn;
                    else
                        toReturn=((char)0)+" "+toReturn;

                    if(toReturn.length()>32) toReturn=toReturn.substring(0,29)+"...";
                    return toReturn;
                }).toList())).withFGColor(Config.Palette.BUTTON_TEXT).build().draw(3,2,screen);
                Sprite.Builder.fromDims(1,PlayMenuScene.this.games.size())
                        .withFGColor(Config.Palette.BUTTON_OUTLINE).build().draw(3,2,screen);
                for(int i=0;i<PlayMenuScene.this.games.size();i++)
                    Renderable.overlayPixel(36,2+i,new Pixel(Character.forDigit(i,36),
                            Config.Palette.BUTTON_TEXT, null),screen);

                Nineslice.Style.PANEL.nineslice.render(screen, screen[0].length-18,screen.length-4,
                        16,3,"Local Play");
                Nineslice.Style.PANEL.nineslice.render(screen, 2,screen.length-4,
                        17,3,"Create Game");
            }
        });
        this.toRender.add(new Renderable(0) {
            @Override
            public void render(Pixel[][] screen) {
                if(!PlayMenuScene.this.createMode) return;

                for(var y=0;y<screen.length;y++){
                    for(var x=0;x<screen[y].length;x++){
                        screen[y][x] = new Pixel(screen[y][x].character, new Color(
                                (int) (screen[y][x].fg.r*0.5),
                                (int) (screen[y][x].fg.g*0.5),
                                (int) (screen[y][x].fg.b*0.5)
                        ), new Color(
                                (int) (screen[y][x].bg.r*0.5),
                                (int) (screen[y][x].bg.g*0.5),
                                (int) (screen[y][x].bg.b*0.5)
                        ));
                    }
                }
            }
        });
        this.toRender.add(new Renderable(1) {
            @Override
            public void render(Pixel[][] screen) {
                if(!PlayMenuScene.this.createMode) return;

                Nineslice.Style.INPUT.nineslice.render(screen, 17,7,
                        screen[0].length-34,3,"Game Name > "+PlayMenuScene.this.gameName);
                Nineslice.Style.INPUT.nineslice.render(screen, 17,11,
                        (screen[0].length-34)/2,3,"Start as > "+PlayMenuScene.this.startAsWhite);

                Nineslice.Style.PANEL.nineslice.render(screen, screen[0].length/2-8,
                        screen.length-10, 14,3,"Create");
            }
        });
        super.init();
    }

    private void refreshGames() {
        if(!PlayData.loggedIn()) return;

        Online.request(Online.ReqMethod.GET, "game/" + PlayData.selfData.username(),
                        (String)null)
                .ifSuccess(data->{
                    try {
                        this.games.clear();
                        var games = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("games");
                        for(var game : games){
                            var gameData = Json.GSON.fromJson(game.toString(), GameData.class);
                            this.games.add(gameData);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        PlayMenuScene.this.dialogMessage="Failed to parse games";
                    }
                }).ifError(error->{
                    PlayMenuScene.this.changeScene(new TitleScene());
                });
    }

    private boolean createMode=false;
    private String gameName="";
    private StartType startAsWhite=StartType.RANDOM;
    private enum StartType{
        WHITE,
        BLACK,
        RANDOM
    }
    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "back", args -> this.changeScene(new TitleScene()),
            "localplay", args ->{
                this.changeScene(new GameScene(new GameData(-1, "Local Game"),
                        "Ooga", "Booga", false));
            },
            "open", args -> {
                if(args.length==0) return;
                if(args[0].length()<3){
                    try {
                        var gameIndex = Integer.valueOf(args[0], 36);
                        if(gameIndex>=0&&gameIndex<this.games.size()){
                            this.changeScene(new GameScene(this.games.get(gameIndex)));
                            return;
                        }
                    }catch(Exception ignored){}
                }
                AtomicBoolean foundGame= new AtomicBoolean(false);
                this.games.stream().filter(data->data.gameName.equals(args[0])).findFirst()
                        .ifPresent(data->{
                            foundGame.set(true);
                            this.changeScene(new GameScene(data));
                        });
                if(!foundGame.get())
                    this.dialogMessage = "Could not find game";
            },
            "create", args -> this.createMode=true
    ),ArgConsumer.helpCommandMaker(
            "back", "Returns to the title screen",
            "localplay", "Starts a game locally",
            "open [game]", "Opens the specified game",
            "create", "Creates a game"
    ));
    private final ArgConsumer createConsumer = new ArgConsumer(Map.of(
            "back", args -> this.createMode=false,
            "name", args -> {
                if(args.length==0) return;
                if(args[0].length()>=3&&args[0].length()<=32)
                    this.gameName = args[0];
                else
                    this.dialogMessage = "Wrong size: name must be 3-32 characters";
            },
            "startas", args -> {
                if(args.length==0) return;
                if(args[0].toLowerCase(Locale.ROOT).equals("white")) this.startAsWhite=StartType.WHITE;
                if(args[0].toLowerCase(Locale.ROOT).equals("black")) this.startAsWhite=StartType.BLACK;
                if(args[0].toLowerCase(Locale.ROOT).equals("random")) this.startAsWhite=StartType.RANDOM;
            },
            "create", args -> {
                var toSend = new JsonObject();
                toSend.addProperty("gameName", this.gameName);
                Online.request(Online.ReqMethod.POST, "game", Json.GSON.toJson(toSend))
                        .ifSuccess(s -> {
                            this.createMode=false;
                            final int id;
                            try{
                                id = ((Double) Json.GSON.fromJson(s, Map.class).get("gameID")).intValue();
                            }catch (Exception e){
                                PlayMenuScene.this.dialogMessage = "Could not parse game ID";
                                return;
                            }
                            Online.request(Online.ReqMethod.PUT, "game",
                                            new JoinGameData(switch (PlayMenuScene.this.startAsWhite) {
                                                case WHITE -> "WHITE";
                                                case BLACK -> "BLACK";
                                                case RANDOM -> Math.random() < 0.5 ? "WHITE" : "BLACK";
                                            }, id))
                                    .ifSuccess(s2 -> {
                                        PlayMenuScene.this.dialogMessage = "Created game!";
                                        this.refreshGames();
                                    }).ifError(errorMessage -> {
                                        PlayMenuScene.this.dialogMessage = "Created game with id " + id + ", but could not join";
                                    });
                        }).ifError(errorMessage -> {
                            System.out.println(errorMessage.message());
                            PlayMenuScene.this.dialogMessage="Could not create game";
                        });
            }
    ),ArgConsumer.helpCommandMaker(
            "back", "Returns to the play menu",
            "name", "Sets the game name",
            "startas [white|black|random]", "Sets what color you start as",
            "create", "Creates the game"
    ));
    @Override
    public void uninit() {

    }

    @Override
    public void onLine(String[] args){
        if(!createMode) {
            this.consumer.tryConsumeArgs(args);
            if (this.consumer.shouldShowHelp) this.dialogMessage = this.consumer.helpCommand;
        }else{
            this.createConsumer.tryConsumeArgs(args);
            if(this.createConsumer.shouldShowHelp) this.dialogMessage = this.createConsumer.helpCommand;
        }
        
        super.onLine(args);
    }
}
