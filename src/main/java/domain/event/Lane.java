package domain.event;

public class Lane {
    private final int index;
    private final double distanta;

    public Lane(int index, double distanta) {
        this.index = index;
        this.distanta = distanta;
    }

    public int getIndex() {
        return index;
    }

    public double getDistanta() {
        return distanta;
    }

    @Override
    public String toString() {
        return "Lane " + index + " (d=" + distanta + ")";
    }
}
