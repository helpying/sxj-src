package com.sxj.supervisor.model.member;

import java.io.Serializable;

import com.sxj.mybatis.pagination.Pagable;

public class AccountQuery extends Pagable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5168931378789020273L;

<<<<<<< HEAD
	private String memberNo;

	private String accountNo;

=======
	private String memberId;
	private String accountId;
>>>>>>> 21c1d89118c71636811b61c371af3886c92b4d4b
	private String accountName;

	private Integer state;

	private String startDate;

	private String endDate;

	private String roleId;

	private Boolean delstate = false;

	public Boolean getDelstate() {
		return delstate;
	}

	public void setDelstate(Boolean delstate) {
		this.delstate = delstate;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
}
