package hr.tvz.android.podcastclient;

import hr.tvz.android.podcastclient.Model.EpisodeHolder;
import hr.tvz.android.podcastclient.Model.Response;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ListenApi {
    @Headers("X-ListenAPI-Key: 113d27d99cff4255b92d85e86e00584e")
    @GET("search")
    Call<Response> getResults(@Query(value = "q") String term,
                              @Query(value = "sort_by_date") int num,
                              @Query(value = "type") String type);

    @Headers("X-ListenAPI-Key: 113d27d99cff4255b92d85e86e00584e")
    @GET("podcasts/{id}")
    Call<EpisodeHolder> getEpisodes(@Path("id") String id, @Query(value = "sort") String term);

}
