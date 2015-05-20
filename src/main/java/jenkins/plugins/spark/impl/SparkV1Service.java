package jenkins.plugins.spark.impl;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import com.cisco.dft.cd.spark.intg.pojo.HttpResponseEntity;

import com.cisco.dft.cd.spark.intg.pojo.Message;
import com.cisco.dft.cd.spark.intg.pojo.Actor;
import com.cisco.dft.cd.spark.intg.service.impl.SparkIntegrationService;
import com.cisco.dft.cd.spark.intg.util.Constants;
import com.cisco.dft.cd.spark.intg.util.Util;
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
        service.inviteParticipants(roomIds,
            "e2756438-fc0b-4281-a3b3-a9dcf9af3aaf",
            token);
    }

    @Override
    public void publish(String message, String color) {
        publish(message, color, shouldNotify(color));
    }

    @Override
    public void publish(String message, String color, boolean notify) {
        try {
            Actor actor = new Actor();
            actor.setUsername("platform-jenkins");
            actor.setPassword("W8.5)M1)/17=y6cTirbVL)oVc|0jF$M0");
            actor.setUid("e2756438-fc0b-4281-a3b3-a9dcf9af3aaf");
            
            Message messageToSend = new Message();
            messageToSend.setRoomId(roomIds[0]);
            messageToSend.setActor(actor); 
            message = Util.emojifyText(message); 
            messageToSend.setMessage(message);
            HttpResponseEntity response = service.publishMessage(messageToSend);
            System.out.println("response: " + response.getStatusCode());
        } catch (Exception e){

        }

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
