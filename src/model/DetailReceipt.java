package model;

public class DetailReceipt {
	private String dishID;
	private String billID;
	private int dishQuantity;
	
	public DetailReceipt(String dishID, String billID, int dishQuantity) {
		super();
		this.dishID = dishID;
		this.billID = billID;
		this.dishQuantity = dishQuantity;
	}

	public String getDishID() {
		return dishID;
	}

	public void setDishID(String dishID) {
		this.dishID = dishID;
	}

	public String getBillID() {
		return billID;
	}

	public void setBillID(String billID) {
		this.billID = billID;
	}

	public int getDishQuantity() {
		return dishQuantity;
	}

	public void setDishQuantity(int dishQuantity) {
		this.dishQuantity = dishQuantity;
	}

}
