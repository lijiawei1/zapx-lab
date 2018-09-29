package org.zap.framework.test.pojo;

import org.zap.framework.lang.LDouble;
import org.zap.framework.orm.annotation.JdbcColumn;
import org.zap.framework.orm.annotation.JdbcTable;
import org.zap.framework.orm.base.BaseEntity;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@JdbcTable(value="ZAP_TEST", alias="ZT")
public class TestVo extends BaseEntity {

	
	public static String CHAR_FIELD = "char_field";
	public static String VARCHAR_FIELD = "varchar_field";
	
	public static String NUMBER_FIELD = "number_field";
	public static String LONG_FIELD = "long_field";
	
	public static String INTEGER_FIELD = "integer_field";
	public static String INT_FIELD = "int_field";
	
	public static String LDOUBLE_FIELD = "ldouble_field";
	
	public static String DBL_FIELD = "dbl_field";
	public static String DOUBLE_FIELD = "double_field";
	
	public static String CLOB_FIELD = "clob_field";
	public static String BLOB_FIELD = "blob_field";
	
	public static String DATETIME_FIELD = "datetime_field";
	public static String DATE_FIELD = "date_field";
	public static String TIME_FIELD = "time_field";
	
	public static String BOOL_FIELD = "bool_field";
	public static String BOOLEAN_FIELD = "boolean_field";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4668854047226193196L;

	@JdbcColumn
	private int dr;

	@JdbcColumn
	private String corp_id;

	@JdbcColumn
	private String dept_id;

	@JdbcColumn
	private String remark;

	@JdbcColumn
	private String creator_id;

	@JdbcColumn
	private LocalDateTime create_time;

	@JdbcColumn
	private String modifier_id;

	@JdbcColumn
	private LocalDateTime modify_time;

	@JdbcColumn(type = 1)
	private String char_field;
	
	@JdbcColumn
	private String varchar_field;
	
	@JdbcColumn
	private BigDecimal number_field;

	@JdbcColumn
	private Long long_field;
	
	@JdbcColumn
	private Integer integer_field;
	
	@JdbcColumn
	private int int_field;
	
	@JdbcColumn
	private LDouble ldouble_field;
	
	@JdbcColumn
	private Double dbl_field;
	
	@JdbcColumn
	private double double_field;
	
	//@JdbcColumn
	private String clob_field;
	
//	@JdbcColumn
//	private byte[] blob_field;
	
	@JdbcColumn
	private LocalDateTime  datetime_field;

	@JdbcColumn
	private LocalDate  date_field;
	
	@JdbcColumn
	private LocalTime  time_field;
	
	@JdbcColumn
	private boolean bool_field;
	
	@JdbcColumn
	private Boolean boolean_field;

	/**
	 * 表示数据库存放的字段类型为date
	 */
	@JdbcColumn(type = Types.DATE)
	private LocalDateTime old_datetime;

	@JdbcColumn(type = Types.DATE)
	private LocalDate old_date;

	@JdbcColumn(type = Types.DATE)
	private LocalTime old_time;
	
	public boolean isBool_field() {
		return bool_field;
	}

	public void setBool_field(boolean bool_field) {
		this.bool_field = bool_field;
	}

	public Boolean getBoolean_field() {
		return boolean_field;
	}

	public void setBoolean_field(Boolean boolean_field) {
		this.boolean_field = boolean_field;
	}

	public String getChar_field() {
		return char_field;
	}

	public void setChar_field(String char_field) {
		this.char_field = char_field;
	}

	public BigDecimal getNumber_field() {
		return number_field;
	}

	public void setNumber_field(BigDecimal number_field) {
		this.number_field = number_field;
	}

	public Integer getInteger_field() {
		return integer_field;
	}

	public void setInteger_field(Integer integer_field) {
		this.integer_field = integer_field;
	}

	public int getInt_field() {
		return int_field;
	}

	public void setInt_field(int int_field) {
		this.int_field = int_field;
	}

	public Double getDbl_field() {
		return dbl_field;
	}

	public void setDbl_field(Double dbl_field) {
		this.dbl_field = dbl_field;
	}

	public double getDouble_field() {
		return double_field;
	}

	public void setDouble_field(double double_field) {
		this.double_field = double_field;
	}

	public Long getLong_field() {
		return long_field;
	}

	public void setLong_field(Long long_field) {
		this.long_field = long_field;
	}

	public String getClob_field() {
		return clob_field;
	}

	public void setClob_field(String clob_field) {
		this.clob_field = clob_field;
	}

//	public byte[] getBlob_field() {
//		return blob_field;
//	}
//
//	public void setBlob_field(byte[] blob_field) {
//		this.blob_field = blob_field;
//	}
	
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

	public String getVarchar_field() {
		return varchar_field;
	}

	public void setVarchar_field(String varchar_field) {
		this.varchar_field = varchar_field;
	}

	public LDouble getLdouble_field() {
		return ldouble_field;
	}

	public void setLdouble_field(LDouble ldouble_field) {
		this.ldouble_field = ldouble_field;
	}

	public LocalDateTime getOld_datetime() {
		return old_datetime;
	}

	public void setOld_datetime(LocalDateTime old_datetime) {
		this.old_datetime = old_datetime;
	}

	public LocalDate getOld_date() {
		return old_date;
	}

	public void setOld_date(LocalDate old_date) {
		this.old_date = old_date;
	}

	public LocalTime getOld_time() {
		return old_time;
	}

	public void setOld_time(LocalTime old_time) {
		this.old_time = old_time;
	}

	public String getCreator_id() {
		return creator_id;
	}

	public void setCreator_id(String creator_id) {
		this.creator_id = creator_id;
	}

	public LocalDateTime getCreate_time() {
		return create_time;
	}

	public void setCreate_time(LocalDateTime create_time) {
		this.create_time = create_time;
	}

	public String getModifier_id() {
		return modifier_id;
	}

	public void setModifier_id(String modifier_id) {
		this.modifier_id = modifier_id;
	}

	public LocalDateTime getModify_time() {
		return modify_time;
	}

	public void setModify_time(LocalDateTime modify_time) {
		this.modify_time = modify_time;
	}

	public String getCorp_id() {
		return corp_id;
	}

	public void setCorp_id(String corp_id) {
		this.corp_id = corp_id;
	}

	public String getDept_id() {
		return dept_id;
	}

	public void setDept_id(String dept_id) {
		this.dept_id = dept_id;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getDr() {
		return dr;
	}

	public void setDr(int dr) {
		this.dr = dr;
	}
}
