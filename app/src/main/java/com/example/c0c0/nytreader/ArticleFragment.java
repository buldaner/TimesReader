package com.example.c0c0.nytreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ArticleFragment extends Fragment {
    private static final String ARG_POSITION = "position";

    public static ArticleFragment newInstance(int position) {
        Bundle args = new Bundle();

        args.putInt(ARG_POSITION, position);

        ArticleFragment fragment = new ArticleFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        DataManager dataManager = DataManager.getInstance();
        final Article article = dataManager.getArticles().get(getArguments().getInt(ARG_POSITION));

        TextView titleView = rootView.findViewById(R.id.text_title);
        titleView.setText(article.getTitle());

        TextView bylineView = rootView.findViewById(R.id.text_byline);
        bylineView.setText(article.getByline());

        TextView publishedDateView = rootView.findViewById(R.id.text_published_date);
        publishedDateView.setText(article.getPublishedDate());

        TextView abstractView = rootView.findViewById(R.id.text_abstract);
        abstractView.setText(article.getAbstract());

        ImageView previewImageView = rootView.findViewById(R.id.image_preview);
        String previewImageUrl = article.getPreviewImageUrl();

        //tap on the image to load the article
        previewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
                startActivity(browserIntent);
            }
        });

        if(previewImageUrl != null) {
            dataManager.loadImage(previewImageUrl, previewImageView);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
