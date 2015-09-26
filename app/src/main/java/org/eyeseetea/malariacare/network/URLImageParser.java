/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.network;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eyeseetea.malariacare.views.URLDrawable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by arrizabalaga on 25/09/15.
 */
public class URLImageParser implements Html.ImageGetter {
    Context c;
    View container;

    private static final String TAG=".URLImageParser";

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     * @param t
     * @param c
     */
    public URLImageParser(View t, Context c) {
        this.c = c;
        this.container = t;
    }

    public Drawable getDrawable(String source) {
        URLDrawable urlDrawable = new URLDrawable();

        // get the actual source
        ImageGetterAsyncTask asyncTask =
                new ImageGetterAsyncTask( urlDrawable);

        asyncTask.execute(source);

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        return urlDrawable;
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;
        String source;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            this.source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            try {
                // set the correct bound according to the result from HTTP call
                urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0
                        + result.getIntrinsicHeight());

                // change the reference of the current drawable to the result
                // from the HTTP call
                urlDrawable.setDrawable(result);

                // redraw the image by invalidating the container
                URLImageParser.this.container.invalidate();
            }catch (Exception ex){
                Log.e(TAG, String.format("onPostExecute(%s): %s",source, ex.toString()));
            }
        }

        /***
         * Get the Drawable from URL
         * @param urlString
         * @return
         */
        public Drawable fetchDrawable(String urlString) {
            try {
                Drawable drawable;
                if(urlString.startsWith("http")){
                    drawable=fetchExternalDrawable(urlString);
                }else{
                    String simpleUrlString=urlString.replace("file:///android_asset/","");
                    drawable=Drawable.createFromStream(c.getAssets().open(simpleUrlString), null);
                }
                drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0
                        + drawable.getIntrinsicHeight());
                return drawable;
            } catch (Exception e) {
                Log.e(TAG, String.format("fetchDrawable(%s): %s",urlString, e.toString()));
                return null;
            }
        }

        /***
         * Get the Drawable from URL
         * @param urlString
         * @return
         */
        public Drawable fetchExternalDrawable(String urlString) {
            try {
                InputStream is = fetch(urlString);
                return Drawable.createFromStream(is, "src");
            } catch (Exception e) {
                Log.e(TAG, String.format("fetchDrawable(%s): %s",urlString, e.toString()));
                return null;
            }
        }


        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }
}
