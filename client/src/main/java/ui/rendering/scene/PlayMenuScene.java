package ui.rendering.scene;

import chess.ChessGame;
import chess.ChessPosition;
import chess.Json;
import com.google.gson.JsonParser;
import model.GameData;
import ui.ArgConsumer;
import ui.Config;
import ui.Online;
import ui.PlayData;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Nineslice;

import java.util.ArrayList;
import java.util.Map;

public class PlayMenuScene extends Scene{
    private ArrayList<GameData> games = new ArrayList<>();
    @Override
    public void init() {
        if(PlayData.loggedIn()) {
            Online.request(Online.ReqMethod.GET, "game/" + PlayData.selfData.username(),
                    null)
                    .ifSuccess(data->{
                        try {
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

        this.toRender.add(new Background());
        this.toRender.add(new Renderable(11) {
            @Override
            public void render(Pixel[][] screen) {
                Nineslice.Style.PANEL.nineslice.render(screen,
                        2, 1, 36, PlayMenuScene.this.games.size()+2);
                Sprite.Builder.fromStr(String.join("\n",PlayMenuScene.this.games.stream().map(data->{
                    var toReturn = data.gameName;

                    ChessGame.TeamColor myTeam=ChessGame.TeamColor.WHITE;
                    if(data.whiteUsername!=null&&!data.whiteUsername.equals(PlayData.currAuth.username())){
                            toReturn+=" vs " + data.whiteUsername;
                            myTeam= ChessGame.TeamColor.BLACK;
                    }else if(data.blackUsername!=null&&!data.blackUsername.equals(PlayData.currAuth.username())){
                            toReturn+=" vs " + data.blackUsername;
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

                Nineslice.Style.PANEL.nineslice.render(screen, screen[0].length-18,screen.length-4,
                        16,3,"Local Play");
            }
        });
        super.init();
    }

    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "back", args -> this.changeScene(new TitleScene()),
            "localplay", args ->{
                this.changeScene(new GameScene(new GameData(-1, "Local Game"),
                        "Ooga", "Booga", false));
            }
    ),ArgConsumer.helpCommandMaker(
            "back", "Returns to the title screen",
            "localplay", "Starts a game locally"
    ));
    @Override
    public void uninit() {

    }

    @Override
    public void onLine(String[] args){
        this.consumer.tryConsumeArgs(args);
        if(this.consumer.shouldShowHelp) this.dialogMessage = this.consumer.helpCommand;
        
        super.onLine(args);
    }
}
