package model;

import java.sql.Date;

public class Cargo {
	private String cargo_id;
	private String cargo_name;
	private String stock_quantity;
	private String price;
	private Date suppiler;
	private Date expiration_date;
	
	public Cargo() {}

	public Cargo(String cargo_id, String cargo_name, String stock_quantity, String price, Date suppiler,
			Date expiration_date) {
		this.cargo_id = cargo_id;
		this.cargo_name = cargo_name;
		this.stock_quantity = stock_quantity;
		this.price = price;
		this.suppiler = suppiler;
		this.expiration_date = expiration_date;
	}
	
	public void updateInfo(Cargo tmp) {
		this.cargo_id = tmp.cargo_id;
		this.cargo_name = tmp.cargo_name;
		this.stock_quantity = tmp.stock_quantity;
		this.price = tmp.price;
		this.suppiler = tmp.suppiler;
		this.expiration_date = tmp.expiration_date;
	}

	public String getCargo_id() {
		return cargo_id;
	}

	public void setCargo_id(String cargo_id) {
		this.cargo_id = cargo_id;
	}

	public String getCargo_name() {
		return cargo_name;
	}

	public void setCargo_name(String cargo_name) {
		this.cargo_name = cargo_name;
	}

	public String getStock_quantity() {
		return stock_quantity;
	}

	public void setStock_quantity(String stock_quantity) {
		this.stock_quantity = stock_quantity;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public Date getSuppiler() {
		return suppiler;
	}

	public void setSuppiler(Date suppiler) {
		this.suppiler = suppiler;
	}

	public Date getExpiration_date() {
		return expiration_date;
	}

	public void setExpiration_date(Date expiration_date) {
		this.expiration_date = expiration_date;
	}
}
