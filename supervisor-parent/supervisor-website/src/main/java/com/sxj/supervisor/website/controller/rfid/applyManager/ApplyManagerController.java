﻿package com.sxj.supervisor.website.controller.rfid.applyManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sxj.supervisor.entity.member.MemberEntity;
import com.sxj.supervisor.entity.rfid.apply.RfidApplicationEntity;
import com.sxj.supervisor.enu.rfid.apply.ReceiptStateEnum;
import com.sxj.supervisor.enu.rfid.applyManager.M_PayStateEnum;
import com.sxj.supervisor.model.rfid.app.RfidApplicationQuery;
import com.sxj.supervisor.service.rfid.app.IRfidApplicationService;
import com.sxj.supervisor.website.controller.BaseController;
import com.sxj.util.exception.WebException;
import com.sxj.util.logger.SxjLogger;

@Controller
@RequestMapping("/rfid/applyManager")
public class ApplyManagerController extends BaseController {
	@Autowired
	private IRfidApplicationService appService;

	/**
	 * 物流标签申请管理列表
	 * 
	 * @param map
	 * @param query
	 * @return
	 */
	@RequestMapping("applyManager_list")
	public String applyManager_list(ModelMap map, RfidApplicationQuery query,
			HttpSession session) throws WebException {
		try {
			if (query != null) {
				query.setPagable(true);
			}
			MemberEntity member = getLoginInfo(session).getMember();
			query.setMemberNo(member.getMemberNo());
			List<RfidApplicationEntity> list = appService.query(query);
			ReceiptStateEnum[] receipt_states = ReceiptStateEnum.values();
			M_PayStateEnum[] m_pay_states = M_PayStateEnum.values();
			map.put("receipt_states", receipt_states);
			map.put("m_pay_states", m_pay_states);
			map.put("list", list);
			map.put("query", query);
		} catch (Exception e) {
			SxjLogger.error("物流标签申请管理列表错误", e, this.getClass());
			throw new WebException("物流标签申请管理列表错误");
		}
		return "site/rfid/applyManager/gysordermgr";
	}

	/**
	 * 删除
	 */
	@RequestMapping("del")
	public @ResponseBody Map<String, String> del(String id, String applyNo)
			throws WebException {
		try {
			Map<String, String> map = new HashMap<String, String>();
			Boolean flag = appService.delApp(id, applyNo);
			if (flag) {
				map.put("flag", "ok");
			} else {
				map.put("flag", "no");
			}
			return map;
		} catch (Exception e) {
			SxjLogger.error("物流标签申请管理列表删除错误", e, this.getClass());
			throw new WebException("物流标签申请管理列表删除错误");
		}
	}
}
