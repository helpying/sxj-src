package com.sxj.supervisor.service.record;

import com.sxj.supervisor.entity.record.RecordEntity;
import com.sxj.supervisor.model.record.RecordQuery;
import com.sxj.util.persistent.ResultList;

public interface IRecordService {
	/**
	 * 新增备案
	**/
	public void addRecord(RecordEntity record);
	
	/**
	 * 修改备案
	 *
	 * @param    record
	**/
	public void modifyRecord(RecordEntity record);
	
	/**
	 * 删除备案
	 *
	 * @param    id
	**/
	public void deleteRecord(String id);
	
	/**
	 * 获取备案详情
	 *
	 * @param    id
	**/
	public RecordEntity getRecord(String id);
	
	/**
	 * 查询备案列表
	 *
	 * @param    query
	**/
	public ResultList<RecordEntity> queryRecord(RecordQuery query);
	
	/**
	 * 绑定合同
	 *
	 * @param    contractNo
	 * @param    refContractNo
	 * @param    recordId
	**/
	public void bindingContract(String contractNo, String refContractNo, String recordId);
}