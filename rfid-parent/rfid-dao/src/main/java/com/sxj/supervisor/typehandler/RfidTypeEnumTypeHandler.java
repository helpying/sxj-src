package com.sxj.supervisor.typehandler;

import org.apache.ibatis.type.EnumOrdinalTypeHandler;

import com.sxj.supervisor.enu.rfid.RfidTypeEnum;

public class RfidTypeEnumTypeHandler extends
		EnumOrdinalTypeHandler<RfidTypeEnum> {

	public RfidTypeEnumTypeHandler(Class<RfidTypeEnum> type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

}
