package jenkins.plugins.spark.impl;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import com.cisco.dft.cd.spark.intg.pojo.HttpResponseEntity;
// import com.cisco.dft.cd.spark.intg.service.pojo.*;
// import com.cisco.dft.cd.spark.intg.service.*;
// import com.cisco.dft.cd.spark.intg.service.pojo.Actor;
import com.cisco.dft.cd.spark.intg.pojo.Message;
import com.cisco.dft.cd.spark.intg.pojo.Actor;
import com.cisco.dft.cd.spark.intg.service.impl.SparkIntegrationService;
import com.cisco.dft.cd.spark.intg.util.Constants;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.plugins.spark.SparkService;

public class SparkV1Service extends SparkService {

    private static final Logger logger = Logger.getLogger(SparkV1Service.class.getName());
    private static final String[] DEFAULT_ROOMS = new String[0];

    private final String server;
    private final String token;
    private final String[] roomIds;
    private final String sendAs;
    private SparkIntegrationService service;

    public SparkV1Service(String server, String token, String roomIds, String sendAs) {
        this.server = server;
        this.token = token;
        this.roomIds = roomIds == null ? DEFAULT_ROOMS : roomIds.split("\\s*,\\s*");
        this.sendAs = sendAs;
        this.service = new SparkIntegrationService();
        service.inviteParticipants("a75a3670-fd91-11e4-a797-61b8418480fd",
            "5a86a12b-c580-4ada-b4c0-9d0dfe2d72cd",
            "Yzk5NzI4MjctMDc3MC00ZGQ0LTgwYTQtYzk1NWQ1YjFjYTU1OTQ0OWMyMzQtMjgx");
    }

    @Override
    public void publish(String message, String color) {
        publish(message, color, shouldNotify(color));
    }

    @Override
    public void publish(String message, String color, boolean notify) {
        // for (String roomId : roomIds) {
        //     logger.log(Level.INFO, "Posting: {0} to {1}: {2} {3}", new Object[]{sendAs, roomId, message, color});
        //     HttpClient client = getHttpClient();
        //     String url = "https://" + server + "/v1/rooms/message";
        //     PostMethod post = new PostMethod(url);

        //     try {
        //         post.addParameter("auth_token", token);
        //         post.addParameter("from", sendAs);
        //         post.addParameter("room_id", roomId);
        //         post.addParameter("message", message);
        //         post.addParameter("color", color);
        //         post.addParameter("notify", notify ? "1" : "0");
        //         post.getParams().setContentCharset("UTF-8");
        //         int responseCode = client.executeMethod(post);
        //         String response = post.getResponseBodyAsString();
        //         if (responseCode != HttpStatus.SC_OK || !response.contains("\"sent\"")) {
        //             logger.log(Level.WARNING, "Spark post may have failed. Response: {0}", response);
        //         }
        //     } catch (Exception e) {
        //         logger.log(Level.WARNING, "Error posting to Spark", e);
        //     } finally {
        //         post.releaseConnection();
        //     }
        // }

        Actor actor = new Actor();
        actor.setUsername("platform-jenkins");
        actor.setPassword("W8.5)M1)/17=y6cTirbVL)oVc|0jF$M0");
        actor.setUid("e2756438-fc0b-4281-a3b3-a9dcf9af3aaf");
        
        Message messageToSend = new Message();
        messageToSend.setMessage(message);
        messageToSend.setRoomId("c60f38e0-ef86-11e4-9df9-270f179cbc2c");
        messageToSend.setActor(actor);
        HttpResponseEntity response = service.publishMessage(messageToSend);
        System.out.println("response: " + response.getStatusCode());
    }

    private boolean shouldNotify(String color) {
        return !color.equalsIgnoreCase("green");
    }

    public String getServer() {
        return server;
    }

    public String[] getRoomIds() {
        return roomIds;
    }

    public String getSendAs() {
        return sendAs;
    }
}
