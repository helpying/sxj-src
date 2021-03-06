package com.sxj.supervisor.service.impl.contract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sxj.supervisor.dao.contract.IAccountingDao;
import com.sxj.supervisor.dao.contract.IContractPayDao;
import com.sxj.supervisor.entity.pay.PayRecordEntity;
import com.sxj.supervisor.enu.contract.PayStageEnum;
import com.sxj.supervisor.model.contract.ContractPayModel;
import com.sxj.supervisor.model.statistics.AccountingModel;
import com.sxj.supervisor.service.contract.IContractPayService;
import com.sxj.supervisor.service.contract.IContractService;
import com.sxj.util.exception.ServiceException;
import com.sxj.util.logger.SxjLogger;
import com.sxj.util.persistent.QueryCondition;

@Service
public class ContractPayServiceImpl implements IContractPayService {
	@Autowired
	private IContractPayDao payDao;

	@Autowired
	private IAccountingDao accountingDao;

	@Autowired
	private IContractService contractService;

	@Override
	@Transactional(readOnly = true)
	public List<PayRecordEntity> queryPayList(ContractPayModel query)
			throws ServiceException {
		try {
			if (query == null) {
				return null;
			}
			QueryCondition<PayRecordEntity> condition = new QueryCondition<PayRecordEntity>();
			condition.addCondition("memberNo", query.getMemberNo());// 会员号
			condition.addCondition("memberType", query.getMemberType());// 会员号
			condition.addCondition("payNo", query.getPayNo());// 支付单号
			condition.addCondition("contractNo", query.getContractNo());// 合同号
			condition.addCondition("rfidNo", query.getRfidNo());// Rfid编号
			condition.addCondition("startPayDate", query.getStartPayDate());// 开始时间
			condition.addCondition("endPayDate", query.getEndPayDate());// 结束时间
			condition.addCondition("state", query.getState());//
			condition.addCondition("memberName_A", query.getMemberName_A());//
			condition.addCondition("payMode", query.getPayMode());//
			condition.addCondition("type", query.getType());//
			condition.addCondition("PayContentState",
					query.getPayContentState());//
			condition.setPage(query);
			List<PayRecordEntity> payList = payDao.queryPayContract(condition);
			query.setPage(condition);
			return payList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("查询付款管理出错", e);
		}
	}

	@Override
	public void update_state(String id, Integer state) throws ServiceException {
		// TODO Auto-generated method stub

	}

	/**
	 * 甲方付款
	 */
	@Override
	@Transactional
	public String pay(String id, Double payReal) throws ServiceException {
		try {
			PayRecordEntity re = payDao.getPayRecordEntity(id);
			PayStageEnum[] payState = PayStageEnum.values();
			if (re.getState().ordinal() < 4) {
				re.setPayReal(payReal);
				re.setState(payState[4]);
				payDao.update_pay(re);
				return "ok";
			} else {
				return "false";
			}
		} catch (Exception e) {
			throw new ServiceException("甲方付款出错！", e);
		}
	}

	/**
	 * 乙方确认收款
	 */
	@Override
	@Transactional
	public String pay_ok(String id) throws ServiceException {
		try {
			PayRecordEntity re = payDao.getPayRecordEntity(id);
			PayStageEnum[] payState = PayStageEnum.values();
			if (re.getState().ordinal() == 4) {
				re.setState(payState[5]);
				payDao.update_pay(re);
				contractService.modifyBatchPayState(re.getContractNo(),
						re.getRfidNo(), re.getPayNo());
				return "ok";
			} else {
				return "false";
			}
		} catch (Exception e) {
			throw new ServiceException("乙方确认付款出错！", e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<AccountingModel> query_finance(AccountingModel query,
			String startDate, String endDate) throws ServiceException {
		try {
			QueryCondition<AccountingModel> condition = new QueryCondition<AccountingModel>();
			condition.addCondition("recordNo", query.getRecordNo());// 备案号
			condition.addCondition("contractNo", query.getContractNo());// 合同号
			condition.addCondition("contractType", query.getContractType());// 合同类型
			condition.addCondition("startDate", startDate);// 开始时间
			condition.addCondition("endDate", endDate);// 结束时间
			condition.setPage(query);
			List<AccountingModel> list = accountingDao.query(condition);
			query.setPage(condition);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("财务统计查询出错！", e);
		}
	}

	@Override
	@Transactional
	public PayRecordEntity getPayRecordEntity(String id)
			throws ServiceException {
		try {
			PayRecordEntity pay = payDao.getPayRecordEntity(id);
			return pay;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	@Transactional
	public void addPayRecordEntity(PayRecordEntity pay) throws ServiceException {
		try {
			payDao.addPay(pay);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("新增付款管理出错！", e);
		}

	}

	@Override
	@Transactional(readOnly = true)
	public String getPayNoByRfidNo(String rfidNo) throws ServiceException {
		try {
			PayRecordEntity pay = payDao.getEntityByRfidNo(rfidNo);
			return pay.getPayNo();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("查询支付单号出错！", e);
		}
	}

	@Override
	public String changeState(String payNo, String state)
			throws ServiceException {
		try {
			payDao.changeState(payNo, state);
			return "1";
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			throw new ServiceException();
		}
	}
}
