package com.sxj.supervisor.service.impl.rfid.open;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sxj.spring.modules.mapper.JsonMapper;
import com.sxj.supervisor.dao.contract.IContractBatchDao;
import com.sxj.supervisor.dao.contract.IContractDao;
import com.sxj.supervisor.dao.contract.IContractModifyBatchDao;
import com.sxj.supervisor.dao.contract.IContractReplenishBatchDao;
import com.sxj.supervisor.dao.rfid.logistics.ILogisticsRfidDao;
import com.sxj.supervisor.dao.rfid.ref.ILogisticsRefDao;
import com.sxj.supervisor.dao.rfid.window.IWindowRfidDao;
import com.sxj.supervisor.dao.rfid.windowRef.IWindowRfidRefDao;
import com.sxj.supervisor.entity.contract.ContractBatchEntity;
import com.sxj.supervisor.entity.contract.ContractEntity;
import com.sxj.supervisor.entity.contract.ModifyBatchEntity;
import com.sxj.supervisor.entity.contract.ReplenishBatchEntity;
import com.sxj.supervisor.entity.pay.PayRecordEntity;
import com.sxj.supervisor.entity.rfid.logistics.LogisticsRfidEntity;
import com.sxj.supervisor.entity.rfid.ref.LogisticsRefEntity;
import com.sxj.supervisor.entity.rfid.window.WindowRfidEntity;
import com.sxj.supervisor.entity.rfid.windowRef.WindowRefEntity;
import com.sxj.supervisor.enu.contract.PayContentStateEnum;
import com.sxj.supervisor.enu.contract.PayModeEnum;
import com.sxj.supervisor.enu.contract.PayStageEnum;
import com.sxj.supervisor.enu.contract.PayTypeEnum;
import com.sxj.supervisor.enu.rfid.logistics.LabelStateEnum;
import com.sxj.supervisor.model.contract.BatchItemModel;
import com.sxj.supervisor.model.contract.ContractModel;
import com.sxj.supervisor.model.open.Bacth;
import com.sxj.supervisor.model.open.BatchModel;
import com.sxj.supervisor.model.open.BatchNo;
import com.sxj.supervisor.model.open.ContractNo;
import com.sxj.supervisor.model.open.WinTypeModel;
import com.sxj.supervisor.service.contract.IContractPayService;
import com.sxj.supervisor.service.contract.IContractService;
import com.sxj.supervisor.service.rfid.open.IOpenRfidService;
import com.sxj.util.exception.ServiceException;
import com.sxj.util.logger.SxjLogger;
import com.sxj.util.persistent.QueryCondition;

@Service
@Transactional
public class OpenRfidServiceImpl implements IOpenRfidService {
	/**
	 * 批次DAO
	 */
	@Autowired
	private IContractBatchDao contractBatchDao;
	/**
	 * 变更合同批次DAO
	 */
	@Autowired
	private IContractModifyBatchDao contractModifyBatchDao;
	/**
	 * 补损合同批次
	 */
	@Autowired
	private IContractReplenishBatchDao contractReplenishBatchDao;

	@Autowired
	private ILogisticsRfidDao logisticsDao;

	@Autowired
	private ILogisticsRefDao logisticsRefDao;

	@Autowired
	private IWindowRfidDao windowRfidDao;

	@Autowired
	private IContractDao contractDao;

	@Autowired
	private IContractService contractService;
	@Autowired
	private IContractPayService contractPayService;

	@Autowired
	private IWindowRfidRefDao windowRfidRefDao;

