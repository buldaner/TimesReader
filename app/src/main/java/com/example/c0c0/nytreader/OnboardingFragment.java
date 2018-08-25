package com.example.c0c0.nytreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.OnboardingSupportFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OnboardingFragment extends OnboardingSupportFragment{
    private SharedPreferencesManager mPrefManager;

    @Nullable
    @Override
    public  View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //get preferences manager
        mPrefManager = SharedPreferencesManager.getInstance(this.getActivity().getApplicationContext());

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getPageCount() {
        return 1;
    }

    @Override
    protected String getPageTitle(int pageIndex) {
        return getString(R.string.app_name);
    }

    @Override
    protected String getPageDescription(int pageIndex) {
        return getString(R.string.bulleted_list);
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
        return null;
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected  void onFinishFragment() {
        super.onFinishFragment();

        //onboarding complete
        mPrefManager.putInt(getString(R.string.setting_onboarding_complete), 1);

        getActivity().finish();
    }
}
