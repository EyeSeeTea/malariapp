package org.eyeseetea.malariacare.observables;

import java.util.Observable;


public class ObservablePush extends Observable {
    private static ObservablePush instance = new ObservablePush();

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }

    public static ObservablePush getInstance() {
        return instance;
    }
}
