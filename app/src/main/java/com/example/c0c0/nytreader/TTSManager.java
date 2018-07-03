package com.example.c0c0.nytreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Locale;

public class TTSManager {
    private final String TAG = this.getClass().getName();
    private final String STORY_TEXT_SELECTOR = ".story-body-text, .e2kc3sl0";

    private static TTSManager mInstance;
    private Context mCtx;
    private TextToSpeech mTextToSpeech;
    private SharedPreferences mSharedPreferences;

    public static TTSManager getInstance(Context context
            , UtteranceProgressListener progressListener) {
        if(mInstance == null) {
            mInstance = new TTSManager(context, progressListener);
        }
        return mInstance;
    }

    public static TTSManager getInstance(Context context
        , UtteranceProgressListener progressListener, boolean forceNew) {
        if(mInstance == null || forceNew) {
            mInstance = new TTSManager(context, progressListener);
        }
        return mInstance;
    }

    private TTSManager(final Context context, UtteranceProgressListener progressListener) {
        mCtx = context;
        mSharedPreferences = mCtx.getSharedPreferences(mCtx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.SUCCESS) {
                    Toast.makeText(context
                        , "Text-to-speech is not supported by your device."
                        , Toast.LENGTH_LONG).show();
                }
            }
        });

        mTextToSpeech.setOnUtteranceProgressListener(progressListener);
    }

    private void setLocale() {
        String locale = mSharedPreferences.getString(mCtx.getString(R.string.setting_voice_locale), "US");

        switch(locale) {
            case "US":
                mTextToSpeech.setLanguage(Locale.US);
                break;
            case "UK":
                mTextToSpeech.setLanguage(Locale.UK);
                break;
            case "IN":
                mTextToSpeech.setLanguage(new Locale("en", "IN"));
                break;
            case "AU":
                mTextToSpeech.setLanguage(new Locale("en", "AU"));
                break;
        }
    }

    public TextToSpeech getTextToSpeech() {
        return mTextToSpeech;
    }

    public void playDocument(Article article) {
        Document document = article.getDocument();
        String title = article.getTitle().replace(".", "") + ".";
        String byline = article.getByline().replace(".", "") + ".";
        ArrayList<Element> nodes = document.select(STORY_TEXT_SELECTOR);
        setLocale();

        mTextToSpeech.speak(title
                , TextToSpeech.QUEUE_ADD
                , null
                , "speakArticle");

        mTextToSpeech.speak(byline
                , TextToSpeech.QUEUE_ADD
                , null
                , "speakArticle");

        /*for (Element node : nodes) {
            String text = node.text()
                    .replaceAll("(?i)MR\\.", "MR")
                    .replaceAll("(?i)MS\\.", "MISS")
                    .replaceAll("(?i)MRS\\.", "MRS")
                    .replaceAll("(?i)DR\\.", "DR")
                    .replaceAll("(\\s\\w)\\.", "$1");

            mTextToSpeech.speak(text
                    , TextToSpeech.QUEUE_ADD
                    , null
                    , "speakArticle");
        }*/

        mTextToSpeech.speak(""
                , TextToSpeech.QUEUE_ADD
                , null
                , "articleComplete");
    }
}
