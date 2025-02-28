package com.example.netflixplus.retrofitAPI;


import com.example.netflixplus.entities.MediaResponseDTO;

import java.util.List;
import java.util.UUID;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.DELETE;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Headers;

public interface RetrofitInterface {


    @Multipart
    @POST("media/upload")
    Call<ResponseBody> uploadVideo(
            @Part MultipartBody.Part videoFile,
            @Part MultipartBody.Part thumbnail,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("genre") RequestBody genre,
            @Part("year") RequestBody year,
            @Part("publisher") RequestBody publisher,
            @Part("duration") RequestBody duration
    );


    @POST("media/authenticate")
    Call<Void> authenticate();


    @GET("media")
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    Call<List<MediaResponseDTO>> getAllMedia();



    @GET("media/genre/{genre}")
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    Call<List<MediaResponseDTO>> getMediaByGenre(@Path("genre") String genre);



    @GET("media/title/{title}")
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    Call<List<MediaResponseDTO>> getMediaByTitle(@Path("title") String title);



    @GET("media/{id}")
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    Call<MediaResponseDTO> getMediaById(@Path("id") UUID id);



    @DELETE("media/title/{title}")
    Call<Void> deleteMediaByTitle(@Path("title") String title);



    @DELETE("media/{id}")
    Call<Void> deleteById(@Path("id") UUID id);



    @DELETE("media/title/{title}")
    Call<Void> deleteByTitle(@Path("title") String title);
}