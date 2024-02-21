package env;

public class Environment {
    public static final String wsScheme;
    public static final String baseUrl;
    public static final String httpScheme;

    private static final EnvType type = EnvType.DEV;
    static{
        wsScheme=type.wsScheme;
        baseUrl=type.baseURL;
        httpScheme=type.httpScheme;
    }

    private enum EnvType{
        DEV("ws://", "http://", "localhost:6000/"),
        PROD("wss://", "https://", "kyfexuwu.com/chess/");

        private final String wsScheme;
        private final String httpScheme;
        private final String baseURL;
        EnvType(String wsScheme, String httpScheme, String baseURL){
            this.wsScheme=wsScheme;
            this.httpScheme=httpScheme;
            this.baseURL=baseURL;
        }
    }
}
