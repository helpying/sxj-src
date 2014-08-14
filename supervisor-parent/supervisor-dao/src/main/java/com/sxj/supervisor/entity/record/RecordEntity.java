package com.sxj.supervisor.entity.record;

import java.io.Serializable;
import java.util.Date;

import com.sxj.mybatis.orm.annotations.Column;
import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.GeneratedValue;
import com.sxj.mybatis.orm.annotations.GenerationType;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.pagination.Pagable;
import com.sxj.supervisor.dao.record.IRecordDao;

/**
 * 备案实体类
 * @author Administrator
 *
 */
@Entity(mapper = IRecordDao.class)
@Table(name = "RECORD")
public class RecordEntity extends Pagable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5309323695383014154L;

	/**
	 * 主键ID
	 **/
	@Id(column = "ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;

	/**
	 * 备案号
	 **/
	@Column(name = "RECORD_NO")
	private String recordNo;

	/**
	 * 申请会员ID
	 **/
	@Column(name = "APPLY_ID")
	private String applyId;

	/**
	 * 申请会员名称
	 **/
	@Column(name = "APPLY_NAME")
	private String applyName;

	/**
	 * 甲方会员ID
	 **/
	@Column(name = "MEMBER_ID_A")
	private String memberIdA;

	/**
	 * 甲方会员名称
	 **/
	@Column(name = "MEMBER_NAME_A")
	private String memberNameA;

	/**
	 * 乙方会员ID
	 **/
	@Column(name = "MEMBER_ID_B")
	private String memberIdB;

	/**
	 * 乙方会员名称
	 **/
	@Column(name = "MEMBER_NAME_B")
	private String memberNameB;

	/**
	 * 备案类型
	 **/
	@Column(name = "TYPE")
	private Integer type;

	/**
	 * 备案扫描件
	 **/
	@Column(name = "IMG_PATH")
	private String imgPath;

	/**
	 * 备案状态
	 **/
	@Column(name = "STATE")
	private Integer state;

	/**
	 * 合同类型
	 **/
	@Column(name = "CONTRACT_TYPE")
	private Integer contractType;

	/**
	 * 绑定合同号
	 **/
	@Column(name = "CONTRACT_NO")
	private String contractNo;

	/**
	 * 关联招标合同
	 **/
	@Column(name = "REF_CONTRACT_NO")
	private String refContractNo;

	/**
	 * 申请时间
	**/
	@Column(name = "APPLY_DATE")
	private Date applyDate;
	
	/**
	 * 受理时间
	**/
	@Column(name = "ACCEPT_DATE")
	private Date acceptDate;
	
	public Date getApplyDate() {
		return applyDate;
	}

	public void setApplyDate(Date applyDate) {
		this.applyDate = applyDate;
	}

	public Date getAcceptDate() {
		return acceptDate;
	}

	public void setAcceptDate(Date acceptDate) {
		this.acceptDate = acceptDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRecordNo() {
		return recordNo;
	}

	public void setRecordNo(String recordNo) {
		this.recordNo = recordNo;
	}

	public String getApplyId() {
		return applyId;
	}

	public void setApplyId(String applyId) {
		this.applyId = applyId;
	}

	public String getApplyName() {
		return applyName;
	}

	public void setApplyName(String applyName) {
		this.applyName = applyName;
	}

	public String getMemberIdA() {
		return memberIdA;
	}

	public void setMemberIdA(String memberIdA) {
		this.memberIdA = memberIdA;
	}

	public String getMemberNameA() {
		return memberNameA;
	}

	public void setMemberNameA(String memberNameA) {
		this.memberNameA = memberNameA;
	}

	public String getMemberIdB() {
		return memberIdB;
	}

	public void setMemberIdB(String memberIdB) {
		this.memberIdB = memberIdB;
	}

	public String getMemberNameB() {
		return memberNameB;
	}

	public void setMemberNameB(String memberNameB) {
		this.memberNameB = memberNameB;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getContractType() {
		return contractType;
	}

	public void setContractType(Integer contractType) {
		this.contractType = contractType;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getRefContractNo() {
		return refContractNo;
	}

	public void setRefContractNo(String refContractNo) {
		this.refContractNo = refContractNo;
	}
}