package com.sxj.supervisor.entity.rfid.purchase;

import java.io.Serializable;
import java.util.Date;

import com.sxj.mybatis.orm.annotations.Column;
import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.GeneratedValue;
import com.sxj.mybatis.orm.annotations.GenerationType;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Sn;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.pagination.Pagable;

@Entity
@Table(name = "R_RFID_PURCHASE")
public class RfidPurchaseEntity extends Pagable implements Serializable {

	/**
	 * RFID采购申请单
	 */
	private static final long serialVersionUID = 5185244023917957270L;

	/**
	 * ID
	 */
	@Id(column = "ID")
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	/**
	 * 采购单编号
	 */
	@Column(name = "PURCHASE_NO")
	@Sn(pattern = "000000", step = 1, table = "T_SN", stubValue = "CG", stub = "F_SN_NAME", sn = "F_SN_NUMBER")
	private String purchaseNo;

	/**
	 * 供应商编号
	 */
	@Column(name = "SUPPLIER_NO")
	private String supplierNo;

	/**
	 * 供应商名称
	 */
	@Column(name = "SUPPLIER_NAME")
	private String supplierName;

	/**
	 * RFID类型
	 */
	@Column(name = "RFID_TYPE")
	private Enum rfidType;

	/**
	 * 数量
	 */
	@Column(name = "COUNT")
	private Long count;

	/**
	 * 单价
	 */
	@Column(name = "PRICE")
	private Long price;

	/**
	 * 金额
	 */
	@Column(name = "AMOUNT")
	private Long amount;

	/**
	 * 采购日期
	 */
	@Column(name = "PURCHASE_DATE")
	private Date purchaseDate;

	/**
	 * 招标合同号
	 */
	@Column(name = "CONTRACT_NO")
	private String contractNo;

	/**
	 * 申请单编号
	 */
	@Column(name = "APPLY_NO")
	private String applyNo;

	/**
	 * 导入状态
	 */
	@Column(name = "IMPORT_STATE")
	private Enum importState;
	/**
	 * 支付状态
	 */
	@Column(name = "PAY_STATE")
	private Enum payState;

	/**
	 * 收货状态
	 */
	@Column(name = "RECEIPT_STATE")
	private Enum receiptState;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPurchaseNo() {
		return purchaseNo;
	}

	public void setPurchaseNo(String purchaseNo) {
		this.purchaseNo = purchaseNo;
	}

	public String getSupplierNo() {
		return supplierNo;
	}

	public void setSupplierNo(String supplierNo) {
		this.supplierNo = supplierNo;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public Enum getRfidType() {
		return rfidType;
	}

	public void setRfidType(Enum rfidType) {
		this.rfidType = rfidType;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getApplyNo() {
		return applyNo;
	}

	public void setApplyNo(String applyNo) {
		this.applyNo = applyNo;
	}

	public Enum getImportState() {
		return importState;
	}

	public void setImportState(Enum importState) {
		this.importState = importState;
	}

	public Enum getPayState() {
		return payState;
	}

	public void setPayState(Enum payState) {
		this.payState = payState;
	}

	public Enum getReceiptState() {
		return receiptState;
	}

	public void setReceiptState(Enum receiptState) {
		this.receiptState = receiptState;
	}

}