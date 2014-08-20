package com.sxj.mybatis.orm.model;

import com.sxj.mybatis.orm.annotations.Column;
import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.GeneratedValue;
import com.sxj.mybatis.orm.annotations.GenerationType;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.orm.mapper.FunctionMapper;
import com.sxj.mybatis.sn.annotations.Sn;

@Entity(mapper = FunctionMapper.class)
@Table(name = "TEST_FUNCTION")
public class Function
{
    @Id(column = "ID")
    @GeneratedValue(strategy = GenerationType.UUID, length = 31)
    private String functionId;
    
    @Sn(table = "T_SN", stub = "F_SN_NAME", stubValue = "function", sn = "F_SN_NUMBER", step = 1, pattern = "0000")
    @Column(name = "TITLE")
    private String functionName;
    
    public String getFunctionId()
    {
        return functionId;
    }
    
    public void setFunctionId(String functionId)
    {
        this.functionId = functionId;
    }
    
    public String getFunctionName()
    {
        return functionName;
    }
    
    public void setFunctionName(String functionName)
    {
        this.functionName = functionName;
    }
}
