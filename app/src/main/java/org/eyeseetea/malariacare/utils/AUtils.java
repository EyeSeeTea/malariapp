/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.HeaderDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.layout.utils.QuestionRow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public abstract class AUtils {
    public static String round(float base, int decimalPlace) {
        String numberSigns = "";
        for (int i = 0; i < decimalPlace; i++) {
            numberSigns += "0";
        }

        DecimalFormat df = new DecimalFormat("0." + numberSigns);
        return df.format(base);
    }

    public static float safeParseFloat(String floatStr) {
        try {
            return Float.parseFloat(floatStr);
        } catch (NumberFormatException nfe) {
            Log.d("AUtils",
                    String.format("Error when parsing string %s to float number", floatStr));
            return 0f;
        }
    }

    public static List<BaseModel> convertTabToArrayCustom(TabDB tab) {
        List<BaseModel> result = new ArrayList<BaseModel>();

        for (HeaderDB header : tab.getHeaders()) {
            result.add(header);
            for (QuestionDB question : header.getQuestions()) {
                if (tab.getType().equals(Constants.TAB_AUTOMATIC) || tab.getType().equals(
                        Constants.TAB_AUTOMATIC_NON_SCORED) || question.hasChildren()) {
                    result.add(question);
                }
            }
        }

        return result;
    }

    public static List preloadTabItems(TabDB tab, String module) {
        List<? extends BaseModel> items;

        if (tab.isCompositeScore()) {
            items = CompositeScoreDB.listByProgram(Session.getSurveyByModule(module).getProgram());
        } else {

            items = Session.getTabsCache().get(tab.getId_tab());

            if (items == null) {
                items = convertTabToArrayCustom(tab);
            }
            Session.getTabsCache().put(tab.getId_tab(), items);
        }

        return compressTabItems(items);
    }

    /**
     * Turns a list of headers, questions into a list of headers, questions and questionRows.
     */
    public static List compressTabItems(List items) {
        List<Object> compressedItems = new ArrayList<>();
        Iterator<Object> iterator = items.iterator();
        QuestionRow lastRow = null;
        while (iterator.hasNext()) {
            Object item = iterator.next();

            //Header
            if (item instanceof HeaderDB) {
                compressedItems.add(item);
                continue;
            }

            //Normal question
            if (item instanceof QuestionDB && !((QuestionDB) item).belongsToCustomTab()) {
                compressedItems.add(item);
                continue;
            }

            //Custom tabs questions/titles
            QuestionDB question = (QuestionDB) item;
            //Question that belongs to a customtab
            if (question.isCustomTabNewRow()) {
                lastRow = new QuestionRow();
                compressedItems.add(lastRow);
            }
            lastRow.addQuestion(question);
        }
        return compressedItems;
    }

    public static StringBuilder convertFromInputStreamToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (IOException e) {
            Log.d("AUtils", String.format("Error reading inputStream [%s]", inputStream));
            e.printStackTrace();
        }

        return stringBuilder;
    }


    /**
     * This method check if the Internet conexion is active
     *
     * @return return true if all is correct.
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) PreferencesState.getInstance().getContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        return netInfo.isConnected();
    }

    /**
     * Shows an alert dialog with a big message inside based on a raw resource HTML formatted
     *
     * @param titleId Id of the title resource
     * @param rawId   Id of the raw text resource in HTML format
     */
    public static void showAlertWithHtmlMessage(int titleId, int rawId, Context context) {
        InputStream message = context.getResources().openRawResource(rawId);
        String stringMessage = AUtils.convertFromInputStreamToString(message).toString();
        final SpannableString linkedMessage = new SpannableString(Html.fromHtml(stringMessage));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

        showAlertWithLogoAndVersion(titleId, linkedMessage, context);
    }

    /**
     * Shows an alert dialog with a big message inside based on a raw resource
     *
     * @param titleId Id of the title resource
     * @param rawId   Id of the raw text resource
     */
    public static void showAlertWithMessage(int titleId, int rawId, Context context) {
        InputStream message = context.getResources().openRawResource(rawId);
        showAlertWithLogoAndVersion(titleId,
                AUtils.convertFromInputStreamToString(message).toString(), context);
    }

    /**
     * Shows an alert dialog with a big message inside based on a raw resource HTML formatted
     *
     * @param titleId Id of the title resource
     * @param rawId   Id of the raw text resource in HTML format
     */
    public static void showAlertWithHtmlMessageAndLastCommit(int titleId, int rawId,
            Context context) {
        String stringMessage = getMessageWithCommit(rawId, context);
        final SpannableString linkedMessage = new SpannableString(Html.fromHtml(stringMessage));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

        showAlertWithLogoAndVersion(titleId, linkedMessage, context);
    }

    public static String getCommitHash(Context context) {
        String stringCommit;
        //Check if lastcommit.txt file exist, and if not exist show as unavailable.
        int layoutId = context.getResources().getIdentifier("lastcommit", "raw",
                context.getPackageName());
        if (layoutId == 0) {
            stringCommit = context.getString(R.string.unavailable);
        } else {
            InputStream commit = context.getResources().openRawResource(layoutId);
            stringCommit = AUtils.convertFromInputStreamToString(commit).toString();
        }
        return stringCommit;
    }

    /**
     * Merge the lastcommit into the raw file
     *
     * @param rawId Id of the raw text resource in HTML format
     */
    public static String getMessageWithCommit(int rawId, Context context) {
        InputStream message = context.getResources().openRawResource(rawId);
        String stringCommit = getCommitHash(context);
        String stringMessage = AUtils.convertFromInputStreamToString(message).toString();
        if (stringCommit.contains(context.getString(R.string.unavailable))) {
            stringCommit = String.format(context.getString(R.string.lastcommit), stringCommit);
            stringCommit = stringCommit + " " + context.getText(R.string.lastcommit_unavailable);
        } else {
            stringCommit = String.format(context.getString(R.string.lastcommit), stringCommit);
        }
        stringMessage = String.format(stringMessage, stringCommit);
        return stringMessage;
    }

    public static void showAlert(int titleId, CharSequence text, Context context) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(titleId))
                .setMessage(text)
                .setNeutralButton(android.R.string.ok, null).create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(
                LinkMovementMethod.getInstance());
    }

    public static void showAlertWithLogoAndVersion(int titleId, CharSequence text,
            Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_about);
        dialog.setTitle(titleId);
        dialog.setCancelable(true);

        //set up text title
        TextView textTile = (TextView) dialog.findViewById(R.id.aboutTitle);
        textTile.setText(BuildConfig.FLAVOR.toUpperCase() + "(dev) " + BuildConfig.VERSION_NAME);
        textTile.setGravity(Gravity.RIGHT);

        //set up text title
        TextView textContent = (TextView) dialog.findViewById(R.id.aboutMessage);
        textContent.setMovementMethod(LinkMovementMethod.getInstance());
        textContent.setText(text);
        //set up button
        Button button = (Button) dialog.findViewById(R.id.aboutButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //now that the dialog is set up, it's time to show it
        dialog.show();
    }

    /**
     * Shows an alert dialog asking for acceptance of the announcement. If ok calls the accept the
     * annoucement, do nothing otherwise
     */
    public static void showAnnouncement(int titleId, String message, final Context context) {
        final SpannableString linkedMessage = new SpannableString(Html.fromHtml(message));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

        final UserDB loggedUser = UserDB.getLoggedUser();
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(titleId))
                .setMessage(linkedMessage)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferencesState.getInstance().setUserAccept(true);
                        checkUserClosed(loggedUser, context);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkUserClosed(loggedUser, context);
                    }
                }).create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(
                LinkMovementMethod.getInstance());
    }

    public static void checkUserClosed(UserDB user, Context context) {
        if (isUserClosed(user)) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PreferencesState.getInstance().setUserAccept(false);
                    DashboardActivity.dashboardActivity.executeLogout();
                }
            };
            closeUser(R.string.admin_announcement,
                    PreferencesState.getInstance().getContext().getString(R.string.user_close),
                    context, listener);
        }
    }

    private static boolean isUserClosed(UserDB user) {
        return user.getCloseDate() != null && user.getCloseDate().before(new Date());
    }

    /**
     * Shows an alert dialog asking for acceptance of the announcement. If ok calls the accept the
     * annoucement, do nothing otherwise
     */
    public static void closeUser(int titleId, String message, final Context context,
            DialogInterface.OnClickListener listener) {
        SpannableString linkedMessage = new SpannableString(Html.fromHtml(message));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(titleId))
                .setMessage(linkedMessage)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, listener).show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(
                LinkMovementMethod.getInstance());
    }


    public static String escapeQuotes(String value) {
        return value.replace("'", "\\"+"\'").replace("\"", "\\"+"\"");
    }
}
