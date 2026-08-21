package com.ruoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * <p>
 * 【架构位置】ruoyi-admin —— 整个后端服务的启动入口（main方法所在）。
 * <p>
 * 【注解说明】@SpringBootApplication 是三合一注解：
 * @Configuration（配置类）+ @EnableAutoConfiguration（自动装配）+ @ComponentScan（扫描com.ruoyi包下所有组件）。
 * 【为什么排除DataSourceAutoConfiguration】若依使用自定义多数据源（DruidConfig手动创建master/slave动态数据源），
 * 不排除Spring Boot默认的数据源自动配置会冲突报错。
 * <p>
 * 【启动后加载顺序】yml配置 → 各@Configuration(Security/Druid/MyBatis/Redis...) → Mapper扫描 → 内嵌Tomcat监听8080。
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        // 启动Spring Boot：创建Spring容器、加载所有Bean、启动内嵌Tomcat
        SpringApplication.run(RuoYiApplication.class, args);
        // 启动成功后在控制台打印若依标志图案（banner.txt之外的额外提示）
        System.out.println("(♥◠‿◠)ﾉﾞ  若依启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
