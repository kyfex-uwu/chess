package ui.rendering.scene;

import model.AuthData;
import model.LoginData;
import model.UserData;
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
    private String username="";
    private String password="";
    @Override
    public void init() {
        super.init();
        this.toRender.add(new Background());
        this.toRender.add(new Renderable(11) {
            @Override
            public void render(Pixel[][] screen) {
                var screenCenter = screen[0].length/2;
                Nineslice.Style.NAV_PANEL.nineslice.render(screen,
                        screenCenter-11, screen.length-7, 10,5, "Back");
                Nineslice.Style.PANEL.nineslice.render(screen,
                        screenCenter+1, screen.length-7, 12,5, "Log In");
                Nineslice.Style.NAV_PANEL.nineslice.render(screen,
                        screen[0].length-18, screen.length-7, 14,5, "Register");
            }
        });
        this.toRender.add(new Renderable(9) {
            @Override
            public void render(Pixel[][] screen) {
                if(PlayData.loggedIn())
                    Nineslice.Style.PANEL.nineslice.render(screen,
                            3, 1, 6+11+16, 3, "Logged in: "+PlayData.currAuth.username());
                Nineslice.Style.INPUT.nineslice.render(screen,
                        3, 5, screen[0].length-6,3, "Username > "+LoginScene.this.username);
                Nineslice.Style.INPUT.nineslice.render(screen,
                        3, 9, screen[0].length-6,3, "Password > "+"*".repeat(LoginScene.this.password.length()));
            }
        });
    }

    @Override
    public void uninit() {

    }

    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "login", args -> {
                if(args.length>=2){
                    this.username = args[0];
                    this.password = args[1];
                }

                var data = new LoginData(this.username, this.password);
                if(!data.isValid()){
                    this.dialogMessage = "Invalid login data";
                    return;
                }
                Online.request(Online.ReqMethod.POST, "session",
                        data, AuthData.class)
                        .ifSuccess(authData -> {
                            PlayData.currAuth = authData;
                            LoginScene.this.dialogMessage = "Logged in!";
                            PlayData.save();
                            Online.request(Online.ReqMethod.GET, "user"+LoginScene.this.username,
                                            (String)null, UserData.class)
                                    .ifSuccess(userData -> {
                                        PlayData.selfData = userData;
                                    }).ifError(error -> {
                                        LoginScene.this.dialogMessage = "Logged in - "+error.message();
                                    });
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

            "register", args -> this.changeScene(new RegisterScene()),
            "back", args -> this.changeScene(new TitleScene())
    ), ArgConsumer.helpCommandMaker(
            "login","Logs in with the given username and password",
            "login [username] [password]","Shorthand for setting the credentials, then logging in",
            "username [username]","Sets the username field",
            "password [password]","Sets the password field",
            "register","Opens the register screen",
            "back","Returns to the title screen"
    ));
    @Override
    public void onLine(String[] args) {
        this.consumer.tryConsumeArgs(args);
        if(this.consumer.shouldShowHelp) this.dialogMessage = this.consumer.helpCommand;

        super.onLine(args);
    }
}
