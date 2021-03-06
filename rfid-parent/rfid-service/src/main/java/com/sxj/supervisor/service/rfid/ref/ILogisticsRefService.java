package com.sxj.supervisor.service.rfid.ref;

import java.sql.SQLException;
import java.util.List;

import com.sxj.supervisor.entity.rfid.ref.LogisticsRefEntity;
import com.sxj.supervisor.model.rfid.ref.LogisticsRefQuery;
import com.sxj.util.exception.ServiceException;

public interface ILogisticsRefService {
	/**
	 * 高级查询
	 */
	public List<LogisticsRefEntity> query(LogisticsRefQuery query)
			throws ServiceException;

	/**
	 * 新增
	 * 
	 * @param app
	 * @throws ServiceException
	 */
	public void add(LogisticsRefEntity model) throws ServiceException,
			SQLException;

	/**
	 * 更新
	 * 
	 * @param app
	 * @throws ServiceException
	 */
	public void update(LogisticsRefEntity model) throws ServiceException;

	/**
	 * 获取信息
	 * 
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public LogisticsRefEntity getRef(String id) throws ServiceException;

	/**
	 * 逻辑删除
	 * 
	 * @param app
	 * @throws ServiceException
	 */
	public void del(String id) throws ServiceException;
}
