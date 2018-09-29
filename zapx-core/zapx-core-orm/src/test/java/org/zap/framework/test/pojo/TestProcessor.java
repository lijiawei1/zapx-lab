package org.zap.framework.test.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestProcessor {
	
	private LocalDateTime  datetime_field;
	
	private LocalDate  date_field;
	
	private LocalTime  time_field;
	
	private boolean bool_field;
	
	private String varchar_field;
	
	private BigDecimal number_field;

	public LocalDateTime getDatetime_field() {
		return datetime_field;
	}

	public void setDatetime_field(LocalDateTime datetime_field) {
		this.datetime_field = datetime_field;
	}

	public LocalDate getDate_field() {
		return date_field;
	}

	public void setDate_field(LocalDate date_field) {
		this.date_field = date_field;
	}

	public LocalTime getTime_field() {
		return time_field;
	}

	public void setTime_field(LocalTime time_field) {
		this.time_field = time_field;
	}

	public boolean isBool_field() {
		return bool_field;
	}

	public void setBool_field(boolean bool_field) {
		this.bool_field = bool_field;
	}

	public String getVarchar_field() {
		return varchar_field;
	}

	public void setVarchar_field(String varchar_field) {
		this.varchar_field = varchar_field;
	}

	public BigDecimal getNumber_field() {
		return number_field;
	}

	public void setNumber_field(BigDecimal number_field) {
		this.number_field = number_field;
	}
	
	

}
