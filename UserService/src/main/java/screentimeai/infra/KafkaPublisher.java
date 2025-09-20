//package screentimeai.infra;
//import org.springframework.context.event.EventListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//import screentimeai.domain.*;
//
//@Component
//public class KafkaPublisher {
//    private final KafkaTemplate<String, String> kafkaTemplate;
//
//    public KafkaPublisher(KafkaTemplate<String, String> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    @EventListener
//    public void handleSignedUp(SignedUp event) {
//        String message = String.format("User{id=%d,nickname=%s,action=signup}",
//                event.getId(), event.getNickname());
//        kafkaTemplate.send("user_signup", message);
//        System.out.println("📤 Kafka published signup: " + message);
//    }
//
//    @EventListener
//    public void handleLoggin(LoggedIn event) {
//        String message = String.format("User{id=%d,nickname=%s,action=login}",
//                event.getId(), event.getNickname());
//        kafkaTemplate.send("user_login", message);
//        System.out.println("📤 Kafka published login: " + message);
//    }
//
//    @EventListener
//    public void handleLoggedOut(LoggedOut event) {
//        String message = String.format("User{id=%d,nickname=%s,action=logout}",
//                event.getId(), event.getNickname());
//        kafkaTemplate.send("user_logout", message);
//        System.out.println("📤 Kafka published logout: " + message);
//    }
//
//    @EventListener
//    public void handlePasswordChange(PasswordChanged event) {
//        String message = String.format("User{id=%d,nickname=%s,action=password-changed}",
//                event.getId(), event.getNickname());
//        kafkaTemplate.send("user_password_changed", message);
//        System.out.println("📤 Kafka published logout: " + message);
//    }
//
//
//    @EventListener
//    public void handleDeactivated(AccountDeactivated event) {
//        String message = String.format("User{id=%d,nickname=%s,action=account-deactivated}",
//                event.getId(), event.getNickname());
//        kafkaTemplate.send("user_deactivated", message);
//        System.out.println("📤 Kafka published logout: " + message);
//    }
//}

