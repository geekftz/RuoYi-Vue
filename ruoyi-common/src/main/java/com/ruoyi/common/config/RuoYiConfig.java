package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 * <p>
 * 【架构位置】common模块 → config，项目级配置的承载类，对应application.yml中"ruoyi:"前缀的配置块。
 * 【注解专项】@ConfigurationProperties(prefix="ruoyi")：Spring Boot自动把yml中ruoyi.name、ruoyi.version等
 *             值绑定到本类同名字段；@Component注册为Bean，其他类用@Autowired注入或直接调静态方法。
 * 【为何字段是static】profile/addressEnabled/captchaType用static字段+实例setter的写法：
 *             Spring给实例赋值时通过setter写入static变量，这样工具类（如FileUploadUtils）无需注入即可静态调用
 *             RuoYiConfig.getUploadPath()，是若依的实用主义风格。
 * 【对系统影响】profile决定上传文件的磁盘落盘根目录（如D:/ruoyi/uploadPath），
 *             配合Constants.RESOURCE_PREFIX="/profile"的URL映射对外提供文件访问。
 * 
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "ruoyi")
public class RuoYiConfig
{
    /** 项目名称 */
    private String name;

    /** 版本 */
    private String version;

    /** 版权年份 */
    private String copyrightYear;

    /** 上传路径（对应yml的ruoyi.profile，文件上传的磁盘根目录，所有上传文件都在其子目录下） */
    private static String profile;

    /** 获取地址开关（对应yml的ruoyi.addressEnabled：登录日志/操作日志是否解析IP归属地，开启后调ip2region库查"省 市"） */
    private static boolean addressEnabled;

    /** 验证码类型（对应yml的ruoyi.captchaType：math算式验证码 / char字符验证码，CaptchaConfig据此选择生成器） */
    private static String captchaType;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getCopyrightYear()
    {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear)
    {
        this.copyrightYear = copyrightYear;
    }

    public static String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        RuoYiConfig.profile = profile;
    }

    public static boolean isAddressEnabled()
    {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled)
    {
        RuoYiConfig.addressEnabled = addressEnabled;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        RuoYiConfig.captchaType = captchaType;
    }

    /**
     * 获取导入上传路径（Excel导入文件的临时存放目录：profile/import）
     */
    public static String getImportPath()
    {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径（用户头像存放目录：profile/avatar）
     */
    public static String getAvatarPath()
    {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径（通用下载/导出文件目录：profile/download）
     */
    public static String getDownloadPath()
    {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径（通用上传文件目录：profile/upload；前端调/common/upload接口传的文件就落在这里）
     */
    public static String getUploadPath()
    {
        return getProfile() + "/upload";
    }
}
