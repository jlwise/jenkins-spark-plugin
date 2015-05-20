package jenkins.plugins.spark;

import com.google.common.base.Preconditions;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Result;
//import hudson.Util;
import hudson.util.LogTaskListener;
import hudson.util.VariableResolver;

import com.cisco.dft.cd.spark.intg.util.Util;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.newHashMap;
import static hudson.Util.replaceMacro;
import static java.util.logging.Level.INFO;

public enum NotificationType {

    STARTED("orange", true) {
        @Override
        protected String getStatus() {
            return Messages.Started();
        }
    },
    ABORTED("gray") {
        @Override
        protected String getStatus() {
            return Messages.Aborted();
        }
    },
    SUCCESS("green") {
        @Override
        protected String getStatus() {
            return Messages.Success();
        }
    },
    FAILURE("red") {
        @Override
        protected String getStatus() {
            return Messages.Failure();
        }
    },
    NOT_BUILT("gray") {
        @Override
        protected String getStatus() {
            return Messages.NotBuilt();
        }
    },
    BACK_TO_NORMAL("green") {
        @Override
        protected String getStatus() {
            return Messages.BackToNormal();
        }
    },
    UNSTABLE("yellow") {
        @Override
        protected String getStatus() {
            return Messages.Unstable();
        }
    },
    UNKNOWN("purple") {
        @Override
        protected String getStatus() {
            throw new IllegalStateException();
        }
    };

    private static final Logger LOGGER = Logger.getLogger(NotificationType.class.getName());
    private final String color;
    private final boolean isStartType;

    private NotificationType(String color, boolean isStartType) {
        this.color = color;
        this.isStartType = isStartType;
    }

    private NotificationType(String color) {
        this(color, false);
    }

    protected abstract String getStatus();

    public String getColor() {
        return color;
    }

    public final String getMessage(AbstractBuild<?, ?> build, SparkNotifier notifier) {
        String format = getTemplateFor(notifier);
        Map<String, String> messageVariables = collectParametersFor(build);

        return replaceMacro(format, new VariableResolver.ByMap<String>(messageVariables));
    }

    private String getTemplateFor(SparkNotifier notifier) {
        String userConfig;
        String defaultConfig;
        if (isStartType) {
            userConfig = notifier.getStartJobMessage();
            defaultConfig = Messages.JobStarted();
        } else {
            userConfig = notifier.getCompleteJobMessage();
            defaultConfig = Messages.JobCompleted();
        }
        return defaultConfig;
        // if (Util.fixEmptyAndTrim(userConfig) == null) {
        //     Preconditions.checkNotNull(defaultConfig, "Default template not set for %s", this);
        //     return defaultConfig;
        // } else {
        //     return userConfig;
        // }
    }

    private Map<String, String> collectParametersFor(AbstractBuild<?, ?> build) {
        Map<String, String> merged = newHashMap();
        merged.putAll(build.getBuildVariables());
        merged.putAll(getEnvVars(build));

        String cause = NotificationTypeUtils.getCause(build);
        String changes = NotificationTypeUtils.getChanges(build);
        String[] projectName = build.getProject().getFullDisplayName().split(" » ");
        if(projectName.length > 2) 
            merged.put("JOB_SHORT_NAME", projectName[projectName.length - 2] + "/" + projectName[projectName.length - 1]);
        else
            merged.put("JOB_SHORT_NAME", build.getProject().getFullDisplayName());
        merged.put("STATUS", getStatus());
        merged.put("DURATION", build.getDurationString());
        merged.put("URL", NotificationTypeUtils.getUrl(build));
        merged.put("CAUSE", cause);
        merged.put("CHANGES_OR_CAUSE", changes != null ? changes : cause);
        merged.put("CHANGES", changes);
        merged.put("PRINT_FULL_ENV", merged.toString());
        return merged;
    }

    private EnvVars getEnvVars(AbstractBuild<?, ?> build) {
        try {
            return build.getEnvironment(new LogTaskListener(LOGGER, INFO));
        } catch (IOException e) {
            throw propagate(e);
        } catch (InterruptedException e) {
            throw propagate(e);
        }
    }

    public static final NotificationType fromResults(Result previousResult, Result result) {
        if (result == Result.ABORTED) {
            return ABORTED;
        } else if (result == Result.FAILURE) {
            return FAILURE;
        } else if (result == Result.NOT_BUILT) {
            return NOT_BUILT;
        } else if (result == Result.UNSTABLE) {
            return UNSTABLE;
        } else if (result == Result.SUCCESS) {
            if (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) {
                return BACK_TO_NORMAL;
            } else {
                return SUCCESS;
            }
        }

        return UNKNOWN;
    }
}
