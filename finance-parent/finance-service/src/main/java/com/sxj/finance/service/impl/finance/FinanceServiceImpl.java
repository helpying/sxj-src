package com.sxj.finance.service.impl.finance;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sxj.finance.dao.finance.FinanceDao;
import com.sxj.finance.entity.FinanceEntity;
import com.sxj.finance.enu.finance.PayStageEnum;
import com.sxj.finance.model.finance.FinanceModel;
import com.sxj.finance.service.finance.IFinanceService;
import com.sxj.util.common.ISxjHttpClient;
import com.sxj.util.exception.ServiceException;
import com.sxj.util.logger.SxjLogger;
import com.sxj.util.persistent.QueryCondition;

@Service
public class FinanceServiceImpl implements IFinanceService {

	private final String url = "http://127.0.0.1:8080/supervisor-website/pay/changeState.htm";

	@Autowired
	private FinanceDao financeDao;

	@Autowired
	private ISxjHttpClient cl;

	@Override
	@Transactional(readOnly = true)
	public List<FinanceEntity> queryWebSite(FinanceModel query)
			throws ServiceException {
		try {
			QueryCondition<FinanceEntity> condition = new QueryCondition<FinanceEntity>();
			if (query != null) {
				condition.addCondition("memberNo", query.getMemberNo());// 会员号
				condition.addCondition("payNo", query.getPayNo());// 供应商ID
				condition.addCondition("contractNo", query.getContractNo());// 供应商名称
				condition.addCondition("state", query.getState());
				condition.setPage(query);
			}
			List<FinanceEntity> list = financeDao.queryWebSite(condition);
			query.setPage(condition);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			SxjLogger.error(e.getMessage(), e, this.getClass());
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<FinanceEntity> queryManage(FinanceModel query)
			throws ServiceException {
		try {
			QueryCondition<FinanceEntity> condition = new QueryCondition<FinanceEntity>();
			if (query != null) {
				condition.addCondition("payNo", query.getPayNo());// 供应商ID
				condition.addCondition("contractNo", query.getContractNo());// 供应商名称
				condition.addCondition("state", query.getState());
				condition.setPage(query);
			}
			List<FinanceEntity> list = financeDao.queryManage(condition);
			query.setPage(condition);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			SxjLogger.error(e.getMessage(), e, this.getClass());
		}
		return null;
	}

	@Override
	@Transactional
	public Boolean apply(FinanceEntity fe) throws ServiceException {
		try {
			FinanceEntity f = financeDao.getFinanceEntityById(fe.getId());
			if (f.getState().ordinal() == 0) {
				fe.setState(PayStageEnum.Stage2_0);
				fe.setApplyDate(new Date());
				financeDao.update(fe);
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			SxjLogger.error(e.getMessage(), e, this.getClass());
			return false;
		}
		return true;
	}

	@Override
	@Transactional
	public Boolean pay(FinanceEntity fe) throws ServiceException {
		try {
			FinanceEntity f = financeDao.getFinanceEntityById(fe.getId());
			if (f.getState().ordinal() == 2) {
				fe.setState(PayStageEnum.Stage2_2);
				fe.setPayDate(new Date());
				financeDao.update(fe);
				Map<String, String> map = new HashMap<String, String>();
				map.put("payNo", f.getPayNo());
				map.put("state", "2");
				cl.sslPost(url, map, "");
			} else {
				return false;
			}
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			return false;
		}
		return true;
	}

	@Override
	@Transactional
	public Boolean shelve(FinanceEntity fe) throws ServiceException {
		try {
			FinanceEntity f = financeDao.getFinanceEntityById(fe.getId());
			if (f.getState().ordinal() == 1) {
				fe.setState(PayStageEnum.Stage2_3);
				fe.setShelveDate(new Date());
				financeDao.update(fe);
				Map<String, String> map = new HashMap<String, String>();
				map.put("payNo", f.getPayNo());
				map.put("state", "3");
				cl.sslPost(url, map, "");
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			SxjLogger.error(e.getMessage(), e, this.getClass());
			return false;
		}
		return true;
	}

	@Override
	@Transactional
	public Boolean accept(FinanceEntity fe) throws ServiceException {
		try {
			FinanceEntity f = financeDao.getFinanceEntityById(fe.getId());
			if (f.getState().ordinal() == 1) {
				fe.setState(PayStageEnum.Stage2_1);
				fe.setAcceptDate(new Date());
				financeDao.update(fe);
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			SxjLogger.error(e.getMessage(), e, this.getClass());
			return false;
		}
		return true;
	}

	@Override
	@Transactional
	public String setModel(Map<String, Object> map) throws ServiceException {
		try {
			FinanceEntity fe = new FinanceEntity();
			fe.setMemberNo(map.get("memberNo_A").toString());
			fe.setPayNo(map.get("payNo").toString());
			fe.setContractNo(map.get("contractNo").toString());
			fe.setBatchNo(map.get("batchNo").toString());
			fe.setPayAmount(Double.valueOf(map.get("payAmount").toString()));
			fe.setContent(map.get("content").toString());
			fe.setState(PayStageEnum.Stage1);
			fe.setCreatDate(new Date());
			FinanceModel query = new FinanceModel();
			query.setPayNo(fe.getPayNo());
			List<FinanceEntity> list = queryManage(query);
			if (list == null || list.size() == 0) {
				financeDao.add(fe);
			}
			return "1";
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			throw new ServiceException("增加数据出错", e);
		}
	}
}
