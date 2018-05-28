package com.example.c0c0.nytreader;

import android.content.Intent;
import android.provider.Settings;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private Toolbar mToolbar;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private DataManager mDataManager;
    private RequestManager mRequestManager;
    private TTSManager mTTSManager;
    private FloatingActionButton mPlayButton;
    private FloatingActionButton mStopButton;
    private FloatingActionButton mPrevButton;
    private FloatingActionButton mNextButton;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPlayingItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get toolbar
        mToolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);

        //get play button
        mPlayButton = findViewById(R.id.fab_play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                playClick();
            }
        });
        mPlayButton.show();

        //get stop button
        mStopButton = findViewById(R.id.fab_stop);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                stopClick();
            }
        });
        mStopButton.hide();

        //get previous button
        mPrevButton = findViewById(R.id.fab_prev);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                previousClick();
            }
        });

        //get next button
        mNextButton = findViewById(R.id.fab_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                nextClick();
            }
        });

        //get refresh layout
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshArticles();
                    }
                }
        );

        //create request manager
        mRequestManager = RequestManager.getInstance(getApplicationContext());

        //create text-to-speech manager
        mTTSManager = TTSManager.getInstance(getApplicationContext(), getProgressListener());

        //create viewpager adapter
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //create data manager
        mDataManager = DataManager.getInstance(getApplicationContext());

        //fetch articles
        mDataManager.fetchArticleInformation(
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mDataManager.loadArticleInformation(response);
                    mSectionsPagerAdapter.notifyDataSetChanged();
                    setToolbarTextFromPosition(0);
                }
            }
        );

        //get pager from layout
        mViewPager = findViewById(R.id.container);

        //hack to make interaction with swipeRefreshLayout less shitty
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled( int position, float v, int i1 ) {
            }

            @Override
            public void onPageSelected( int position ) {
                setToolbarTextFromPosition(position);
            }

            @Override
            public void onPageScrollStateChanged( int state ) {
                enableDisableSwipeRefresh( state == ViewPager.SCROLL_STATE_IDLE );
            }
        } );

        //attach adapter to pager
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    private void setToolbarTextFromPosition(int position) {
        mToolbar.setTitle(mDataManager.getArticles().get(position).getSection());
        mToolbar.setSubtitle(mDataManager.getArticles().get(position).getSubsection());
    }

    //this is where we control what happens when speaking stops and starts. by default, let's have
    //the app scroll to the next page and start reading the next article when the current one is
    //finished.
    private UtteranceProgressListener getProgressListener() {
        return new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // On speech start.
            }

            @Override
            public void onDone(final String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(utteranceId.equals("articleComplete")) {
                            nextClick();
                        }
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                // There was an error.
            }
        };
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

    private void playClick() {
        mPlayingItem = mViewPager.getCurrentItem();
        final Article currentArticle = mDataManager.getArticles().get(mPlayingItem);
        Document document = currentArticle.getDocument();

        //if we need to fetch the article text and parse it and so on, do that now
        if(document == null) {
            StringRequest documentRequest = new StringRequest
                    (Request.Method.GET, currentArticle.getUrl(), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            currentArticle.setDocument(Jsoup.parse(response));
                            mTTSManager.playDocument(currentArticle);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, error.getMessage());
                        }
                    });

            mRequestManager.addToRequestQueue(documentRequest);
        } else {
            mTTSManager.playDocument(currentArticle);
        }

        mPlayButton.hide();
        mStopButton.show();
    }

    private void stopClick() {
        if(mTTSManager.getTextToSpeech().isSpeaking()) {
            mTTSManager.getTextToSpeech().stop();
        }

        mStopButton.hide();
        mPlayButton.show();
    }

    private void previousClick() {
        if(mPlayingItem - 1 >= 0) {
            mViewPager.setCurrentItem(mPlayingItem - 1);
            stopClick();
            playClick();
        }
    }

    private void nextClick() {
        if(mPlayingItem + 1 <= mDataManager.getArticles().size() - 1) {
            mViewPager.setCurrentItem(mPlayingItem + 1);
            stopClick();
            playClick();
        }
    }

    private void refreshArticles() {
        //fetch articles
        mDataManager.fetchArticleInformation(
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mDataManager.loadArticleInformation(response);
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        mViewPager.setCurrentItem(0);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            Toast.makeText(getApplicationContext()
                    , "Language and Input -> Text-to-Speech"
                    , Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ArticleFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // show one page per article.
            return mDataManager.getArticles().size();
        }
    }
}
