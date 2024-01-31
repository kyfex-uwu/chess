package ui.rendering.screen;

import model.AuthData;
import model.LoginData;
import ui.ArgConsumer;
import ui.Config;
import ui.Online;
import ui.PlayData;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Nineslice;

import java.util.Map;

public class LoginScene extends Scene{
    private static Nineslice field = new Nineslice("""
            \s/~\\\s
            \s   \s
            \s\\~/\s""", ()->Config.Palette.INPUT_ACCENT, ()->Config.Palette.INPUT_MAIN, ()->Config.Palette.INPUT_ACCENT)
            .floatText(Nineslice.FloatDir.LEFT);
    private String username="";
    private String password="";
    @Override
    public void init() {
        this.toRender.add(new Background());
        this.toRender.add(new Renderable(10) {
            @Override
            public void render(Pixel[][] screen) {
                var screenCenter = screen[0].length/2;
                Nineslice.Style.NAV_PANEL.nineslice.render(screen,
                        screenCenter-11, screen.length-7, 10,5, "Back");
                Nineslice.Style.PANEL.nineslice.render(screen,
                        screenCenter+1, screen.length-7, 12,5, "Log In");
            }
        });
        this.toRender.add(new Renderable(9) {
            @Override
            public void render(Pixel[][] screen) {
                if(PlayData.currAuth.isValid())
                    Nineslice.Style.PANEL.nineslice.render(screen,
                            3, 1, 6+11+16, 3, "Logged in: "+PlayData.currAuth.username());
                field.render(screen,
                        3, 5, screen[0].length-6,3, "Username | "+LoginScene.this.username);
                field.render(screen,
                        3, 9, screen[0].length-6,3, "Password | "+"*".repeat(LoginScene.this.password.length()));
                if(LoginScene.this.dialogMessage !=null)
                    Nineslice.Style.DIALOG.nineslice.render(screen,
                            (screen[0].length-LoginScene.this.dialogMessage.length()-6)/2,13,
                            LoginScene.this.dialogMessage.length()+6,3, LoginScene.this.dialogMessage);
            }
        });
    }

    @Override
    public void uninit() {

    }

    private String dialogMessage=null;
    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "login", args -> {
                if(args.length>=2){
                    this.username = args[0];
                    this.password = args[1];
                }

                Online.request(Online.ReqMethod.POST, "session",
                        new LoginData(this.username, this.password), AuthData.class)
                        .ifSuccess(authData -> {
                            PlayData.currAuth = authData;
                            LoginScene.this.dialogMessage = "Logged in!";
                        }).
                        ifError(error -> {
                            LoginScene.this.dialogMessage = error.message();
                        });
            },
            "username", args -> {
                if(args.length>=1)
                    this.username = args[0];
            },
            "password", args -> {
                if(args.length>=1)
                    this.password = args[0];
            },
            "back", args -> {
                this.changeScene(new TitleScene());
            }
    ));
    @Override
    public void onLine(String[] args) {
        this.consumer.tryConsumeArgs(args);

        super.onLine(args);
        this.dialogMessage =null;
    }
}
