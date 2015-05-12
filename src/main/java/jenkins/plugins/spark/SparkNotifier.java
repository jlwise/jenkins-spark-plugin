package jenkins.plugins.spark;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.plugins.spark.impl.SparkV1Service;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.logging.Logger;

import static jenkins.plugins.spark.NotificationType.STARTED;

@SuppressWarnings({"unchecked"})
public class SparkNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(SparkNotifier.class.getName());
    private String token;
    private String room;
    private boolean startNotification;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;

    private String startJobMessage;
    private String completeJobMessage;

    @DataBoundConstructor
    public SparkNotifier(String token, String room, boolean startNotification, boolean notifySuccess,
            boolean notifyAborted, boolean notifyNotBuilt, boolean notifyUnstable, boolean notifyFailure,
            boolean notifyBackToNormal,
            String startJobMessage, String completeJobMessage) {
        this.token = token;
        this.room = room;
        this.startNotification = startNotification;
        this.notifySuccess = notifySuccess;
        this.notifyAborted = notifyAborted;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifyUnstable = notifyUnstable;
        this.notifyFailure = notifyFailure;
        this.notifyBackToNormal = notifyBackToNormal;

        this.startJobMessage = startJobMessage;
        this.completeJobMessage = completeJobMessage;
    }

    /* notification enabled disabled setters/getters */

    public boolean isStartNotification() {
        return startNotification;
    }

    public void setStartNotification(boolean startNotification) {
        this.startNotification = startNotification;
    }

    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    public void setNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
    }

    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    public void setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
    }

    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public void setNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
    }

    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

    public void setNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
    }

    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    public void setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
    }

    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    public void setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
    }

    /* notification message configurations */

    public String getStartJobMessage() {
        return startJobMessage;
    }

    public void setStartJobMessage(String startJobMessage) {
        this.startJobMessage = startJobMessage;
    }


    public String getCompleteJobMessage() {
        return completeJobMessage;
    }

    public void setCompleteJobMessage(String completeJobMessage) {
        this.completeJobMessage = completeJobMessage;
    }

    /* Default notification messages for UI */

    public String getStartJobMessageDefault() {
        return Messages.JobStarted();
    }

    public String getCompleteJobMessageDefault() {
        return Messages.JobCompleted();
    }

    public String getRoom() {
        return StringUtils.isBlank(room) ? getDescriptor().getRoom() : room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        //This is here to ensure that the reported build status is actually correct. If we were to return false here,
        //other build plugins could still modify the build result, making the sent out Spark notification incorrect.
        return true;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        logger.fine("Creating build start notification");
        publishNotificationIfEnabled(STARTED, build);

        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        logger.fine("Creating build completed notification");
        Result result = build.getResult();
        Result previousResult = findPreviousBuildResult(build);

        NotificationType notificationType = NotificationType.fromResults(previousResult, result);
        publishNotificationIfEnabled(notificationType, build);

        return true;
    }

    private Result findPreviousBuildResult(AbstractBuild<?,?> build) {
        do {
            build = build.getPreviousBuild();
            if (build == null || build.isBuilding()) {
                return null;
            }
        } while (build.getResult() == Result.ABORTED || build.getResult() == Result.NOT_BUILT);
        return build.getResult();
    }

    private void publishNotificationIfEnabled(NotificationType notificationType, AbstractBuild<?, ?> build) {
        if (isNotificationEnabled(notificationType)) {
            getSparkService().publish(notificationType.getMessage(build, this), notificationType.getColor());
        }
    }

    private boolean isNotificationEnabled(NotificationType type) {
        switch (type) {
            case ABORTED:
                return notifyAborted;
            case BACK_TO_NORMAL:
                return notifyBackToNormal;
            case FAILURE:
                return notifyFailure;
            case NOT_BUILT:
                return notifyNotBuilt;
            case STARTED:
                return startNotification;
            case SUCCESS:
                return notifySuccess;
            case UNSTABLE:
                return notifyUnstable;
            default:
                return false;
        }
    }

    private SparkService getSparkService() {
        DescriptorImpl desc = getDescriptor();
        String authToken = Util.fixEmpty(token) != null ? token : desc.getToken();
        return getSparkService(desc.getServer(), authToken,
                StringUtils.isBlank(room) ? desc.getRoom() : room, desc.getSendAs());
    }

    private static SparkService getSparkService(String server, String token, String room,
            String sendAs) {        
            return new SparkV1Service(server, token, room, sendAs);
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String server = "api.spark.com";
        private String token;
        private String room;
        private String sendAs = "Jenkins";
        private static int testNotificationCount = 0;

        public DescriptorImpl() {
            load();
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRoom() {
            return room;
        }

        public void setRoom(String room) {
            this.room = room;
        }

        public String getSendAs() {
            return sendAs;
        }

        public void setSendAs(String sendAs) {
            this.sendAs = sendAs;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
            request.bindJSON(this, formData);

            save();
            return super.configure(request, formData);
        }

        public FormValidation doSendTestNotification(@QueryParameter("spark.server") String server,
                @QueryParameter("spark.room") String room, @QueryParameter("spark.sendAs") String sendAs) {
            SparkService service = getSparkService(server, token,  room, sendAs);
            service.publish(Messages.TestNotification(++testNotificationCount), "yellow");
            return FormValidation.ok(Messages.TestNotificationSent());
        }

        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }
    }

    /**
     * The settings defined here have been moved to the {@link SparkNotifier} configuration (shows up under the Post
     * Build task view).
     *
     * @deprecated The plugin configuration should be stored in {@link SparkNotifier}. This class only exists, so
     * configurations can be migrated for the build jobs.
     */
    @Deprecated
    public static class SparkJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {
        private final String room;
        private final boolean startNotification;
        private final boolean notifySuccess;
        private final boolean notifyAborted;
        private final boolean notifyNotBuilt;
        private final boolean notifyUnstable;
        private final boolean notifyFailure;
        private final boolean notifyBackToNormal;


        @DataBoundConstructor
        public SparkJobProperty(String room,
                                  boolean startNotification,
                                  boolean notifyAborted,
                                  boolean notifyFailure,
                                  boolean notifyNotBuilt,
                                  boolean notifySuccess,
                                  boolean notifyUnstable,
                                  boolean notifyBackToNormal) {
            this.room = room;
            this.startNotification = startNotification;
            this.notifyAborted = notifyAborted;
            this.notifyFailure = notifyFailure;
            this.notifyNotBuilt = notifyNotBuilt;
            this.notifySuccess = notifySuccess;
            this.notifyUnstable = notifyUnstable;
            this.notifyBackToNormal = notifyBackToNormal;
        }

        @Exported
        public String getRoom() {
            return room;
        }

        @Exported
        public boolean getStartNotification() {
            return startNotification;
        }

        @Exported
        public boolean getNotifySuccess() {
            return notifySuccess;
        }

        @Exported
        public boolean getNotifyAborted() {
            return notifyAborted;
        }

        @Exported
        public boolean getNotifyFailure() {
            return notifyFailure;
        }

        @Exported
        public boolean getNotifyNotBuilt() {
            return notifyNotBuilt;
        }

        @Exported
        public boolean getNotifyUnstable() {
            return notifyUnstable;
        }

        @Exported
        public boolean getNotifyBackToNormal() {
            return notifyBackToNormal;
        }

        @Extension
        public static final class DescriptorImpl extends JobPropertyDescriptor {
            public String getDisplayName() {
                return "Spark Notifications";
            }

            @Override
            public boolean isApplicable(Class<? extends Job> jobType) {
                return true;
            }

            @Override
            public SparkJobProperty newInstance(StaplerRequest sr, JSONObject formData) throws hudson.model.Descriptor.FormException {
                return new SparkJobProperty(sr.getParameter("sparkProjectRoom"),
                        sr.getParameter("sparkStartNotification") != null,
                        sr.getParameter("sparkNotifyAborted") != null,
                        sr.getParameter("sparkNotifyFailure") != null,
                        sr.getParameter("sparkNotifyNotBuilt") != null,
                        sr.getParameter("sparkNotifySuccess") != null,
                        sr.getParameter("sparkNotifyUnstable") != null,
                        sr.getParameter("sparkNotifyBackToNormal") != null);
            }
        }
    }
}
