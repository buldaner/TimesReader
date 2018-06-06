package com.example.c0c0.nytreader;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DataManager {
    private final String BASE_API = "https://api.nytimes.com/svc/topstories/v2/home.json";
    private final String TAG = this.getClass().getName();

    private static DataManager mInstance;
    private Context mCtx;
    private ArrayList<Article> mArticles;

    public static DataManager getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DataManager(context);
        }
        return mInstance;
    }

    public static DataManager getInstance() {
        return mInstance;
    }

    private DataManager(Context context) {
        mCtx = context;
        mArticles = new ArrayList();
    }

    public void fetchArticleInformation(Response.Listener<JSONObject> onFetch) {
        //initialize request object
        String url = BASE_API + "?api-key=c1ffc89c59334bdbb6c74b4914976a40";

        JsonObjectRequest articleRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, onFetch, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, (error.getMessage() == null ? "API fetch request failure" : error.getMessage()));
                    }
                });

        RequestManager.getInstance(mCtx).addToRequestQueue(articleRequest);
    }

    public void loadArticleInformation(JSONObject response) {
        try {
            mArticles.clear();
            JSONArray results = response.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject thisArticle = results.getJSONObject(i);

                String url = thisArticle.getString("url");
                String section = thisArticle.getString("section");
                String subsection = thisArticle.getString("subsection");
                String title = thisArticle.getString("title");
                String byline = thisArticle.getString("byline");
                String _abstract = thisArticle.getString("abstract");
                String publishedDate = thisArticle.getString("published_date");
                JSONArray multimedia = thisArticle.getJSONArray("multimedia");

                Article thisArticleInformation = new Article(mCtx,
                    url, section, subsection, title, byline, _abstract, publishedDate, multimedia
                );

                mArticles.add(thisArticleInformation);
            }
        } catch ( JSONException e ) {
            Toast.makeText(mCtx
                    , "There has been an error. Please contact the developer."
                    , Toast.LENGTH_LONG).show();
            Log.e(TAG, (e.getMessage() == null ? "loadArticleInformation" : e.getMessage()));
        }
    }

    public ArrayList<Article> getArticles() {
        return mArticles;
    }

    public void loadImage(String url, ImageView view) {
        Picasso.get().load(url).fit().centerCrop().into(view);
    }
}
