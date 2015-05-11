package jenkins.plugins.spark;

public class SparkNotifierBuilder {
    private String token = "token";
    private String room = "room";
    private boolean startNotification = false;
    private boolean notifySuccess = false;
    private boolean notifyAborted = true;
    private boolean notifyNotBuilt = false;
    private boolean notifyUnstable = true;
    private boolean notifyFailure = true;
    private boolean notifyBackToNormal = true;
    private String messageJobStarted;
    private String messageJobCompleted;

    public static SparkNotifierBuilder builder() {
        return new SparkNotifierBuilder();
    }

    public SparkNotifierBuilder setToken(String token) {
        this.token = token;
        return this;
    }

    public SparkNotifierBuilder setRoom(String room) {
        this.room = room;
        return this;
    }

    public SparkNotifierBuilder setStartNotification(boolean startNotification) {
        this.startNotification = startNotification;
        return this;
    }

    public SparkNotifierBuilder setNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
        return this;
    }

    public SparkNotifierBuilder setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
        return this;
    }

    public SparkNotifierBuilder setNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
        return this;
    }

    public SparkNotifierBuilder setNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
        return this;
    }

    public SparkNotifierBuilder setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
        return this;
    }

    public SparkNotifierBuilder setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
        return this;
    }

    public SparkNotifier build() {
        return new SparkNotifier(token, room, startNotification, notifySuccess, notifyAborted, notifyNotBuilt,
                notifyUnstable, notifyFailure, notifyBackToNormal, messageJobStarted, messageJobCompleted);
    }

    public SparkNotifierBuilder setMessageJobStarted(String messageJobStarted) {
        this.messageJobStarted = messageJobStarted;
        return this;
    }

    public SparkNotifierBuilder setMessageJobCompleted(String messageJobCompleted) {
        this.messageJobCompleted = messageJobCompleted;
        return this;
    }
}