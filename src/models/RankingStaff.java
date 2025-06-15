package models;

public class RankingStaff {
    private String staffID;
    private int totalSessions;
    private int totalDishes;
    private double rating;
    private int ranking;
    
	public RankingStaff(String staffID, int totalSessions, int totalDishes, double rating, int ranking) {
		super();
		this.staffID = staffID;
		this.totalSessions = totalSessions;
		this.totalDishes = totalDishes;
		this.rating = rating;
		this.ranking = ranking;
	}
	
	public String getStaffID() {
		return staffID;
	}
	public void setStaffID(String staffID) {
		this.staffID = staffID;
	}
	public int getTotalSessions() {
		return totalSessions;
	}
	public void setTotalSessions(int totalSessions) {
		this.totalSessions = totalSessions;
	}
	public int getTotalDishes() {
		return totalDishes;
	}
	public void setTotalDishes(int totalDishes) {
		this.totalDishes = totalDishes;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public int getRanking() {
		return ranking;
	}
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}    
}
