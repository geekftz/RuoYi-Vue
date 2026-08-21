package com.ruoyi.common.core.domain.entity;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.annotation.Excel.Type;
import com.ruoyi.common.annotation.Excels;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.xss.Xss;

/**
 * 用户对象 sys_user
 * <p>
 * 【架构位置】common模块 → core/domain/entity，实体类（Entity），与数据库sys_user表一一对应。
 * 【为何在common而非system】虽然用户业务在ruoyi-system模块，但LoginUser（安全模块要用）、
 * TokenService（framework模块要用）等底层组件都依赖SysUser，放common避免循环依赖。
 * 【继承BaseEntity】自动拥有createBy/createTime/updateBy/updateTime/remark五个审计字段 +
 * params(Map)扩展参数 + searchValue检索值，详见BaseEntity注释。
 * 【三类注解速览】
 *   @Excel系列：若依Excel工具注解，标注哪些字段参与导入导出、列名、字典转换（ExcelUtil解析）；
 *   校验注解（@NotBlank/@Size/@Email/@Xss）：配合Controller的@Valid做入参校验，失败自动抛异常；
 *   @JsonProperty(WRITE_ONLY)：Jackson序列化控制，防止密码密文随JSON返回给前端。
 * 
 * @author ruoyi
 */
public class SysUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户ID（主键，自增） */
    // 【注解专项】@Excel：若依自定义Excel注解。name=导出列名；type=Type.EXPORT表示仅导出时包含（导入模板不要这一列）；
    // cellType=NUMERIC数字格式；prompt=列批注提示。ExcelUtil.exportExcel()会反射读取这些注解生成表格。
    @Excel(name = "用户序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "用户编号")
    private Long userId;

    /** 部门ID */
    @Excel(name = "部门编号", type = Type.IMPORT)
    private Long deptId;

    /** 用户账号 */
    @Excel(name = "登录名称")
    private String userName;

    /** 用户昵称 */
    @Excel(name = "用户名称")
    private String nickName;

    /** 用户邮箱 */
    @Excel(name = "用户邮箱")
    private String email;

    /** 手机号码 */
    @Excel(name = "手机号码", cellType = ColumnType.TEXT)
    private String phonenumber;

    /** 用户性别（0=男 1=女 2=未知，对应字典sys_user_sex） */
    // 【readConverterExp】导出Excel时把数据库存的"0/1/2"自动转成"男/女/未知"显示，导入时反向转换。
    // 前端表格里看到的性别标签则是通过字典组件（dict-tag）+ sys_user_sex字典翻译的，与Excel互不影响。
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /** 用户头像 */
    private String avatar;

    /** 密码（BCrypt加密后的密文，永不返回给前端，见getPassword上的@JsonProperty注解） */
    private String password;

    /** 账号状态（0正常 1停用） */
    @Excel(name = "账号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除）——若依统一逻辑删除：删除用户其实是UPDATE del_flag='2'，所有查询SQL都带del_flag='0'条件 */
    private String delFlag;

    /** 最后登录IP */
    @Excel(name = "最后登录IP", type = Type.EXPORT)
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    /** 密码最后更新时间 */
    private Date pwdUpdateDate;

    /** 部门对象（关联对象，非sys_user表字段，查询时联表sys_dept填充） */
    // 【注解专项】@Excels：当一个关联对象要拆成多列导出时使用。targetAttr指定取关联对象的哪个属性：
    // 这里导出的Excel会多出"部门名称""部门负责人"两列，值分别来自dept.deptName、dept.leader。
    @Excels({
        @Excel(name = "部门名称", targetAttr = "deptName", type = Type.EXPORT),
        @Excel(name = "部门负责人", targetAttr = "leader", type = Type.EXPORT)
    })
    private SysDept dept;

    /** 角色对象 */
    private List<SysRole> roles;

    /** 角色组（前端"分配角色"多选框选中的角色ID数组，非表字段；新增/修改用户时由Service层据此写入sys_user_role关联表） */
    private Long[] roleIds;

    /** 岗位组（同上，对应sys_user_post关联表） */
    private Long[] postIds;

    /** 角色ID（数据权限用：查询用户列表时按"本角色数据范围"过滤，见@DataScope切面；非表字段） */
    private Long roleId;

    public SysUser()
    {

    }

    public SysUser(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    /**
     * 是否为超级管理员
     * <p>
     * 【判断规则】userId == 1L 即超管（若依硬编码约定，建表初始数据admin的ID就是1）。
     * 【权限特权】超管绕过一切权限校验：@PreAuthorize放行、数据权限不加过滤SQL、菜单全量返回。
     */
    public boolean isAdmin()
    {
        return SecurityUtils.isAdmin(this.userId);
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    // 【注解专项】@Xss：若依自定义校验注解，禁止值中包含HTML脚本字符（防XSS攻击）；
    // @Size：JSR-303标准校验注解，限制字符串长度。这些校验在Controller方法加@Valid/@Validated时自动触发，
    // 校验失败抛MethodArgumentNotValidException，由全局异常处理器转成"用户昵称长度不能超过30个字符"提示返回前端。
    // 注意：校验注解写在getter上是若依的习惯写法（字段上有@Excel，注解分工），JSR-303规范允许标在字段或getter上。
    @Xss(message = "用户昵称不能包含脚本字符")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    @Xss(message = "用户账号不能包含脚本字符")
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    // 【注解专项】@JsonProperty(WRITE_ONLY)：Jackson序列化控制——"只写不读"：
    // 前端提交JSON里的password可以正常反序列化进来（如修改密码接口），
    // 但后端返回JSON时绝不包含password字段，防止BCrypt密文泄露给前端。
    // 这是比@JSONField(serialize=false)更精细的方案（后者会连反序列化也禁掉，导致无法接收密码）。
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getLoginIp()
    {
        return loginIp;
    }

    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }

    public Date getLoginDate()
    {
        return loginDate;
    }

    public void setLoginDate(Date loginDate)
    {
        this.loginDate = loginDate;
    }

    public Date getPwdUpdateDate()
    {
        return pwdUpdateDate;
    }

    public void setPwdUpdateDate(Date pwdUpdateDate)
    {
        this.pwdUpdateDate = pwdUpdateDate;
    }

    public SysDept getDept()
    {
        return dept;
    }

    public void setDept(SysDept dept)
    {
        this.dept = dept;
    }

    public List<SysRole> getRoles()
    {
        return roles;
    }

    public void setRoles(List<SysRole> roles)
    {
        this.roles = roles;
    }

    public Long[] getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds)
    {
        this.roleIds = roleIds;
    }

    public Long[] getPostIds()
    {
        return postIds;
    }

    public void setPostIds(Long[] postIds)
    {
        this.postIds = postIds;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("deptId", getDeptId())
            .append("userName", getUserName())
            .append("nickName", getNickName())
            .append("email", getEmail())
            .append("phonenumber", getPhonenumber())
            .append("sex", getSex())
            .append("avatar", getAvatar())
            .append("password", getPassword())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("loginIp", getLoginIp())
            .append("loginDate", getLoginDate())
            .append("pwdUpdateDate", getPwdUpdateDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("dept", getDept())
            .toString();
    }
}
