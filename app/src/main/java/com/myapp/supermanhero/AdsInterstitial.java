package com.myapp.supermanhero;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Arrays;
import java.util.List;

public class AdsInterstitial {

    private InterstitialAd interstitialAd;
    //private FirebaseUtiti firebaseUtiti;

    public AdsInterstitial(String device, String id_ads, Context context){
        //this.firebaseUtiti = firebaseUtiti;
        List<String> testDeviceIds = Arrays.asList(device);
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);

        // Create a full screen content callback.
        FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                // Proceed to the next level.
                //firebaseUtiti.updateDBAds("Interstitial", Constant.APPNAME,"interstitials onAdDismissedFullScreenContent ");
            }
            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when fullscreen content failed to show.
                //firebaseUtiti.updateDBAds("Interstitial",Constant.APPNAME,"the ad onAdFailedToShowFullScreenContent "+adError.getMessage());
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                // Make sure to set your reference to null so you don't
                // show it a second time.
                interstitialAd = null;
                //firebaseUtiti.updateDBAds("Interstitial",Constant.APPNAME,"onAdShowedFullScreenContent");
            }
        };

        InterstitialAd.load(
                context,
                id_ads,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        interstitialAd.setOnPaidEventListener(onPaidEventListener);
                        interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                        //firebaseUtiti.updateDBAds("Interstitial",Constant.APPNAME,
                        //        "InterstitialAdLoadCallback "+ad.getResponseInfo().getResponseId() );
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        //firebaseUtiti.updateDBAds("onAdFailedToLoad ",Constant.APPNAME,
                        //        "the ad failed to load "+adError.getMessage());
                    }
                });
    }

    public void showAds(Activity act){
        if (interstitialAd != null) {
            interstitialAd.show(act);
        } else {
            //firebaseUtiti.updateDBAds("Interstitial",Constant.APPNAME,"interstitials ads null");
        }
    }

    OnPaidEventListener onPaidEventListener = adValue -> {
        //firebaseUtiti.updateDBAds("Interstitial", Constant.APPNAME,
         //       "Paided "+adValue.getCurrencyCode()+" "+adValue.getValueMicros()+" "+adValue.getPrecisionType() );
    };
}
