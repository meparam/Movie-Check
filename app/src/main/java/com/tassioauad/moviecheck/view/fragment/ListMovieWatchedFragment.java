package com.tassioauad.moviecheck.view.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tassioauad.moviecheck.MovieCheckApplication;
import com.tassioauad.moviecheck.R;
import com.tassioauad.moviecheck.dagger.ListMovieWatchedViewModule;
import com.tassioauad.moviecheck.model.entity.MovieWatched;
import com.tassioauad.moviecheck.presenter.ListMovieWatchedPresenter;
import com.tassioauad.moviecheck.view.ListMovieWatchedView;
import com.tassioauad.moviecheck.view.activity.MovieProfileActivity;
import com.tassioauad.moviecheck.view.adapter.MovieWatchedListAdapter;
import com.tassioauad.moviecheck.view.adapter.OnItemClickListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ListMovieWatchedFragment extends Fragment implements ListMovieWatchedView {

    @Bind(R.id.recyclerview_moviewatched)
    RecyclerView recyclerViewMovieWatched;
    @Bind(R.id.linearlayout_anyfounded)
    LinearLayout linearLayoutAnyFounded;
    MovieWatchedListAdapter movieWatchedListAdapter;

    @Inject
    ListMovieWatchedPresenter presenter;
    private List<MovieWatched> movieWatchedList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MovieCheckApplication) getActivity().getApplication()).getObjectGraph()
                .plus(new ListMovieWatchedViewModule(this)).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listmoviewatched, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.loadMovieInterests();

        Tracker defaultTracker = ((MovieCheckApplication) getActivity().getApplication()).getDefaultTracker();
        defaultTracker.setScreenName("List Movie Watched by the User Screen");
        defaultTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static ListMovieWatchedFragment newInstance() {
        ListMovieWatchedFragment fragment = new ListMovieWatchedFragment();
        return fragment;
    }

    @Override
    public void showWatchedMovies(final List<MovieWatched> movieWatchedList) {
        this.movieWatchedList = movieWatchedList;
        linearLayoutAnyFounded.setVisibility(View.GONE);
        recyclerViewMovieWatched.setVisibility(View.VISIBLE);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false);
        recyclerViewMovieWatched.setLayoutManager(layoutManager);
        recyclerViewMovieWatched.setItemAnimator(new DefaultItemAnimator());
        movieWatchedListAdapter = new MovieWatchedListAdapter(movieWatchedList, new OnItemClickListener<MovieWatched>() {
            @Override
            public void onClick(MovieWatched movieWatched, View view) {
                startActivity(MovieProfileActivity.newIntent(getActivity(), movieWatched.getMovie()));
            }

            @Override
            public void onLongClick(final MovieWatched movieWatched, final View view) {
                new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.DialogLight)).setItems(new CharSequence[]{getActivity().getString(R.string.listmoviewatchedfragment_remove)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                removeWatchedMovie(movieWatched);
                                break;
                        }
                    }
                }).create().show();
            }
        });
        recyclerViewMovieWatched.setAdapter(movieWatchedListAdapter);
    }

    private void removeWatchedMovie(MovieWatched movieWatched) {
        movieWatchedListAdapter.remove(movieWatchedList.indexOf(movieWatched));
        presenter.remove(movieWatched);
        movieWatchedList.remove(movieWatched);
    }

    @Override
    public void warnAnyWatchedMovieFounded() {
        recyclerViewMovieWatched.setVisibility(View.GONE);
        linearLayoutAnyFounded.setVisibility(View.VISIBLE);
    }
}
