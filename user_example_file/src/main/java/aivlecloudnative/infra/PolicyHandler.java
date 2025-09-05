package aivlecloudnative.infra;

import aivlecloudnative.application.UserService;
import aivlecloudnative.domain.AuthorAccepted;
import aivlecloudnative.domain.BookViewed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class PolicyHandler {

    @Autowired
    private UserService userService;

    @Bean
    public Consumer<BookViewed> updateBookRead() {
        return bookViewed -> {
            System.out.println("##### listener UpdateBookRead : " + bookViewed);
            userService.updateBookRead(bookViewed);
        };
    }

    @Bean
    public Consumer<AuthorAccepted> authorApproved() {
        return authorAccepted -> {
            System.out.println("##### listener AuthorApproved : " + authorAccepted);
            userService.authorApproved(authorAccepted);
        };
    }

}
