package org.eyeseetea.malariacare.fragments;

import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

public class WebViewInterceptor {

    @android.webkit.JavascriptInterface
    public void log(){
        System.out.println("Event on javascript detected");
    }

    @android.webkit.JavascriptInterface
    public void onClick(String json){
        Toast.makeText(PreferencesState.getInstance().getContext(), "\"onClick detected:\" + value"+json, Toast.LENGTH_SHORT).show();
    }
}
