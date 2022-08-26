package com.optum.seat.models;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
@Entity
@Table(name = "user_last_booking")
public class UserLastBooking {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Size(max = 20)
	@Column(name="empid", unique = true)
	private String empID;
	
	
	private String slot;
	
	private Date date;
	
	private Integer last_trxn_id;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getEmpID() {
		return empID;
	}
	public void setEmpID(String empID) {
		this.empID = empID;
	}
	public String getSlot() {
		return slot;
	}
	public void setSlot(String slot) {
		this.slot = slot;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Integer getLast_trxn_id() {
		return last_trxn_id;
	}
	public void setLast_trxn_id(Integer last_trxn_id) {
		this.last_trxn_id = last_trxn_id;
	}
	public UserLastBooking( @Size(max = 20) String empID,  String slot, Date date, Integer last_trxn_id) {
		super();
		this.empID = empID;
		this.slot = slot;
		this.date = date;
		this.last_trxn_id = last_trxn_id;
	}
	public UserLastBooking() {
		
	}
	
	
	
	
	
	
	
}
