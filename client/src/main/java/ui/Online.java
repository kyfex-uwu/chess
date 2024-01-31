package ui;

import chess.Json;
import model.Data;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Base64;
import java.util.function.Consumer;

public class Online {
    private static final String baseUrl = "http://localhost:6000/";
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
    public static class Response<T extends Data>{
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
        if(method==ReqMethod.GET&&toSend!=null){
            System.out.println("you cant get with body bozo");
            new Exception().printStackTrace();
        }
        try{
            // Specify the desired endpoint
            URI uri = new URI(baseUrl+endpoint);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            connection.setRequestMethod(method.method);
            connection.setDoOutput(true);
            if(PlayData.currAuth!=null)
                connection.addRequestProperty("Authorization", PlayData.currAuth.authToken());

            if(toSend!=null)
                try (var outputStream = connection.getOutputStream()) {
                    outputStream.write(Json.GSON.toJson(toSend).getBytes());
                }

            // Make the request
            connection.connect();

            if(connection.getResponseCode()/100!=2){
                try{
                    return new Response<>(Json.GSON.fromJson(
                            new String(connection.getErrorStream().readAllBytes()),
                            Response.ErrorMessage.class));
                }catch(Exception ignored){}

                return new Response<>(new Response.ErrorMessage("Could not process server error"));
            }

            // Output the response body
            try (InputStream respBody = connection.getInputStream()) {
                var bodyStr = new String(respBody.readAllBytes());

                try{
                    return new Response<>(Json.GSON.fromJson(bodyStr, expectedType));
                }catch(Exception ignored){}

                return new Response<>(new Response.ErrorMessage("Could not parse server response"));
            }
        }catch(Exception e){
            e.printStackTrace();
            return new Response<>(new Response.ErrorMessage("Something went wrong"));
        }
    }
}
