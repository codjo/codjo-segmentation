package net.codjo.segmentation.releasetest;
import net.codjo.agent.UserId;
import net.codjo.security.common.api.SecurityContext;
import net.codjo.security.common.api.User;
import net.codjo.security.server.api.UserFactory;
import java.util.ArrayList;
import java.util.List;
/**
 *
 */
public class UserFactoryMock implements UserFactory {
    private final User user;


    public UserFactoryMock(User user) {
        this.user = user;
    }


    public User getUser(UserId userId, SecurityContext securityContext) {
        return user;
    }


    public List<String> getRoleNames() {
        return new ArrayList<String>();
    }
}
