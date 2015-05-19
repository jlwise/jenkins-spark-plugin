package jenkins.plugins.spark;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.listeners.ItemListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.spark.SparkNotifier.SparkJobProperty;

@Extension
public class ConfigurationMigrator extends ItemListener {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationMigrator.class.getName());

    @Override
    public void onLoaded() {
        for (AbstractProject<?, ?> item : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            SparkJobProperty property = item.getProperty(SparkJobProperty.class);
            if (property != null) {
                SparkNotifier notifier = item.getPublishersList().get(SparkNotifier.class);
                if (notifier != null) {
                    notifier.setRoom(property.getRoom());
                    // notifier.setBearerToken(property.getBearerToken());
                    notifier.setStartNotification(property.getStartNotification());
                    notifier.setNotifyAborted(property.getNotifyAborted());
                    notifier.setNotifyBackToNormal(property.getNotifyBackToNormal());
                    notifier.setNotifyFailure(property.getNotifyFailure());
                    notifier.setNotifyNotBuilt(property.getNotifyNotBuilt());
                    notifier.setNotifySuccess(property.getNotifySuccess());
                    notifier.setNotifyUnstable(property.getNotifyUnstable());
                }
                try {
                    item.removeProperty(SparkJobProperty.class);
                    LOGGER.log(Level.INFO, "Successfully migrated project configuration for build job: {0}",
                            item.getFullDisplayName());
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, "An error occurred while trying to update job configuration for "
                            + item.getName(), ioe);
                }
            }
        }
    }
}
