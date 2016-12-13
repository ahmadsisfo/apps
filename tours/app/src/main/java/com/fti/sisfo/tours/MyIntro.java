package com.fti.sisfo.tours;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;

public class MyIntro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add your slide's fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(SampleSlide.newInstance(R.layout.intro));
        addSlide(SampleSlide.newInstance(R.layout.intro2));
        addSlide(SampleSlide.newInstance(R.layout.intro3));
        addSlide(SampleSlide.newInstance(R.layout.intro4));
        //addSlide(second_fragment);
       // addSlide(third_fragment);
        //addSlide(fourth_fragment);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        //addSlide(AppIntroFragment.newInstance("Welcome", "Welcome to Tours App for you", R.drawable.ic_menu_camera, R.color.colorAccent));
        //addSlide(AppIntroFragment.newInstance("Best Objek", "We recommend you with the best place", R.drawable.ic_menu_send, R.color.colorPrimary));
        //addSlide(AppIntro2Fragment.newInstance("Welcome!", "We just need some permissions to start. (This is only as an example...this app doesn't utilize any of the perms.)\n", R.drawable.ic_slide1, Color.parseColor("#2196F3")));
        //addSlide(AppIntro2Fragment.newInstance("Camera", "We need to use the camera.\n", R.drawable.ic_slide2, Color.parseColor("#2196F3")));

        showStatusBar(true);
        setZoomAnimation();
        // OPTIONAL METHODS
        // Override bar/separator color.
        //setBarColor(Color.parseColor("#3F51B5"));
        //setSeparatorColor(Color.parseColor("#2196F3"));
        setProgressIndicator();
        // Hide Skip/Done button.
        //showSkipButton(false);
        //showDoneButton(true);

        showSkipButton(false);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permisssion in Manifest.
        //setVibrate(true);
        //setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        loadMainActivity();
    }

    public void getStarted(View v) {
        loadMainActivity();
    }
}