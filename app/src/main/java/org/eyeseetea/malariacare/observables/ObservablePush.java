package org.eyeseetea.malariacare.observables;

import java.util.Observable;


public class ObservablePush extends Observable {
    private static ObservablePush instance = new ObservablePush();

    public void pushFinish() {
        synchronized (this) {
            setChanged();
            notifyObservers();
        }
    }

    public static ObservablePush getInstance() {
        return instance;
    }
}
