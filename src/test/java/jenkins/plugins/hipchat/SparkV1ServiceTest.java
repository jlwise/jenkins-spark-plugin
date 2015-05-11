    package jenkins.plugins.spark;

import jenkins.plugins.spark.impl.SparkV1Service;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class SparkV1ServiceTest {
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        SparkV1Service service = new SparkV1Service("badhost", "token", "room", "from");
        service.publish("message", "yellow");
    }

    @Test
    public void shouldBeAbleToOverrideHost() {
        SparkV1Service service = new SparkV1Service("some.other.host", "token", "room", "from");
        assertEquals("some.other.host", service.getServer());
    }

    @Test
    public void shouldSplitTheRoomIds() {
        SparkV1Service service = new SparkV1Service(null, "token", "room1,room2", "from");
        assertArrayEquals(new String[]{"room1", "room2"}, service.getRoomIds());
    }

    @Test
    public void shouldTrimTheRoomIds() {
        SparkV1Service service = new SparkV1Service(null, "token", "room1, room2", "from");
        assertArrayEquals(new String[]{"room1", "room2"}, service.getRoomIds());
    }

    @Test
    public void shouldNotSplitTheRoomsIfNullIsPassed() {
        SparkV1Service service = new SparkV1Service(null, "token", null, "from");
        assertArrayEquals(new String[0], service.getRoomIds());
    }

    @Test
    public void shouldBeAbleToOverrideFrom() {
        SparkV1Service service = new SparkV1Service(null, "token", "room", "from");
        assertEquals("from", service.getSendAs());
    }
}
