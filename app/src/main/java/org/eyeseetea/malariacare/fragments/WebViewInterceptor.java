package org.eyeseetea.malariacare.fragments;

import org.eyeseetea.malariacare.domain.entity.ServerClassification;

public class WebViewInterceptor {

    public interface BubbleClickListener{
        void onClickMultipleSurveys(String uidList);
        void onClickSingleSurvey(String uidList);
    }

    BubbleClickListener mBubbleClickListener;

    @android.webkit.JavascriptInterface
    public void clickLog() {
        System.out.println("Event on javascript detected");
    }

    @android.webkit.JavascriptInterface
    public void passUidList(String uidList) {
        if(mBubbleClickListener!=null) {
            mBubbleClickListener.onClickMultipleSurveys(uidList);
        }
    }

    @android.webkit.JavascriptInterface
    public void moveToFeedback(String uidList) {
        if(mBubbleClickListener!=null) {
            mBubbleClickListener.onClickSingleSurvey(uidList);
        }
    }

    public void setBubbleClickListener(
            BubbleClickListener bubbleClickListener) {
        mBubbleClickListener = bubbleClickListener;
    }
}
