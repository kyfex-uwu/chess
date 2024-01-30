package ui;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public class ArgConsumer {
    public abstract static class ArgPasser implements Consumer<String[]> {
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
    public ArgConsumer(Map<String, Consumer<String[]>> args){
        this.consumers = args;
    }
    public ConsumerResponseType tryConsumeArgs(String argsUnsplit){
        return this.tryConsumeArgs(argsUnsplit.split(" "));
    }
    public ConsumerResponseType tryConsumeArgs(String[] args){
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
