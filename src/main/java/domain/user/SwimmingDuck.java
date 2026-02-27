package domain.user;

public class SwimmingDuck extends Duck implements Swimmer {

    public SwimmingDuck(Long id, String username, String email, String password,
                        double speed, double endurance) {
        super(id, username, email, password, DuckType.SWIMMING, speed, endurance, null);
    }

    @Override
    public void swim() {
        System.out.println(getUsername() + " is swimming");
    }
}
