package screentimeai.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import screentimeai.UserserviceApplication;

//<<< Clean Arch / Outbound Adaptor
public class AbstractEvent {

    String eventType;
    Long timestamp;

    public AbstractEvent(Object aggregate) {
        this();
        BeanUtils.copyProperties(aggregate, this);
    }

    public AbstractEvent() {
        this.setEventType(this.getClass().getSimpleName());
        this.timestamp = System.currentTimeMillis();
    }

//    public void publish() {
//        /**
//         * spring streams 함수형 방식
//         */
//        StreamBridge streamBridge = UserserviceApplication.applicationContext.getBean(
//            StreamBridge.class
//        );
//
//        streamBridge.send("eventProducer-out-0", this);
//    }
    public void publish() {
        if (UserserviceApplication.applicationContext == null) {
            System.out.println("⚠️ ApplicationContext is null, skipping publish for " + this.getClass().getSimpleName());
            return;
        }

        try {
            StreamBridge streamBridge = UserserviceApplication.applicationContext.getBean(StreamBridge.class);
            streamBridge.send("eventProducer-out-0", this);
            System.out.println("📤 Event published via StreamBridge: " + this.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("⚠️ Failed to publish event: " + e.getMessage());
        }
    }



    public void publishAfterCommit() {
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    AbstractEvent.this.publish();
                }
            }
        );
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean validate() {
        return getEventType().equals(getClass().getSimpleName());
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;

        try {
            json = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }

        return json;
    }
}
//>>> Clean Arch / Outbound Adaptor
