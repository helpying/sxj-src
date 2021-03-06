package com.sxj.supervisor.enu.rfid.windowRef;


public enum LinkStateEnum {
	windowApply("门窗申请", 0),rfidLoss("RFID补损", 1),windowLoss("门窗补损", 2);

	// 成员变量
	private Integer id;

	private String name;

	private LinkStateEnum(String name, Integer id) {
		this.name = name;
		this.id = id;
	}

	// 普通方法
	public static String getName(Integer id) {
		for (LinkStateEnum c : LinkStateEnum.values()) {
			if (c.getId() == id) {
				return c.name;
			}
		}
		return null;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
