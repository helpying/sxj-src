package com.sxj.supervisor.service.system;

import java.util.List;

import com.sxj.supervisor.entity.system.FunctionEntity;
import com.sxj.supervisor.model.function.FunctionModel;

public interface IFunctionService {

	/**
	 * 读取所有菜单
	 * 
	 * @return
	 */
	public List<FunctionModel> queryFunctions();

	/**
	 * 读取所有菜单
	 * 
	 * @return
	 */
	public List<FunctionEntity> queryChildrenFunctions(String parentId);

	/**
	 * 根据获取系统功能信息
	 * 
	 * @param id
	 * @return
	 */
	public FunctionEntity getFunction(String id);
}