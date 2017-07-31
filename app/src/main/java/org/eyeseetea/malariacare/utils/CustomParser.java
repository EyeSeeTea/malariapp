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

package org.eyeseetea.malariacare.data.remote.api;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.View;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.views.URLDrawable;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

/**
 * Created by arrizabalaga on 25/09/15.
 */
public class CustomParser implements Html.ImageGetter, Html.TagHandler {
    Context c;
    View container;

    /**
     * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
     * and on top of Stack is the most nested list
     */
    Stack<String> lists = new Stack<String>();
    /**
     * Tracks indexes of ordered lists so that after a nested list ends
     * we can continue with correct index of outer list
     */
    Stack<Integer> olNextIndex = new Stack<Integer>();
    /**
     * List indentation in pixels. Nested lists use multiple of this.
     */
    private static final int indent = 10;
    private static final int listItemIndent = indent * 2;
    private static final BulletSpan bullet = new BulletSpan(indent);

    private static final String TAG=".URLImageParser";

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     * @param t
     * @param c
     */
    public CustomParser(View t, Context c) {
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

    // Code from here got from https://bitbucket.org/Kuitsi/android-textview-html-list (Thanks Kuitsi) to interprete lists in HTML
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("ul")) {
            if (opening) {
                lists.push(tag);
            } else {
                lists.pop();
            }
        } else if (tag.equalsIgnoreCase("ol")) {
            if (opening) {
                lists.push(tag);
                olNextIndex.push(Integer.valueOf(1)).toString();//TODO: add support for lists starting other index than 1
            } else {
                lists.pop();
                olNextIndex.pop().toString();
            }
        } else if (tag.equalsIgnoreCase("li")) {
            if (opening) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                    output.append("\n");
                }
                String parentList = lists.peek();
                if (parentList.equalsIgnoreCase("ol")) {
                    start(output, new Ol());
                    output.append(olNextIndex.peek().toString() + ". ");
                    olNextIndex.push(Integer.valueOf(olNextIndex.pop().intValue() + 1));
                } else if (parentList.equalsIgnoreCase("ul")) {
                    start(output, new Ul());
                }
            } else {
                if (lists.peek().equalsIgnoreCase("ul")) {
                    if ( output.length() > 0 && output.charAt(output.length() - 1) != '\n' ) {
                        output.append("\n");
                    }
                    // Nested BulletSpans increases distance between bullet and text, so we must prevent it.
                    int bulletMargin = indent;
                    if (lists.size() > 1) {
                        bulletMargin = indent-bullet.getLeadingMargin(true);
                        if (lists.size() > 2) {
                            // This get's more complicated when we add a LeadingMarginSpan into the same line:
                            // we have also counter it's effect to BulletSpan
                            bulletMargin -= (lists.size() - 2) * listItemIndent;
                        }
                    }
                    BulletSpan newBullet = new BulletSpan(bulletMargin);
                    end(output,
                            Ul.class,
                            new LeadingMarginSpan.Standard(listItemIndent * (lists.size() - 1)),
                            newBullet);
                } else if (lists.peek().equalsIgnoreCase("ol")) {
                    if ( output.length() > 0 && output.charAt(output.length() - 1) != '\n' ) {
                        output.append("\n");
                    }
                    int numberMargin = listItemIndent * (lists.size() - 1);
                    if (lists.size() > 2) {
                        // Same as in ordered lists: counter the effect of nested Spans
                        numberMargin -= (lists.size() - 2) * listItemIndent;
                    }
                    end(output,
                            Ol.class,
                            new LeadingMarginSpan.Standard(numberMargin));
                }
            }
        } else {
            if (opening) Log.d("TagHandler", "Found an unsupported tag " + tag);
        }
    }

    /** @see android.text.Html */
    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spanned.SPAN_MARK_MARK);
    }

    /** Modified from {@link android.text.Html} */
    private static void end(Editable text, Class<?> kind, Object... replaces) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);
        text.removeSpan(obj);
        if (where != len) {
            for (Object replace: replaces) {
                text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return;
    }
    // Code until here got from https://bitbucket.org/Kuitsi/android-textview-html-list (Thanks Kuitsi) to interprete lists in HTML

    /** @see android.text.Html */
    private static Object getLast(Spanned text, Class<?> kind) {
		/*
		 * This knows that the last returned object from getSpans()
		 * will be the most recently added.
		 */
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        }
        return objs[objs.length - 1];
    }

    private static class Ul { }
    private static class Ol { }

    private void processUl(boolean opening, Editable output){

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
                CustomParser.this.container.invalidate();
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


        private InputStream fetch(String urlString) throws IOException {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urlString)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().byteStream();
        }
    }
}
