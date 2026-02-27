package domain.profile;

import domain.user.User;

public class ProfilePage {
    private final User user;

    public ProfilePage(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
