package com.ruoyi.common.utils.ip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;

/**
 * 获取地址类
 * <p>
 * 【能力】根据IP地址查询对应的地理位置（省份+城市），如"广东省 深圳市"。
 * 【使用场景】登录日志中记录用户的登录地点（sys_logininfor表的login_location字段），
 * 前端在"日志管理→登录日志"页面可以看到每个用户的登录地点。
 * 【数据来源】调用太平洋网络(whois.pconline.com.cn)的免费IP查询接口。
 * 【配置开关】application.yml中的 ruoyi.addressEnabled 控制是否启用（内网部署时通常关闭）。
 * 【注意】内网IP直接返回"内网IP"不查询外网接口。
 * 
 * @author ruoyi
 */
public class AddressUtils
{
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // IP地址查询（太平洋网络的免费IP归属地查询接口）
    public static final String IP_URL = "https://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    /**
     * 根据IP查询真实地理地址
     * 
     * @param ip IP地址
     * @return 地理地址字符串（如"广东省 深圳市"），查询失败返回"XX XX"
     */
    public static String getRealAddressByIP(String ip)
    {
        // 内网不查询（内网IP没有公网归属地信息）
        if (IpUtils.internalIp(ip))
        {
            return "内网IP";
        }
        // 检查yml中是否开启了地址查询开关（ruoyi.addressEnabled=true）
        if (RuoYiConfig.isAddressEnabled())
        {
            try
            {
                // 调用外部IP查询接口，返回GBK编码的JSON
                String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.GBK);
                if (StringUtils.isEmpty(rspStr))
                {
                    log.error("获取地理位置异常 {}", ip);
                    return UNKNOWN;
                }
                // 解析JSON获取省份和城市
                JSONObject obj = JSON.parseObject(rspStr);
                String region = obj.getString("pro");
                String city = obj.getString("city");
                return String.format("%s %s", region, city);
            }
            catch (Exception e)
            {
                log.error("获取地理位置异常 {}", ip);
            }
        }
        return UNKNOWN;
    }
}
