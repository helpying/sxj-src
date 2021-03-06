package com.sxj.supervisor.open.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sxj.spring.modules.mapper.JsonMapper;
import com.sxj.supervisor.entity.member.AccountEntity;
import com.sxj.supervisor.entity.member.MemberEntity;
import com.sxj.supervisor.enu.member.AccountStatesEnum;
import com.sxj.supervisor.model.login.SupervisorPrincipal;
import com.sxj.supervisor.model.open.BatchModel;
import com.sxj.supervisor.model.open.WinTypeModel;
import com.sxj.supervisor.rfid.login.ApiShiroRedisCache;
import com.sxj.supervisor.rfid.login.ApiToken;
import com.sxj.supervisor.service.open.member.IAccountService;
import com.sxj.supervisor.service.open.member.IMemberService;
import com.sxj.supervisor.service.rfid.open.IOpenRfidService;
import com.sxj.supervisor.service.rfid.window.IWindowRfidService;
import com.sxj.util.common.StringUtils;
import com.sxj.util.exception.ServiceException;
import com.sxj.util.logger.SxjLogger;

@Controller
@RequestMapping("/rfid")
public class OpenRfidController {
	@Autowired
	private IAccountService accountService;

	@Autowired
	private IOpenRfidService openRfidService;

	@Autowired
	private IMemberService memServive;

	@Autowired
	private IWindowRfidService windowRfid;

