package com.example.c0c0.nytreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.OnboardingSupportFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OnboardingFragment extends OnboardingSupportFragment{
    private SharedPreferencesManager mPrefManager;
    private View mContentView;

    private final int[] titles = {
            R.string.var_onboarding_title_1,
            R.string.var_onboarding_title_2
    };

    private final int[] descriptions = {
            R.string.var_onboarding_description_1,
            R.string.var_onboarding_description_2
    };

    private final int[] content = {
            R.string.var_onboarding_content_1,
            R.string.var_onboarding_content_2
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //get preferences manager
        mPrefManager = SharedPreferencesManager.getInstance(this.getActivity().getApplicationContext());
        //setArrowColor(getResources().getColor(R.color.colorAccent));
        //setDotBackgroundColor(getResources().getColor(R.color.colorAccent));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getPageCount() {
        return 2;
    }

    @Override
    protected String getPageTitle(int pageIndex) {
        return getString(titles[pageIndex]);
    }

    @Override
    protected String getPageDescription(int pageIndex) {
        return getString(descriptions[pageIndex]);
    }

    @Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container) {
        View bgView = new View(getActivity());
        bgView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        return bgView;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        mContentView = inflater.inflate(R.layout.text_onboarding, container, false);
        return mContentView;
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected void onPageChanged(final int newPage, int previousPage) {
        TextView text = getActivity().findViewById(R.id.text_onboarding);
        text.setText(content[newPage]);
    }

    @Override
    protected  void onFinishFragment() {
        super.onFinishFragment();

        //onboarding complete
        mPrefManager.putInt(getString(R.string.setting_onboarding_complete), 1);

        getActivity().finish();
    }
}
