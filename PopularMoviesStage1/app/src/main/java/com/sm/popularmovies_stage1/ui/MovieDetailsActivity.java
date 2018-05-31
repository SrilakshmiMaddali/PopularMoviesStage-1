package com.sm.popularmovies_stage1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

import com.sm.popularmovies_stage1.BuildConfig;
import com.sm.popularmovies_stage1.R;
import com.sm.popularmovies_stage1.model.MoviedbService;
import com.sm.popularmovies_stage1.model.Movies;
import com.sm.popularmovies_stage1.model.PopularMoviesDto;
import com.sm.popularmovies_stage1.model.RetrofitClientInstance;
import com.sm.popularmovies_stage1.model.TrailerAdapter;
import com.sm.popularmovies_stage1.model.TrailerVideo;
import com.sm.popularmovies_stage1.model.VideoDataDto;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Details view page, to display movie details.
 */
public class MovieDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";
    protected static final String NA = "Not Available";
    TextView movieTitle;
    ImageView moviePosterImage;
    List<TrailerVideo> mTrailerVideoList;
    TrailerAdapter mTrailerAdapter;
    ListView mTrailerList;
    Context mContext;
    Movies mUserElectedMovie;
    private static final String TAG = "MovieDetailsActivity";


    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detailed_activity);
        ButterKnife.bind(this);
        mContext = this;
        moviePosterImage = (ImageView) findViewById(R.id.movie_poster);
        movieTitle = (TextView) findViewById(R.id.title);
        mTrailerList = (ListView) findViewById(R.id.trailer_list);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        // get parcelable data from intent.
        mUserElectedMovie = (Movies) intent.getParcelableExtra(EXTRA_MOVIE);
        if (mUserElectedMovie == null) {
            // EXTRA_POSITION not found in intent
            closeOnError();
            return;
        }

        // initiate resources.
        populateUI(mUserElectedMovie);
        Picasso.with(this)
                .load(getPath(mUserElectedMovie.getmPosterPath()))
                .fit()
                .into(moviePosterImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        // image loading finished, so remove progressbar.
                        //mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        // On error , while loading sandwich image, display error message.
                        moviePosterImage.setVisibility(View.INVISIBLE);
                        //mProgressBar.setVisibility(View.GONE);
                        movieTitle.setText(getString(R.string.detail_image_error_message));
                        movieTitle.setVisibility(View.VISIBLE);
                    }
                });

        getMovieList(getVideoTrailerUrl());

        setTitle(mUserElectedMovie.getmOriginalTitle());
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

    // Prepares poster url.
    private String getPath(String posterPath) {
        String url = "http://image.tmdb.org/t/p/" + "w500/" + posterPath;
        return url;
    }

    private void populateUI(@NonNull Movies movie) {
        // Load views, from model object
       // mProgressBar = (ProgressBar) findViewById(R.id.loadingprogress);
        //mProgressBar.setVisibility(View.VISIBLE);

        TextView originalTitleTextView = (TextView) findViewById(R.id.original_title_tv);
        String originalTitle = movie.getmOriginalTitle();
        if (!originalTitle.isEmpty()) {
            originalTitleTextView.setText(originalTitle);
        } else {
            originalTitleTextView.setText(NA);
        }

        TextView releaseDateTextView = (TextView) findViewById(R.id.release_date_tv);
        String releaseDate = movie.getmReleaseDate();
        if (!releaseDate.isEmpty()) {
            releaseDateTextView.setText(releaseDate);
        } else {
            releaseDateTextView.setText(NA);
        }

        RatingBar userRatingTextView = (RatingBar) findViewById(R.id.user_rating_tv);
        String userRating = movie.getmPopularity();
        if (!userRating.isEmpty()) {
            userRatingTextView.setRating(Float.parseFloat(userRating));
        } else {
            userRatingTextView.setRating(0);
        }

        TextView movieOverview = (TextView)findViewById(R.id.movie_synopsis_tv);
        String overview = movie.getmOverview();
        if (!overview.isEmpty()) {
            movieOverview.setText(overview);
        } else {
            movieOverview.setText(NA);
        }
    }

    private void getMovieList(Call<VideoDataDto> call) {

        call.enqueue(new retrofit2.Callback<VideoDataDto>() {
            @Override
            public void onResponse(@NonNull Call<VideoDataDto> call, @NonNull Response<VideoDataDto> response) {
                //progressDoalog.dismiss();
                if (response != null && response.body() != null) {
                    mTrailerVideoList = response.body().getResults();
                    generateDataList(mTrailerVideoList);
                } else {
                    Log.d(TAG, "null Popular movies response.");
                    Toast.makeText(MovieDetailsActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VideoDataDto> call, @NonNull Throwable t) {
                //progressDialog.dismiss();
                Toast.makeText(MovieDetailsActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Call<VideoDataDto> getVideoTrailerUrl() {
        MoviedbService service = RetrofitClientInstance.getRetrofitInstance().create(MoviedbService.class);
        return service.getVideosList(mUserElectedMovie.getmId(),BuildConfig.API_KEY);
    }

    private void generateDataList(List<TrailerVideo> moviesList) {
        mTrailerAdapter = new TrailerAdapter(mContext, mTrailerVideoList);
        mTrailerList.setAdapter(mTrailerAdapter);
        mTrailerList.invalidateViews();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // relase all resources.
        moviePosterImage = null;
        movieTitle = null;
        //mProgressBar = null;
    }
}