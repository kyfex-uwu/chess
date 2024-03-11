package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ArgConsumer {
    public abstract static class ArgPasser implements Consumer<String[]> {
        //does not work!!
        private final ArgConsumer next;
        private final Runnable onRun;
        public ArgPasser(Runnable onRun, ArgConsumer next){
            this.next=next;
            this.onRun=onRun;
        }
        public ArgPasser(ArgConsumer next){ this(()->{}, next); }
        @Override
        public void accept(String[] nextArgs) {

        }
    }
    public enum ConsumerResponseType{
        NONE_FOUND,
        ONE_FOUND,
        MANY_FOUND;
    }

    private final Map<String, Consumer<String[]>> consumers;
    public String helpCommand;
    public boolean shouldShowHelp=false;
    public static LinkedHashMap<String, String> helpCommandMaker(String... strings){
        var toReturn = new LinkedHashMap<String, String>();
        for(int i=0;i<strings.length;i+=2)
            toReturn.put(strings[i], strings[i+1]);
        return toReturn;
    }
    public ArgConsumer(Map<String, Consumer<String[]>> args, String helpCommand){
        args=new HashMap<>(args);
        if(!args.containsKey("help")) args.put("help",args2 -> this.shouldShowHelp=true);
        this.consumers = args;
        this.helpCommand = helpCommand;
    }
    public ArgConsumer(Map<String, Consumer<String[]>> args, Map<String, String> helpMap){
        this(args, "commands:\n"+String.join("\n",helpMap.entrySet().stream()
                .map(entry->" - "+entry.getKey()+(entry.getValue()==null?"":": "+entry.getValue())).toList()));
    }
    private static Map<String, String> helpCommandTo(Map<String, Consumer<String[]>> args){
        Map<String, String> mapToGive = new HashMap<>();
        for(var key : args.keySet())
            mapToGive.put(key, null);
        return mapToGive;
    }
    public ArgConsumer(Map<String, Consumer<String[]>> args){
        this(args,helpCommandTo(args));
    }
    public ConsumerResponseType tryConsumeArgs(String[] args){
        this.shouldShowHelp=false;
        if(args.length<1||args[0].isEmpty()) return ConsumerResponseType.NONE_FOUND;

        ArrayList<String> maybeKeys = new ArrayList<>();
        for(var key : this.consumers.keySet())
            if(key.startsWith(args[0])) maybeKeys.add(key);

        if(maybeKeys.size()==0){
            return ConsumerResponseType.NONE_FOUND;
        }else if(maybeKeys.size()==1){
            String[] newArgs = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            this.consumers.get(maybeKeys.get(0)).accept(newArgs);
            return ConsumerResponseType.ONE_FOUND;
        }else{
            return ConsumerResponseType.MANY_FOUND;
        }
    }
}
