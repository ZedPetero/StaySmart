package application.model;

import java.sql.Timestamp;

/** One row of the landlord dashboard's "Recent Activity" list. */
public class Activity {
    public enum Kind { APPLICATION, TOUR, MESSAGE, APPROVAL }

    private final Kind kind;
    private final String title;
    private final String subtitle;
    private final Timestamp time;

    public Activity(Kind kind, String title, String subtitle, Timestamp time) {
        this.kind = kind;
        this.title = title;
        this.subtitle = subtitle;
        this.time = time;
    }

    public Kind getKind() { return kind; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public Timestamp getTime() { return time; }
}
