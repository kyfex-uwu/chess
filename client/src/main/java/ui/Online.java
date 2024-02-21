package ui;

import chess.Json;
import env.Environment;
import model.Data;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.function.Consumer;

public class Online {
    public enum ReqMethod{
        GET("GET"),
        POST("POST"),
        DELETE("DELETE"),
        PUT("PUT");
        public final String method;
        ReqMethod(String method){
            this.method=method;
        }
    }
    public static class Response<T>{
        public record ErrorMessage(String message){}

        public final ErrorMessage errorMessage;
        public final T data;
        private Response(ErrorMessage errorMessage){
            this.errorMessage = errorMessage;
            this.data=null;
        }
        private Response(T data){
            this.errorMessage =null;
            this.data=data;
        }
        public Response<T> ifError(Consumer<ErrorMessage> errorHandler){
            if(this.errorMessage!=null) errorHandler.accept(this.errorMessage);
            return this;
        }
        public Response<T> ifSuccess(Consumer<T> successHandler){
            if(this.data!=null) successHandler.accept(this.data);
            return this;
        }
    }
    public static class EmptyData implements Data{
        @Override
        public boolean isValid() {
            return true;
        }
    }
    public static <T extends Data> Response<T> request(ReqMethod method, String endpoint,
                                                       Data toSend, Class<T> expectedType){
        return request(method, endpoint, Json.GSON.toJson(toSend), expectedType);
    }
    public static Response<String> request(ReqMethod method, String endpoint, Data toSend){
        return request(method, endpoint, Json.GSON.toJson(toSend));
    }
    public static <T extends Data> Response<T> request(ReqMethod method, String endpoint,
                                                       String toSend, Class<T> expectedType){
        var toReturn = request(method, endpoint, toSend);
        if(toReturn.data!=null){
            try{
                return new Response<>(Json.GSON.fromJson(toReturn.data, expectedType));
            }catch(Exception e){
                return new Response<>(new Response.ErrorMessage("Could not parse server response json"));
            }
        }else{
            return new Response<>(toReturn.errorMessage);
        }
    }
    public static Response<String> request(ReqMethod method, String endpoint, String toSend){
        if(method==ReqMethod.GET&&toSend!=null){
            System.out.println("you cant get with body bozo");
            new Exception().printStackTrace();
        }
        try{
            // Specify the desired endpoint
            URI uri = new URI(Environment.httpScheme + Environment.baseUrl+endpoint);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            connection.setRequestMethod(method.method);
            connection.setDoOutput(true);
            if(PlayData.currAuth!=null)
                connection.addRequestProperty("authorization", PlayData.currAuth.authToken());

            if(toSend!=null)
                try (var outputStream = connection.getOutputStream()) {
                    outputStream.write(toSend.getBytes());
                }

            // Make the request
            connection.connect();

            if(connection.getResponseCode()/100!=2){
                try{
                    return new Response<>(Json.GSON.fromJson(
                            new String(connection.getErrorStream().readAllBytes()),
                            Response.ErrorMessage.class));
                }catch(Exception ignored){}

                return new Response<>(new Response.ErrorMessage("Could not parse server error json"));
            }

            // Output the response body
            try (InputStream respBody = connection.getInputStream()) {
                try{
                    return new Response<>(new String(respBody.readAllBytes()));
                }catch(Exception ignored){}

                return new Response<>(new Response.ErrorMessage("Could not parse server response string"));
            }
        }catch(Exception e){
            e.printStackTrace();
            return new Response<>(new Response.ErrorMessage("Something went wrong"));
        }
    }
}
