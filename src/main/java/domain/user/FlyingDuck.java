package domain.user;

public class FlyingDuck extends Duck implements Flyer {

    public FlyingDuck(Long id, String username, String email, String password,
                      double speed, double endurance) {
        super(id, username, email, password, DuckType.FLYING, speed, endurance, null);
    }

    @Override
    public void fly() {
        System.out.println(getUsername() + " is flying");
    }
}
