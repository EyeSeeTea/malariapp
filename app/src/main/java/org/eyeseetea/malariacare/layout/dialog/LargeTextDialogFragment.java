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

package org.eyeseetea.malariacare.layout.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.eyeseetea.malariacare.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LargeTextDialogFragment extends OkDialogFragment{

    public static DialogFragment newInstance(int title, InputStream inputStream) {
        LargeTextDialogFragment frag = new LargeTextDialogFragment();
        setIcon(android.R.drawable.ic_dialog_alert);
        Bundle args = new Bundle();
        args.putInt("title", title);
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);
        String line="";
        try {
            while (br.ready()) {
                line+=br.readLine()+'\n';
            }
        } catch (IOException io){
            // TODO: catch this exception and show it with a Dialog
        }
        args.putString("message", line);
        frag.setArguments(args);
        return frag;
    }

}
