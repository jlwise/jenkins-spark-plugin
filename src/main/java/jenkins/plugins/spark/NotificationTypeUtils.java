package jenkins.plugins.spark;

import com.google.common.collect.Sets;
import hudson.model.AbstractBuild;
import hudson.model.CauseAction;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import static java.util.logging.Level.*;

public final class NotificationTypeUtils {

    private static final Logger LOGGER = Logger.getLogger(NotificationTypeUtils.class.getName());

    private NotificationTypeUtils() {
    }

    public static String getCause(AbstractBuild<?, ?> build) {
        CauseAction cause = build.getAction(CauseAction.class);
        if (cause != null) {
            return cause.getShortDescription().substring(0, cause.getShortDescription().lastIndexOf(" "));
        } else {
            return null;
        }
    }

    public static String getUrl(AbstractBuild<?, ?> build) {
        return Jenkins.getInstance().getRootUrl() + build.getUrl();
    }

    //\\w+\\s+\\w+
    public static String getChanges(AbstractBuild<?, ?> build) {
        if (!build.hasChangeSetComputed()) {
            LOGGER.log(FINE, "No changeset computed for job {0}", build.getProject().getDisplayName());
            return null;
        }
        Set<String> authors = Sets.newHashSet();
        int changedFiles = 0;
        for (Object o : build.getChangeSet().getItems()) {
            ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
            LOGGER.log(FINEST, "Entry {0}", entry);
            String[] authorName = entry.getAuthor().getDisplayName().split(" ");
            authors.add(authorName[0] + " " + authorName[1]);
            try {
                changedFiles += entry.getAffectedFiles().size();
            } catch (UnsupportedOperationException e) {
                LOGGER.log(INFO, "Unable to collect the affected files for job {0}",
                        build.getProject().getDisplayName());
                return null;
            }
        }
        if (changedFiles == 0) {
            LOGGER.log(FINE, "No changes detected");
            return null;
        }

        return Messages.StartWithChanges(StringUtils.join(authors, ", "));
    }
}
