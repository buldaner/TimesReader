package com.example.c0c0.nytreader;

import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Article {
    private final String TAG = this.getClass().getName();

    private DataManager mDataManager;

    private String mUrl;
    private String mSection;
    private String mSubsection;
    private String mTitle;
    private String mByline;
    private String mAbstract;
    private Date mPublishedDate;
    private String mFormattedPublishedDateString;
    private SimpleDateFormat mDateParser;
    private SimpleDateFormat mDateFormatter;
    private JSONArray mMultimedia;
    private String mPreviewImageUrl;
    private Document mDocument;

    public Article(String mUrl, String mSection, String mSubsection, String mTitle
            , String mByline, String mAbstract, String mPublishedDate, JSONArray multimedia) {
        this.mDataManager = DataManager.getInstance();

        this.mUrl = mUrl;
        this.mSection = mSection;
        this.mSubsection = mSubsection;
        this.mTitle = mTitle;
        this.mByline = mByline;
        this.mAbstract = mAbstract;
        this.mDateParser = new SimpleDateFormat("yyyy-mm-dd");
        this.mDateFormatter = new SimpleDateFormat("MMMM d, yyyy");
        this.mMultimedia = multimedia;

        //format dates for front end
        try {
            this.mPublishedDate = mDateParser.parse(mPublishedDate);
            this.mFormattedPublishedDateString = this.mDateFormatter.format(this.mPublishedDate);
        } catch(java.text.ParseException e) {
            this.mFormattedPublishedDateString = "";
            Log.e(TAG, e.getMessage());
        }

        //look for correct image
        setArticlePreviewUrl();
    }

    private void setArticlePreviewUrl() {
        for(int i = 0; i < mMultimedia.length(); i++) {
            try {
                JSONObject mm = (JSONObject) mMultimedia.get(i);

                if(mm.getString("type").equals("image")
                        && mm.getString("format").equals("superJumbo")) {
                    mPreviewImageUrl = mm.getString("url");
                    break;
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public String getSection() {
        return mSection;
    }

    public String getSubsection() {
        return mSubsection;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getByline() {
        return mByline;
    }

    public String getAbstract() {
        return mAbstract;
    }

    public String getPublishedDate() {
        return mFormattedPublishedDateString;
    }

    public String getPreviewImageUrl() {
        return mPreviewImageUrl;
    }

    public Document getDocument() {
        return mDocument;
    }

    public void setDocument(Document document) {
        mDocument = document;
    }
}
