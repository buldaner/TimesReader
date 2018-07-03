package com.example.c0c0.nytreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
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
import android.widget.ImageView;
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
    private final String API_URL = "http://developer.nytimes.com";
    private final int REQUEST_SLEEP = 5000;

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
    private ImageView mApiImage;
    private SharedPreferencesManager mPrefManager;
    private int mRequestAttempts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get preference manager
        mPrefManager = SharedPreferencesManager.getInstance(getApplicationContext());
        mRequestAttempts = 0;

        //get api branding image
        mApiImage = findViewById(R.id.image_api);

        //per api ToS...
        mApiImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(API_URL));
                startActivity(browserIntent);
            }
        });

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
        refreshArticles();

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
                            mViewPager.setCurrentItem(
                                    mPrefManager.getInt(getString(R.string.var_playing_item))
                            );
                            if(nextClick()) {
                                playClick();
                            }
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
        final int playingItem = mViewPager.getCurrentItem();
        final Article currentArticle = mDataManager.getArticles().get(playingItem);
        final Context ctx = this.getApplicationContext();
        Document document = currentArticle.getDocument();

        //if we need to fetch the article text and parse it and so on, do that now
        if(document == null) {
            StringRequest documentRequest = new StringRequest
                    (Request.Method.GET, currentArticle.getUrl(), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mPrefManager.putInt(getString(R.string.var_playing_item), playingItem);
                            mRequestAttempts = 0;
                            currentArticle.setDocument(Jsoup.parse(response));
                            mTTSManager.playDocument(currentArticle);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            if(mRequestAttempts < 5) {
                                mRequestAttempts++;
                                SystemClock.sleep( REQUEST_SLEEP );
                                playClick();
                            } else {
                                Toast.makeText(ctx
                                        , "There is a problem with the request. Please check" +
                                                " your connection and try again after a few moments. If" +
                                                " problems persist, contact the developer."
                                        , Toast.LENGTH_LONG).show();
                                Log.e(TAG, (e.getMessage() == null ? "playClick" : e.getMessage()));
                            }
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
        int currentItem = mViewPager.getCurrentItem();
        if(currentItem - 1 >= 0) {
            mViewPager.setCurrentItem(currentItem - 1);
        }
    }

    private boolean nextClick() {
        int currentItem = mViewPager.getCurrentItem();
        boolean success = false;

        if(currentItem + 1 < mDataManager.getArticles().size()) {
            mViewPager.setCurrentItem(currentItem + 1);
            success = true;
        }

        return success;
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
                        mPrefManager.putInt(getString(R.string.var_playing_item), 0);
                        mViewPager.setCurrentItem(0);
                        setToolbarTextFromPosition(0);
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

        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                Toast.makeText(getApplicationContext()
                        , "Language and Input -> Text-to-Speech"
                        , Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_american_voice:
                mPrefManager.putString(getString(R.string.setting_voice_locale), "US");
                Toast.makeText(getApplicationContext()
                        , "Voice set to US"
                        , Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_uk_voice:
                mPrefManager.putString(getString(R.string.setting_voice_locale), "UK");
                Toast.makeText(getApplicationContext()
                        , "Voice set to UK"
                        , Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_indian_voice:
                mPrefManager.putString(getString(R.string.setting_voice_locale), "IN");
                Toast.makeText(getApplicationContext()
                        , "Voice set to India"
                        , Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_australian_voice:
                mPrefManager.putString(getString(R.string.setting_voice_locale), "AU");
                Toast.makeText(getApplicationContext()
                        , "Voice set to Australia"
                        , Toast.LENGTH_SHORT).show();
                break;
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
