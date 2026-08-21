package com.ruoyi.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * 路由配置信息
 * <p>
 * 【架构位置】ruoyi-system → domain → vo，「动态路由」的返回对象（VO）。
 * 【前端联动——非常重要】前端登录后调 /getRouters 拿到本对象树，在 permission.js 中 addRoutes 动态注册到 Vue Router，
 * 侧边栏菜单也是用它渲染。字段与 vue-router 的路由配置一一对应：
 * name→路由名、path→路径、component→组件路径（对应 src/views 下的 .vue）、hidden→是否隐藏、
 * redirect/alwaysShow/children 控制嵌套菜单行为、meta→标题/图标等显示信息（见 MetaVo）。
 * 【组装链路】SysMenuServiceImpl.selectMenuTreeByUserId → buildMenus 把 SysMenu 转成本对象树。
 * 【注解专项】@JsonInclude(NON_EMPTY)：空字段不输出到 JSON，减小响应体积、避免前端拿到一堆 null。
 * 
 * @author ruoyi
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo
{
    /**
     * 路由名字
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    private boolean hidden;

    /**
     * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
     */
    private String redirect;

    /**
     * 组件地址：如 system/user/index，前端会映射成 src/views/system/user/index.vue；
     * 特殊值 Layout/InnerLink/ParentView 对应布局组件而非业务页面
     */
    private String component;

    /**
     * 路由参数：如 {"id": 1, "name": "ry"}
     */
    private String query;

    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private Boolean alwaysShow;

    /**
     * 其他元素
     */
    private MetaVo meta;

    /**
     * 子路由
     */
    private List<RouterVo> children;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public boolean getHidden()
    {
        return hidden;
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public String getRedirect()
    {
        return redirect;
    }

    public void setRedirect(String redirect)
    {
        this.redirect = redirect;
    }

    public String getComponent()
    {
        return component;
    }

    public void setComponent(String component)
    {
        this.component = component;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public Boolean getAlwaysShow()
    {
        return alwaysShow;
    }

    public void setAlwaysShow(Boolean alwaysShow)
    {
        this.alwaysShow = alwaysShow;
    }

    public MetaVo getMeta()
    {
        return meta;
    }

    public void setMeta(MetaVo meta)
    {
        this.meta = meta;
    }

    public List<RouterVo> getChildren()
    {
        return children;
    }

    public void setChildren(List<RouterVo> children)
    {
        this.children = children;
    }
}
