package org.eyeseetea.malariacare.domain.exception.push;

public class PushReportException extends Exception {
    String message;

    public PushReportException(String message) {
        this.message = message;
        System.out.println(PushReportException.class.getName() + " message " + message);
    }
    public PushReportException(Throwable throwable) {
        throwable.printStackTrace();
        this.message = throwable.getMessage();
        System.out.println(PushReportException.class.getName() + " message " + message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
