package org.kittelson.interviewsetter2;

public enum ApptViewState {
    TentativeAppts,
    ApptsToConfirm;

    @Override
    public String toString() {
        String str;
        switch (this) {
            case ApptsToConfirm:
                str = "Appts to Confirm";
                break;
            case TentativeAppts:
                str = "Tentative Appts";
                break;
            default:
                str = "Unknown";
                break;
        }
        return str;
    }
}
