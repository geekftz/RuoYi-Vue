package com.ruoyi.system.domain;

/**
 * 当前在线会话
 * <p>
 * 【架构位置】ruoyi-system → domain，注意这不是数据库实体！是「在线用户」监控页的展示对象（VO）。
 * 【数据来源】不查数据库：在线用户 = Redis 中 login_tokens:* 里的 LoginUser 集合，
 * SysUserOnlineServiceImpl 扫描这些 key 后把 LoginUser 的字段拷贝进本对象返回前端。
 * 【配套功能】在线用户页面可「强退」某会话：本质是删除 Redis 中对应的 login_tokens key。
 * 
 * @author ruoyi
 */
public class SysUserOnline
{
    /** 会话编号：即 LoginUser.token（UUID），也是 Redis key login_tokens:xxx 的后半段 */
    private String tokenId;

    /** 部门名称 */
    private String deptName;

    /** 用户名称 */
    private String userName;

    /** 登录IP地址 */
    private String ipaddr;

    /** 登录地址：由 IP 通过 AddressUtils 解析出的地理位置 */
    private String loginLocation;

    /** 浏览器类型：解析 User-Agent 得出 */
    private String browser;

    /** 操作系统：解析 User-Agent 得出 */
    private String os;

    /** 登录时间：毫秒时间戳 */
    private Long loginTime;

    public String getTokenId()
    {
        return tokenId;
    }

    public void setTokenId(String tokenId)
    {
        this.tokenId = tokenId;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation()
    {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation)
    {
        this.loginLocation = loginLocation;
    }

    public String getBrowser()
    {
        return browser;
    }

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }
}
