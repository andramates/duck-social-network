package domain.event;

public class RaceResult {
    private final double timpMinim;
    private final Assignment[] assignments;

    public RaceResult(double timpMinim, Assignment[] assignments) {
        this.timpMinim = timpMinim;
        this.assignments = assignments;
    }

    public double getTimpMinim() {
        return timpMinim;
    }

    public Assignment[] getAssignments() {
        return assignments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("best time: ").append(String.format("%.9f", timpMinim)).append(" secunde\n");
        if (assignments != null) {
            for (Assignment assignment : assignments) {
                sb.append(assignment.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}
