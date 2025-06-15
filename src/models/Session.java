package models;

public class Session {
	private int sessionID;
	private String staffID;
	private String tableID;
	private String billID;
	
	public Session(int sessionID, String staffID, String tableID, String billID) {
		super();
		this.sessionID = sessionID;
		this.staffID = staffID;
		this.tableID = tableID;
		this.billID = billID;
	}

	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	public String getStaffID() {
		return staffID;
	}

	public void setStaffID(String staffID) {
		this.staffID = staffID;
	}

	public String getTableID() {
		return tableID;
	}

	public void setTableID(String tableID) {
		this.tableID = tableID;
	}

	public String getBillID() {
		return billID;
	}

	public void setBillID(String billID) {
		this.billID = billID;
	}
}
