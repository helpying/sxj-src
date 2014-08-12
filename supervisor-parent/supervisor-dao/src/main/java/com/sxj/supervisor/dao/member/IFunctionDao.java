package com.sxj.supervisor.dao.member;

import java.util.List;

import com.sxj.supervisor.entity.member.FunctionEntity;
import com.sxj.util.persistent.QueryCondition;

public interface IFunctionDao {
	/**
	 * 添加系统功能
	 *
	 * @param    function
	**/
	public void addFunction(FunctionEntity function);
	
	/**
	 * 修改系统功能
	 *
	 * @param    function
	**/
	public void updateFunction(FunctionEntity function);
	
	/**
	 * 删除系统功能
	 *
	 * @param    id
	**/
	public void deleteFunction(String id);
	
	/**
	 * 系统功能高级查询
	 *
	 * @param    function
	**/
	public List<FunctionEntity> queryFunction(QueryCondition<FunctionEntity> query);
}