	/**
	 * 登陆
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "login")
	public @ResponseBody Map<String, Integer> login(String userId,
			String password, HttpSession session) {
		Map<String, Integer> retVal = new HashMap<String, Integer>();
		try {
			System.err.println(userId + "++++" + password);
			ApiToken token = null;
			SupervisorPrincipal userBean = null;
			AccountEntity account = null;

			if (StringUtils.isNotEmpty(userId)) {
				account = accountService.getAccountByAccountNo(userId);
				if (account == null) {
					retVal.put("state", 0);
					return retVal;
				}
				if (AccountStatesEnum.stop.equals(account.getState())) {
					retVal.put("state", 0);
					return retVal;
				}
				userBean = new SupervisorPrincipal();
				userBean.setAccount(account);
				MemberEntity member = memServive.memberInfo(account
						.getParentId());
				userBean.setMember(member);
				token = new ApiToken(userBean, password);
			} else {
				retVal.put("state", 0);
				return retVal;
			}

			Subject currentUser = SecurityUtils.getSubject();
			try {
				currentUser.login(token);
				PrincipalCollection principals = currentUser.getPrincipals();
				if (userBean.getAccount() != null) {
					ApiShiroRedisCache.addToMap(userBean.getAccount().getId(),
							principals);
				}
			} catch (AuthenticationException e) {
				SxjLogger.error(e.getMessage(), e, this.getClass());
				retVal.put("state", -1);
				return retVal;

			}
			if (currentUser.isAuthenticated()) {
				session.setAttribute("userinfo", userBean);
				if (account != null) {
					accountService.edit_Login(account.getId());
				}
				retVal.put("state", 1);
			} else {
				retVal.put("state", 1);
			}
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			retVal.put("state", -1);
			return retVal;
		}
		return retVal;
	}

	/**
	 * 注销
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "logout")
	@ResponseBody
	public Map<String, Integer> logout(String userId) {
		Map<String, Integer> retVal = new HashMap<String, Integer>();
		try {
			Subject currentUser = SecurityUtils.getSubject();
			currentUser.logout();
			retVal.put("state", 1);
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			retVal.put("state", 0);
		}
		return retVal;
	}

	/**
	 * 获取批次信息
	 * 
	 * @param rfidNo
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value = "info/batch/{rfidNo}")
	public @ResponseBody BatchModel getRfidBatchInfo(
			@PathVariable String rfidNo, HttpServletResponse response) {
		try {
			BatchModel model = openRfidService.getBatchByRfid(rfidNo);
			response.setContentType("application/json; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(JsonMapper.nonEmptyMapper().toJson(model));
			out.flush();
			out.close();
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			return null;
		}
		return null;
	}

	/**
	 * 获取合同规格信息
	 * 
	 * @param rfidNo
	 * @return
	 * @throws SQLException
	 * @throws ServiceException
	 */
	@RequestMapping(value = "info/contract/{rfidNo}")
	public @ResponseBody WinTypeModel getRfidContractInfo(
			@PathVariable String rfidNo, HttpServletResponse response) {

		try {
			WinTypeModel win = openRfidService.getWinTypeByRfid(rfidNo);
			response.setContentType("application/json; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(JsonMapper.nonEmptyMapper().toJson(win));
			out.flush();
			out.close();
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			return null;
		}
		return null;
	}

	/**
	 * 发货(出库)
	 * 
	 * @param rfidNo
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws ServiceException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = "send/{rfidNo}")
	public @ResponseBody Map<String, Object> sendGoods(
			@PathVariable String rfidNo) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {

			int state = openRfidService.shipped(rfidNo);
			if (state == 1) {
				map.put("state", 1);
				map.put("message", "发货成功");
			} else if (state == 2) {
				map.put("state", 2);
				map.put("message", "不能重复发货");
			} else {
				map.put("state", 0);
				map.put("message", "发货失败");
			}
		} catch (ServiceException | SQLException | IOException e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			map.put("state", 0);
			map.put("message", "发货失败");
		}
		return map;

	}

	/**
	 * 验收
	 * 
	 * @param rfidNo
	 * @return
	 */
	@RequestMapping(value = "check/{rfidNo}")
	public @ResponseBody Map<String, Object> checkAndAccept(
			@PathVariable String rfidNo) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int state = openRfidService.accepting(rfidNo);
			if (state == 1) {
				map.put("state", 1);
				map.put("message", "验收成功");
			} else {
				map.put("state", 0);
				map.put("message", "验收失败");
			}
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			map.put("state", 0);
			map.put("message", "验收失败");
		}
		return map;

	}

	/**
	 * 质检
	 * 
	 * @param rfidNo
	 * @return
	 */
	@RequestMapping(value = "test")
	public @ResponseBody Map<String, Object> testRfid(String contractNo,
			String[] rfidNos) {
		System.err.println(contractNo + "++++" + rfidNos.toString());
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int stepState = windowRfid.testWindow(contractNo, rfidNos);
			if (stepState == 1) {
				map.put("state", "1");
				map.put("message", "质检成功");
			} else {
				map.put("state", "0");
				map.put("message", "质检失败");
			}
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			map.put("state", "0");
			map.put("message", "质检失败");
		}
		return map;
	}

	/**
	 * 安装
	 * 
	 * @param rfidNo
	 * @return
	 */
	@RequestMapping(value = "setup/{rfidNo}")
	public @ResponseBody Map<String, Object> setupRfid(
			@PathVariable String rfidNo) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int stepState = windowRfid.stepWindow(rfidNo);
			if (stepState == 1) {
				map.put("state", "1");
				map.put("message", "安装成功");
			} else {
				map.put("state", "0");
				map.put("message", "安装失败");
			}

		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			map.put("state", "0");
			map.put("message", "安装失败");
		}
		return map;

	}

	/**
	 * 获取合同地址信息
	 * 
	 * @param rfidNo
	 * @return
	 * @throws SQLException
	 * @throws ServiceException
	 */
	@RequestMapping(value = "info/address/{contractNo}")
	public @ResponseBody Map<String, Object> getAddress(
			@PathVariable String contractNo) throws ServiceException,
			SQLException {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			String address = openRfidService.getAddress(contractNo);
			map.put("address", address);
			return map;
		} catch (Exception e) {
			SxjLogger.error(e.getMessage(), e, this.getClass());
			return null;
		}
	}

}
