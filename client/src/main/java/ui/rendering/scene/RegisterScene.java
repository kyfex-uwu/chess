package ui.rendering.scene;

import model.AuthData;
import model.LoginData;
import model.UserData;
import ui.ArgConsumer;
import ui.Online;
import ui.PlayData;
import ui.rendering.Color;
import ui.rendering.Pixel;
import ui.rendering.Renderable;
import ui.rendering.renderable.Background;
import ui.rendering.renderable.Nineslice;

import java.util.Map;

public class RegisterScene extends Scene{

    private String username="";
    private String password="";
    private String password2="";
    private String email="";
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
                        screenCenter+2, screen.length-7, 12,5, "Submit");
            }
        });
        this.toRender.add(new Renderable(9) {
            @Override
            public void render(Pixel[][] screen) {
                var halfScreen = screen[0].length/2;
                Nineslice.Style.INPUT.nineslice.render(screen,
                        3, 5, halfScreen-6,3, "Username > "+RegisterScene.this.username);
                Nineslice.Style.INPUT.nineslice.render(screen,
                        halfScreen, 5, halfScreen-6,3, "Email > "+RegisterScene.this.email);
                Nineslice.Style.INPUT.nineslice.render(screen,
                        3, 8, halfScreen-6,3, "Password > "+"*".repeat(RegisterScene.this.password.length()));
                Nineslice.Style.INPUT.nineslice.render(screen,
                        halfScreen, 8, halfScreen-6,3, "Confirm Password > "+"*".repeat(RegisterScene.this.password2.length()));

                if(!RegisterScene.this.password.isEmpty()&&!RegisterScene.this.password.matches(UserData.passwordRegex)){
                    Renderable.overlayPixel(halfScreen-5,9,
                            new Pixel('!',new Color(255,255,255),new Color(255,0,0)), screen);
                }
                if(!RegisterScene.this.password.equals(RegisterScene.this.password2)){
                    Renderable.overlayPixel(halfScreen*2-8,9,
                            new Pixel('!',new Color(255,255,255),new Color(255,0,0)), screen);
                }
            }
        });
    }

    @Override
    public void uninit() {

    }

    private final ArgConsumer consumer = new ArgConsumer(Map.of(
            "submit", args -> {
                if(args.length>=2){
                    this.username = args[0];
                    this.password = args[1];
                }

                if(!this.password.equals(this.password2)){
                    this.dialogMessage="Password does not match";
                    return;
                }
                var data = new UserData(this.username, this.password, this.email);
                if(!data.isValid()){
                    this.dialogMessage = "Invalid login data";
                    return;
                }
                Online.request(Online.ReqMethod.POST, "user",
                                data, AuthData.class)
                        .ifSuccess(authData -> {
                            PlayData.currAuth = authData;
                            Online.request(Online.ReqMethod.GET, "user/"+RegisterScene.this.username,
                                            (String)null, UserData.class)
                                    .ifSuccess(userData -> {
                                        PlayData.selfData = userData;
                                        RegisterScene.this.dialogMessage = "Registered and logged in!";
                                    }).
                                    ifError(error -> {
                                        RegisterScene.this.dialogMessage = "Registered, but "+error.message();
                                    });
                            RegisterScene.this.dialogMessage = "Registered and logged in!";
                        }).
                        ifError(error -> {
                            RegisterScene.this.dialogMessage = error.message();
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
            "confpass", args -> {
                if(args.length>=1)
                    this.password2 = args[0];
            },
            "email", args -> {
                if(args.length>=1)
                    this.email = args[0];
            },

            "back", args -> this.changeScene(new TitleScene())
    ), ArgConsumer.helpCommandMaker(
            "submit","Registers with the given information",
            "username [username]","Sets the username field",
            "password [password]","Sets the password field",
            "confpass [password]","Confirms the password",
            "email [email]","Sets the email field",
            "back","Returns to the title screen"
    ));
    @Override
    public void onLine(String[] args) {
        this.consumer.tryConsumeArgs(args);
        if(this.consumer.shouldShowHelp) this.dialogMessage = this.consumer.helpCommand;

        super.onLine(args);
    }
}
