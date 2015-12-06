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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer;

/**
 * A simple VO that contains the progress of the pull sync
 * Created by arrizabalaga on 19/11/15.
 */
public class SyncProgressStatus {
    private String message;
    private Exception exception;
    private boolean finish;

    /**
     * Builds a step progress
     * @param message
     */
    public SyncProgressStatus(String message){
        this.message=message;
    }

    /**
     * Builds an error progress
     * @param exception
     */
    public SyncProgressStatus(Exception exception){
        this.exception=exception;
    }

    /**
     * Builds a progress that notifies the process is done
     */
    public SyncProgressStatus(){
        this.finish=true;
    }

    /**
     * Tells if this status represents an ordinary step
     * @return
     */
    public boolean hasProgress(){
        return this.message!=null;
    }

    /**
     * Tells if this status represents an error step
     * @return
     */
    public boolean hasError(){
        return this.exception!=null;
    }

    /**
     * Tells if this status represents a process that has finished
     * @return
     */
    public boolean isFinish(){
        return this.finish;
    }

    /**
     * Getter for the message
     * @return
     */
    public String getMessage(){
        return message;
    }

    /**
     * Getter for the exception
     * @return
     */
    public Exception getException(){
        return exception;
    }
}
