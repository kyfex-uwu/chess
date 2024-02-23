package ui.rendering.scene;

import chess.ChessGame;
import chess.Serialization;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.GameData;
import model.JoinGameData;
import model.UserData;
import ui.*;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Nineslice;
import webSocketMessages.userCommands.JoinAsObserverCommand;
import webSocketMessages.userCommands.JoinAsPlayerCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayMenuScene extends Scene{
    public ArrayList<GameData> myGames = new ArrayList<>();
    @Override
    public void init() {
        this.refreshGames();
        WebsocketManager.init();

        this.toRender.add(new Background());
        this.toRender.add(new Renderable(-1) {
            @Override
            public void render(Pixel[][] screen) {
                Nineslice.Style.PANEL.nineslice.render(screen,
                        2, 1, 36, PlayMenuScene.this.myGames.size()+2);
                Sprite.Builder.fromStr(String.join("\n",PlayMenuScene.this.myGames.stream().map(data->{
                    var toReturn = data.gameName;

                    ChessGame.TeamColor myTeam=null;
                    if(data.whiteUsername==null&&data.blackUsername!=null){
                        if(data.blackUsername.equals(PlayData.currAuth.username())){
                            myTeam = ChessGame.TeamColor.BLACK;
                        }else{
                            toReturn+=": "+data.blackUsername;
                        }
                    }else if(data.whiteUsername!=null&&data.blackUsername==null){
                        if(data.whiteUsername.equals(PlayData.currAuth.username())){
                            myTeam = ChessGame.TeamColor.WHITE;
                        }else{
                            toReturn+=": "+data.whiteUsername;
                        }
                    }else if(data.whiteUsername!=null&&data.blackUsername!=null){
                        if(data.whiteUsername.equals(PlayData.currAuth.username())){
                            myTeam = ChessGame.TeamColor.WHITE;
                            toReturn+=" vs "+data.blackUsername;
                        }else if(data.blackUsername.equals(PlayData.currAuth.username())){
                            myTeam = ChessGame.TeamColor.BLACK;
                            toReturn+=" vs "+data.whiteUsername;
                        }else{
                            toReturn+=": "+data.whiteUsername+" vs "+data.whiteUsername;
                        }
                    }

                    if(data.game.getTeamTurn()==myTeam)
                        toReturn="* "+toReturn;
                    else
                        toReturn=((char)0)+" "+toReturn;

                    if(toReturn.length()>32) toReturn=toReturn.substring(0,29)+"...";
                    return toReturn;
                }).toList())).withFGColor(Config.Palette.BUTTON_TEXT).build().draw(3,2,screen);
                Sprite.Builder.fromDims(1,PlayMenuScene.this.myGames.size())
                        .withFGColor(Config.Palette.BUTTON_OUTLINE).build().draw(3,2,screen);
                for(int i = 0; i<PlayMenuScene.this.myGames.size(); i++)
                    Renderable.overlayPixel(36,2+i,new Pixel(Character.forDigit(i,36),
                            Config.Palette.BUTTON_TEXT, null),screen);

                Nineslice.Style.PANEL.nineslice.render(screen, screen[0].length-18,screen.length-4,
                        16,3,"Local Play");
                Nineslice.Style.PANEL.nineslice.render(screen, 2,screen.length-4,
                        17,3,"Create Game");
                Nineslice.Style.PANEL.nineslice.render(screen, (screen[0].length-18)/2,screen.length-4,
                        18,3,"Browse Games");
            }
        });

        this.toRender.add(new Renderable(0) {
            @Override
            public void render(Pixel[][] screen) {
                if(!PlayMenuScene.this.createMode&&!PlayMenuScene.this.browsing) return;

                Background.darken(screen);
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

        this.toRender.add(new Renderable(1) {
            @Override
            public void render(Pixel[][] screen) {
                if(!PlayMenuScene.this.browsing) return;

                var width = screen[0].length-24;
                var height = screen.length-14;
                Nineslice.Style.PANEL.nineslice.render(screen,10,5,width+4,height+4);

                var gamesPerRow = (int) Math.floor(width/38f);//16+16+4+2+2 username+username+" vs "+id+padding
                var padding = (width-gamesPerRow*38)/2;

                int y=0;
                int x=0;
                int index=PlayMenuScene.this.browsingIndex;
                while(true){
                    if(index>=PlayMenuScene.this.browsableGames.size()||index>gamesPerRow*height/3) break;

                    var data = PlayMenuScene.this.browsableGames.get(index);

                    var names = "";
                    if(data.whiteUsername==null&&data.blackUsername!=null)
                        names = data.blackUsername;
                    else if(data.whiteUsername!=null&&data.blackUsername==null)
                        names = data.whiteUsername;
                    else if(data.whiteUsername!=null&&data.blackUsername!=null)
                        names = data.whiteUsername+" vs "+data.blackUsername;

                    Sprite.Builder.fromStr("   "+data.gameName+"\n"+names).withFGColor(Config.Palette.BUTTON_TEXT)
                            .build().draw(13+padding+x*38,6+y*3,screen);
                    Sprite.Builder.fromStr(Integer.toString(index-PlayMenuScene.this.browsingIndex, 36))
                            .withFGColor(Config.Palette.BUTTON_TEXT)
                            .build().draw(13+padding+x*38, 6+y*3, screen);

                    x++;
                    if(x>=gamesPerRow){
                        x=0;
                        y++;
                    }
                    index++;
                }

                Sprite.Builder.fromStr("<Prev<").withFGColor(Config.Palette.BUTTON_TEXT)
                        .build().draw((screen[0].length-8)/2-8, screen.length-7, screen);
                Sprite.Builder.fromStr("Page 0").withFGColor(Config.Palette.BUTTON_TEXT)
                        .build().draw((screen[0].length-8)/2, screen.length-7, screen);
                Sprite.Builder.fromStr(">Next>").withFGColor(Config.Palette.BUTTON_TEXT)
                        .build().draw((screen[0].length-8)/2+9, screen.length-7, screen);
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
                        this.myGames.clear();
                        var games = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("games");
                        for(var game : games){
                            var gameData = Serialization.GSON.fromJson(game.toString(), GameData.class);
                            this.myGames.add(gameData);
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
    private boolean browsing=false;
    private int browsingIndex=0;
    private ArrayList<GameData> browsableGames = new ArrayList<>();
    private String gameName="";
    private StartType startAsWhite=StartType.RANDOM;
    private enum StartType{
        WHITE,
        BLACK,
        RANDOM
    }
    private static final String[] pfpStarters={
            """
                \s/▼▼^-
                \sL_/|\s
                \s/__|\s
                000000000000000000
                aaaaaaaaaaaaaaaaaa""".replaceAll("\n",""),
            """
                \s┏╸  \s
                \so \\o\s
                \sA  A\s
                044000030440030040
                aaabbbaaabbbbbbaaa""".replaceAll("\n",""),
            """
                \s▄--▄\s
                | ║║ |
                \s▀°°▀\s
                0g00g00000000g00g0
                aaggaaggggggaaggaa""".replaceAll("\n","")
    };
    private static UserData playerGenerator(String name){
        var pfp = pfpStarters[(int) (Math.random()*pfpStarters.length)];
        var newColors=" ".repeat(36).toCharArray();
        for(int i=0;i<6;i++){
            if(pfp.contains(String.valueOf((char)('0'+i)))){
                char newChar = (char) ('s'+(int)(Math.random()*6));
                char toReplace = (char) ('0'+i);
                for(int j=0;j<36;j++){
                    if(pfp.charAt(18+j)==toReplace)
                        newColors[j]=newChar;
                }
            }
        }
        for(int i=0;i<6;i++){
            if(pfp.contains(String.valueOf((char)('a'+i)))){
                char newChar = (char) ('a'+(int)(Math.random()*6));
                char toReplace = (char) ('a'+i);
                for(int j=0;j<36;j++){
                    if(pfp.charAt(18+j)==toReplace)
                        newColors[j]=newChar;
                }
            }
        }
        for(int i=0;i<6;i++){
            if(pfp.contains(String.valueOf((char)('g'+i)))){
                char newChar = (char) ('g'+(int)(Math.random()*6));
                char toReplace = (char) ('g'+i);
                for(int j=0;j<36;j++){
                    if(pfp.charAt(18+j)==toReplace)
                        newColors[j]=newChar;
                }
            }
        }
        return new UserData(name,"","",pfp.substring(0,18)+String.join("",new String(newColors)));
    }
    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "back", args -> this.changeScene(new TitleScene()),
            "localplay", args ->{
                this.changeScene(new GameScene(new GameData(-1, "Local Game"),
                        playerGenerator("Player 1"), playerGenerator("Player 2"), false));
            },
            "open", args -> {
                if(args.length==0) return;
                if(args[0].length()<3){
                    try {
                        var gameIndex = Integer.valueOf(args[0], 36);
                        if(gameIndex>=0&&gameIndex<this.myGames.size()){
                            var game = this.myGames.get(gameIndex);
                            this.changeScene(new GameScene(game));

                            if(game.whiteUsername.equals(PlayData.currAuth.username())||
                                    game.blackUsername.equals(PlayData.currAuth.username()))
                                WebsocketManager.sendMessage(new JoinAsPlayerCommand(
                                        PlayData.currAuth.authToken(), game.gameID,
                                        game.whiteUsername.equals(PlayData.selfData.username())?
                                                ChessGame.TeamColor.WHITE: ChessGame.TeamColor.BLACK));
                            else
                                WebsocketManager.sendMessage(new JoinAsObserverCommand(
                                        PlayData.currAuth.authToken(), game.gameID));
                            return;
                        }
                    }catch(Exception ignored){}
                }
                AtomicBoolean foundGame= new AtomicBoolean(false);
                this.myGames.stream().filter(data->data.gameName.equals(args[0])).findFirst()
                        .ifPresent(data->{
                            foundGame.set(true);
                            this.changeScene(new GameScene(data));
                        });
                if(!foundGame.get())
                    this.dialogMessage = "Could not find game";
            },
            "browse", args -> {
                this.browsing=true;
                Online.request(Online.ReqMethod.GET, "game", (String)null)
                        .ifSuccess(data->{
                            try {
                                this.browsableGames.clear();
                                var games = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("games");
                                for(var game : games){
                                    var gameData = Serialization.GSON.fromJson(game.toString(), GameData.class);
                                    this.browsableGames.add(gameData);
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                                PlayMenuScene.this.browsing=false;
                                PlayMenuScene.this.dialogMessage="Failed to parse games";
                            }
                        }).ifError(error->{
                            PlayMenuScene.this.browsing=false;
                        });
            },
            "create", args -> this.createMode=true
    ),ArgConsumer.helpCommandMaker(
            "back", "Returns to the title screen",
            "localplay", "Starts a game locally",
            "open [game]", "Opens the specified game",
            "browse", "Shows all open games to join",
            "create", "Creates a game"
    ));
    private final ArgConsumer createConsumer = new ArgConsumer(Map.of(
            "back", args -> this.createMode=false,
            "name", args -> {
                if(args.length==0) return;
                var tryName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if(tryName.length()>=3&&tryName.length()<=32)
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
                Online.request(Online.ReqMethod.POST, "game", Serialization.GSON.toJson(toSend))
                        .ifSuccess(s -> {
                            this.createMode=false;
                            final int id;
                            try{
                                id = ((Double) Serialization.GSON.fromJson(s, Map.class).get("gameID")).intValue();
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
    private final ArgConsumer browseConsumer = new ArgConsumer(Map.of(
            "back", args -> this.browsing=false,
            "join", args -> {
                if(args.length==0) return;
                try{
                    int index=this.browsingIndex+Integer.parseInt(args[0],36);
                    if(index<0||index>=this.browsableGames.size()) return;

                    var game = this.browsableGames.get(index);
                    Online.request(Online.ReqMethod.PUT, "game",
                            new JoinGameData(game.whiteUsername==null?"WHITE":"BLACK", game.gameID))
                            .ifSuccess(s -> {
                                if(game.whiteUsername==null) game.whiteUsername = PlayData.selfData.username();
                                else game.blackUsername = PlayData.selfData.username();

                                this.changeScene(new GameScene(game));
                            }).ifError(errorMessage -> {
                                PlayMenuScene.this.dialogMessage="Could not join game";
                            });
                }catch(Exception ignored){}
            },
            "watch", args -> {
                if(args.length==0) return;
                try{
                    int index=this.browsingIndex+Integer.parseInt(args[0],36);
                    if(index<0||index>=this.browsableGames.size()) return;

                    var game = this.browsableGames.get(index);
                    Online.request(Online.ReqMethod.PUT, "game",
                                    new JoinGameData("", game.gameID))
                            .ifSuccess(s -> {
                                this.changeScene(new GameScene(game));
                            }).ifError(errorMessage -> {
                                PlayMenuScene.this.dialogMessage="Could not watch game";
                            });
                }catch(Exception ignored){}
            }
    ),ArgConsumer.helpCommandMaker(
            "back", "Returns to the play menu",
            "join [id]", "Joins the specified game, if it is open",
            "watch [id]", "Watches the specified game"
    ));
    @Override
    public void uninit() {

    }

    @Override
    public void onLine(String[] args){
        var consumer = this.consumer;
        if(this.createMode) consumer=this.createConsumer;
        if(this.browsing) consumer=this.browseConsumer;

        consumer.tryConsumeArgs(args);
        if (consumer.shouldShowHelp) this.dialogMessage = consumer.helpCommand;
        
        super.onLine(args);
    }
}
