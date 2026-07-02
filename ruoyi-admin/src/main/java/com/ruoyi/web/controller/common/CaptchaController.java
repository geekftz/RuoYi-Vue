package com.ruoyi.web.controller.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.code.kaptcha.Producer;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.sign.Base64;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 验证码控制器
 * <p>
 * 提供图形验证码的生成接口。支持两种验证码类型：
 * - char：随机字符验证码
 * - math：数学运算验证码（如 3+5=?）
 * </p>
 * <p>
 * 生成后的验证码存入 Redis（key为captcha_codes:{uuid}，有效期2分钟），
 * 前端在登录时将 uuid 和用户输入的验证码一起提交，后端从 Redis 中取出验证码比对。
 * </p>
 *
 * @author ruoyi
 */
@RestController
public class CaptchaController
{
    /** 验证码生成器（字符类型） */
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    /** 验证码生成器（数学计算类型） */
    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    /** Redis 缓存 */
    @Autowired
    private RedisCache redisCache;

    /** 系统参数配置业务层 */
    @Autowired
    private ISysConfigService configService;
    /**
     * 生成验证码图片
     * <p>
     * 业务流程：
     * 1. 检查系统是否启用了验证码功能（未启用则直接返回）
     * 2. 生成唯一UUID作为验证码标识
     * 3. 根据配置的验证码类型（math/char）生成验证码文本和图片
     * 4. 将验证码答案存入Redis（有效期2分钟）
     * 5. 将验证码图片转为Base64编码返回给前端
     * </p>
     *
     * @param response HTTP响应对象
     * @return 包含uuid、Base64图片、captchaEnabled的结果
     * @throws IOException 图片写入异常
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) throws IOException
    {
        AjaxResult ajax = AjaxResult.success();
        // 检查是否启用验证码功能
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled)
        {
            // 未启用验证码，直接返回
            return ajax;
        }

        // 生成唯一UUID作为验证码标识，用于后续从Redis中取出验证码
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;

        // 根据配置选择验证码类型
        String captchaType = RuoYiConfig.getCaptchaType();
        if ("math".equals(captchaType))
        {
            // 数学运算验证码：文本格式为"3+5@8"，@前是题目，@后是答案
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        }
        else if ("char".equals(captchaType))
        {
            // 随机字符验证码：文本即为答案
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        // 将验证码答案存入Redis，有效期2分钟
        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 将验证码图片转为Base64编码返回给前端
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "jpg", os);
        }
        catch (IOException e)
        {
            return AjaxResult.error(e.getMessage());
        }

        ajax.put("uuid", uuid);
        ajax.put("img", Base64.encode(os.toByteArray()));
        return ajax;
    }
}
