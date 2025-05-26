package model;

public class SimpleRanking {
    private int ranking;
    private String fullName;
    private double rating;

    public SimpleRanking(int ranking, String fullName, double rating) {
        this.ranking = ranking;
        this.fullName = fullName;
        this.rating = rating;
    }

    public int getRanking() { return ranking; }
    public String getFullName() { return fullName; }
    public double getRating() { return rating; }
}