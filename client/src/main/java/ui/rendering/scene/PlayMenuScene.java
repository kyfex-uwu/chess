package ui.rendering.scene;

import chess.ChessGame;
import chess.ChessPosition;
import chess.Json;
import com.google.gson.JsonParser;
import model.GameData;
import ui.Online;
import ui.PlayData;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.Sprite;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Nineslice;

import java.util.ArrayList;

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
                        2, 1, 36, PlayMenuScene.this.games.size()+4);
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
                }).toList())).build().draw(3,2,screen);
            }
        });
        super.init();
    }

    @Override
    public void uninit() {

    }
}