	@Override
	public BatchModel getBatchByRfid(String rfid) throws ServiceException,
			SQLException {
		BatchModel batchModel = new BatchModel();
		Bacth batch = new Bacth();
		QueryCondition<LogisticsRfidEntity> logisticsQuery = new QueryCondition<LogisticsRfidEntity>();
		logisticsQuery.addCondition("rfidNo", rfid);
		List<LogisticsRfidEntity> ref = logisticsDao
				.queryLogisticsRfidList(logisticsQuery);
		QueryCondition<LogisticsRefEntity> logisticsRefQuery = new QueryCondition<LogisticsRefEntity>();
		logisticsRefQuery.addCondition("rfidNo", rfid);
		List<LogisticsRefEntity> logisticsRef = logisticsRefDao
				.queryList(logisticsRefQuery);
		if (logisticsRef != null && logisticsRef.size() > 0) {
			LogisticsRefEntity lRef = logisticsRef.get(0);
			if (lRef.getState().getId() == 1) {//
				if (ref != null && ref.size() > 0) {
					LogisticsRfidEntity le = ref.get(0);
					ContractNo contract = new ContractNo();
					contract.setContractNo(le.getContractNo());
					batchModel.setContract(contract);// 封装合同号
					if (le.getProgressState().getId() == 0) {

						QueryCondition<ContractBatchEntity> query = new QueryCondition<ContractBatchEntity>();
						query.addCondition("rfidNo", rfid);
						query.addCondition("state", 1);// 是否已变更
						List<ContractBatchEntity> cbatchList = contractBatchDao
								.queryBacths(query);// 合同批次
						QueryCondition<ModifyBatchEntity> modifyQuery = new QueryCondition<ModifyBatchEntity>();
						modifyQuery.addCondition("rfidNo", rfid);
						modifyQuery.addCondition("state", 1);// 是否已变更
						List<ModifyBatchEntity> modifyBatch = contractModifyBatchDao
								.queryBacths(modifyQuery);// 变更批次
						QueryCondition<ReplenishBatchEntity> replenishQuery = new QueryCondition<ReplenishBatchEntity>();
						replenishQuery.addCondition("newRfidNo", rfid);
						// 补损批次
						List<ReplenishBatchEntity> batchList = contractReplenishBatchDao
								.queryReplenishBatch(replenishQuery);

						if (cbatchList != null && cbatchList.size() > 0) {
							ContractBatchEntity cbe = cbatchList.get(0);
							BatchNo BatchNo = new BatchNo();
							BatchNo.setBatchNo(cbe.getBatchNo());
							batch.setBatch(BatchNo);
							List<BatchItemModel> batchModelList = this
									.jsonChangeList(cbe.getBatchItems());
							batch.setBatchItems(batchModelList);
						}
						if (modifyBatch != null && modifyBatch.size() > 0) {
							ModifyBatchEntity modiy = modifyBatch.get(0);
							Bacth bacth = new Bacth();
							BatchNo BatchNo = new BatchNo();
							BatchNo.setBatchNo(modiy.getBatchNo());
							bacth.setBatch(BatchNo);
							List<BatchItemModel> batchModelList = this
									.jsonChangeList(modiy.getBatchItems());
							batch.setBatchItems(batchModelList);
						}
						if (batchList != null && batchList.size() > 0) {
							ReplenishBatchEntity rbe = batchList.get(0);
							Bacth bacth = new Bacth();
							BatchNo BatchNo = new BatchNo();
							// BatchNo.setBatchNo(rbe.getBatchNo());
							bacth.setBatch(BatchNo);
							List<BatchItemModel> batchModelList = this
									.jsonChangeList(rbe.getBatchItems());
							batch.setBatchItems(batchModelList);
						}
						if (batch.getBatchItems() != null) {
							batch.setState("1");
						} else {
							batch.setState("2");
						}
					} else {
						batch.setState("4");
					}

				}
			} else {
				batch.setState("3");
			}
			batchModel.setBatchList(batch);
		}

		return batchModel;
	}

