package com.ruoyi.common.enums;

/**
 * 用户状态
 * <p>
 * 【使用者】SysLoginService.login()登录校验时用它比对sys_user.status/del_flag：
 * status=1停用抛"账号已停用"、del_flag=2抛"账号已删除"。枚举双字段(code数据库值+info显示文案)是若依标准写法。
 * 
 * @author ruoyi
 */
public enum UserStatus
{
    OK("0", "正常"), DISABLE("1", "停用"), DELETED("2", "删除");

    private final String code;
    private final String info;

    UserStatus(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode()
    {
        return code;
    }

    public String getInfo()
    {
        return info;
    }
}
