package domain.user;

public class FlyingAndSwimmingDuck extends Duck implements Flyer, Swimmer {
    public FlyingAndSwimmingDuck(Long id, String username, String email, String password,
                                 double speed, double endurance) {
        super(id, username, email, password, DuckType.FLYING_AND_SWIMMING, speed, endurance, null);
    }

    @Override
    public void swim() {
        System.out.println(getUsername() + " is swimming");
    }

    @Override
    public void fly() {
        System.out.println(getUsername() + " is flying");
    }
}
