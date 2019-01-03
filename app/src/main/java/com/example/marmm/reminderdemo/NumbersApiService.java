package com.example.marmm.reminderdemo;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit userguide at
 * https://square.github.io/retrofit/
 * More usage information at
 * https://guides.codepath.com/android/consuming-apis-with-retrofit
 */
public interface NumbersApiService {
    String BASE_URL = "http://numbersapi.com/";
    /**
     * Create a retrofit client.
     */
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    /**
     * The string in the GET annotation is added to the BASE_URL.
     * It simply represents the designed layout of the URLs of the numbersapi.com website,
     * refer to it in a browser and try. This request will deliver a json stream based on month and
     * day of month. It will be put in a DayQuoteTime object by Retrofit.
     */
    @GET("/{number}/trivia?json")
    /**
     * "DayQuoteTime" is the name of the helper class just defined, defining the datamodel, and given as argument.
     * "getTodaysQuote" is the name of the symbol get method. It can be chosen at wish, as long as it is invoked
     * with the same name.
     */
    Call<DayQuoteItem> getTodaysQuote(@Path("number") int monthNumber);
}