	/**
	 * json转化list
	 * 
	 * @param json
	 * @return
	 */
	public List<BatchItemModel> jsonChangeList(String json) {
		List<BatchItemModel> bacthList = new ArrayList<BatchItemModel>();
		try {
			bacthList = JsonMapper.nonEmptyMapper().getMapper()
					.readValue(json, new TypeReference<List<BatchItemModel>>() {
					});
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bacthList;

	}

	/**
	 * 获取门窗
	 */
	@Override
	public WinTypeModel getWinTypeByRfid(String rfid) throws ServiceException,
			SQLException {
		QueryCondition<WindowRfidEntity> query = new QueryCondition<WindowRfidEntity>();
		query.addCondition("rfidNo", rfid);

		List<WindowRfidEntity> win = windowRfidDao.queryWindowRfidList(query);
		QueryCondition<WindowRefEntity> refQuery = new QueryCondition<WindowRefEntity>();
		refQuery.addCondition("rfidNo", rfid);
		List<WindowRefEntity> winRef = windowRfidRefDao
				.queryWindowRfidRefList(refQuery);

		WinTypeModel wtm = new WinTypeModel();
		if (winRef != null && winRef.size() > 0) {
			WindowRefEntity windowRef = winRef.get(0);
			if (windowRef.getState().getId() == 1) {
				if (win != null && win.size() > 0) {
					WindowRfidEntity wre = win.get(0);
					wtm.setContratcNo(wre.getContractNo());
					wtm.setRfidNo(wre.getRfidNo());
					if (wre.getWindowType() != null) {
						wtm.setWinType(wre.getWindowType().getName());
					}
					wtm.setState("1");// 成功
				} else {
					wtm.setState("2");// 未启用
				}
			} else {
				wtm.setState("3");// 未审核
			}
		} else {
			wtm.setState("2");// 未启用
		}
		return wtm;
	}

	/**
	 * 获取合同地址
	 */
	@Override
	public String getAddress(String contractNo) throws ServiceException,
			SQLException {
		QueryCondition<ContractEntity> query = new QueryCondition<ContractEntity>();
		query.addCondition("contractType", "0");
		query.addCondition("contractNo", contractNo);
		List<ContractEntity> ceList = contractDao.queryContract(query);
		String address = "";
		if (ceList != null && ceList.size() > 0) {
			address = ceList.get(0).getEngAddress();
		}
		return address;
	}

	/**
	 * 发货
	 */
	@Override
	@Transactional
	public int shipped(String rfid) throws ServiceException, SQLException,
			JsonParseException, JsonMappingException, IOException {
		try {
			QueryCondition<LogisticsRfidEntity> logisticsQuery = new QueryCondition<LogisticsRfidEntity>();
			logisticsQuery.addCondition("rfidNo", rfid);
			List<LogisticsRfidEntity> ref = logisticsDao
					.queryLogisticsRfidList(logisticsQuery);
			if (ref != null && ref.size() > 0) {
				LogisticsRfidEntity le = ref.get(0);
				if (le.getProgressState().getId() == 2) {
					le.setProgressState(LabelStateEnum.installed);
					logisticsDao.updateLogisticsRfid(le);
					// 更新出库状态
					ContractBatchEntity contractBatch = contractBatchDao
							.getBacthsByRfid(rfid);
					if (contractBatch != null) {
						if (contractBatch.getType() == 1) {
							ContractBatchEntity cbe = new ContractBatchEntity();
							cbe.setId(contractBatch.getId());
							cbe.setWarehouseState(1);
							contractBatchDao.updateBatch(cbe);
						} else if (contractBatch.getType() == 2) {
							ModifyBatchEntity modifyBatch = new ModifyBatchEntity();
							modifyBatch.setId(contractBatch.getId());
							modifyBatch.setWarehouseState(1);
							contractModifyBatchDao.updateBatch(modifyBatch);
						} else if (contractBatch.getType() == 3) {
							ReplenishBatchEntity replenishBatch = new ReplenishBatchEntity();
							replenishBatch.setId(contractBatch.getId());
							replenishBatch.setWarehouseState(1);
							contractReplenishBatchDao
									.updateBatch(replenishBatch);
						}
					}
					return 1;
				} else {
					return 2;
				}
			}
		} catch (ServiceException e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			throw new ServiceException(e.getMessage());
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			throw new ServiceException("更新批次错误", e);
		}

		return 0;
	}

	/**
	 * 验收
	 */
	@Override
	@Transactional
	public int accepting(String rfid) throws ServiceException {
		try {
			QueryCondition<LogisticsRfidEntity> logisticsQuery = new QueryCondition<LogisticsRfidEntity>();
			logisticsQuery.addCondition("rfidNo", rfid);
			List<LogisticsRfidEntity> ref = logisticsDao
					.queryLogisticsRfidList(logisticsQuery);
			if (ref != null && ref.size() > 0) {
				LogisticsRfidEntity le = ref.get(0);
				if (le.getProgressState().getId() == 3) {
					le.setProgressState(LabelStateEnum.hasQuality);
					logisticsDao.updateLogisticsRfid(le);
					// 获取合同信息
					ContractModel cm = contractService
							.getContractModelByContractNo(le.getContractNo());
					if (cm != null) {
						// 获取批次信息
						ContractBatchEntity cb = contractBatchDao
								.getBacthsByRfid(rfid);
						// 生成支付单
						PayRecordEntity pay = new PayRecordEntity();
						pay.setMemberNo_A(cm.getContract().getMemberIdA());
						pay.setMemberName_A(cm.getContract().getMemberNameA());
						pay.setMemberNo_B(cm.getContract().getMemberIdB());
						pay.setMemberName_B(cm.getContract().getMemberNameB());
						pay.setContractNo(cm.getContract().getContractNo());
						pay.setRfidNo(rfid);
						pay.setDateNo(cm.getContract().getContractNo() + "P");// 编号
						pay.setBatchNo(cb.getBatchNo());
						pay.setPayAmount(cb.getAmount());
						if (cm.getContract().getType().getId() == 1) {
							pay.setType(PayTypeEnum.glass);
							pay.setContent("第" + cb.getBatchNo() + "批次玻璃货款");
						} else if (cm.getContract().getType().getId() == 2) {
							pay.setType(PayTypeEnum.extruders);
							pay.setContent("第" + cb.getBatchNo() + "批次型材货款");
						}
						pay.setState(PayStageEnum.Stage1);
						pay.setPayMode(PayModeEnum.cash);
						pay.setPayContentState(PayContentStateEnum.payment);
						contractPayService.addPayRecordEntity(pay);// 生成支付单
						return 1;
					}
				}
			}
			return 0;
		} catch (ServiceException e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			throw new ServiceException(e.getMessage());
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			throw new ServiceException("更新批次错误", e);
		}
	}
}
