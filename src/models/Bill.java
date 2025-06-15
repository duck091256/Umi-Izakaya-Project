package models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Bill {
	private String billID;
	private int wasPay;
	private Date time;
	private Double payment;
	private List<DetailReceipt> detailList = new ArrayList<>();

	public Bill(String billID, int wasPay, Date time, Double payment) {
		super();
		this.billID = billID;
		this.wasPay = wasPay;
		this.time = time;
		this.payment = payment;
	}
	
	public String getBillID() {
		return billID;
	}
	
	public void setBillID(String billID) {
		this.billID = billID;
	}
	
	public int isWasPay() {
		return wasPay;
	}
	
	public void setWasPay(int wasPay) {
		this.wasPay = wasPay;
	}
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
	
	public Double getPayment() {
		return payment;
	}
	
	public void setPayment(Double payment) {
		this.payment = payment;
	}
	
	public List<DetailReceipt> getDetailList() {
	    return detailList;
	}

	public void addDetail(DetailReceipt detail) {
	    detailList.add(detail);
	}
}
