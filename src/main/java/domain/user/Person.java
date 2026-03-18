package domain.user;

import domain.Message;

import java.time.LocalDate;

public class Person extends User {
    private final String lastName;
    private final String firstName;
    private final LocalDate dateOfBirth;
    private final String occupation;
    private final double empathyLevel;

    public Person(Long id, String username, String email, String password,
                  String lastName, String firstName, LocalDate dateOfBirth,
                  String occupation, double empathyLevel) {
        super(id, username, email, password);
        this.lastName = lastName;
        this.firstName = firstName;
        this.dateOfBirth = dateOfBirth;
        this.occupation = occupation;
        this.empathyLevel = empathyLevel;
    }

    public String getLastName() {
        return lastName;
    }
    public String getFirstName() {
        return firstName;
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public String getOccupation() {
        return occupation;
    }
    public double getEmpathyLevel() {
        return empathyLevel;
    }

    public void createEvent(String eventName) {
        eventsLog.add("[CREATE EVENT] " + eventName + " by " + username);
    }

    public void sendMessageTo(Message message, User user) {
        if (user == null || message == null) return;
        eventsLog.add("[SEND MESSAGE] to" + user.getUsername() + ": " + message.getContent());
    }

    @Override
    public String toString() {
        return username;
    }
}

