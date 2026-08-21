package com.ruoyi.framework.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.sql.DataSource;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * Mybatis支持*匹配扫描包
 * <p>
 * 【架构位置】ruoyi-framework / config层 —— MyBatis持久层核心配置。
 * 【为什么需要它】Spring Boot默认的MyBatis自动装配不支持 typeAliasesPackage 写通配符（如 com.ruoyi.**.domain），
 * 若依是多模块工程（system/quartz/generator各自有domain包），因此手动创建 SqlSessionFactory 来支持通配符扫描。
 * <p>
 * 【配置来源】application.yml 中 mybatis.typeAliasesPackage / mapperLocations / configLocation 三个配置项
 *
 * @author ruoyi
 */
@Configuration
public class MyBatisConfig
{
    // Spring环境对象，用于读取 application.yml 中的 mybatis.* 配置项
    @Autowired
    private Environment env;

    /** 类文件匹配模式：扫描包下所有 .class 文件 */
    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    /**
     * 解析 typeAliasesPackage 通配符配置，转换为真实包路径列表（逗号分隔）
     * <p>
     * 效果：XML中的 resultType/parameterType 可以直接写类名（如 SysUser）而不用写全限定名
     *
     * @param typeAliasesPackage application.yml配置的实体类包路径（可含**通配符、逗号分隔多个）
     * @return 展开后的真实包名列表，如 "com.ruoyi.system.domain,com.ruoyi.quartz.domain"
     */
    public static String setTypeAliasesPackage(String typeAliasesPackage)
    {
        ResourcePatternResolver resolver = (ResourcePatternResolver) new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        List<String> allResult = new ArrayList<String>();
        try
        {
            for (String aliasesPackage : typeAliasesPackage.split(","))
            {
                List<String> result = new ArrayList<String>();
                aliasesPackage = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                        + ClassUtils.convertClassNameToResourcePath(aliasesPackage.trim()) + "/" + DEFAULT_RESOURCE_PATTERN;
                Resource[] resources = resolver.getResources(aliasesPackage);
                if (resources != null && resources.length > 0)
                {
                    MetadataReader metadataReader = null;
                    for (Resource resource : resources)
                    {
                        if (resource.isReadable())
                        {
                            metadataReader = metadataReaderFactory.getMetadataReader(resource);
                            try
                            {
                                result.add(Class.forName(metadataReader.getClassMetadata().getClassName()).getPackage().getName());
                            }
                            catch (ClassNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (result.size() > 0)
                {
                    HashSet<String> hashResult = new HashSet<String>(result);
                    allResult.addAll(hashResult);
                }
            }
            if (allResult.size() > 0)
            {
                typeAliasesPackage = String.join(",", (String[]) allResult.toArray(new String[0]));
            }
            else
            {
                throw new RuntimeException("mybatis typeAliasesPackage 路径扫描错误,参数typeAliasesPackage:" + typeAliasesPackage + "未找到任何包");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return typeAliasesPackage;
    }

    /**
     * 解析 mapperLocations 配置，加载所有Mapper XML文件资源
     *
     * @param mapperLocations XML路径数组（如 classpath*:mapper/**\/*.xml）
     * @return 所有匹配到的Mapper XML资源，交给SqlSessionFactory解析其中的SQL语句
     */
    public Resource[] resolveMapperLocations(String[] mapperLocations)
    {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<Resource>();
        if (mapperLocations != null)
        {
            for (String mapperLocation : mapperLocations)
            {
                try
                {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * 手动构建 SqlSessionFactory（MyBatis核心工厂，负责创建执行SQL的SqlSession）
     * <p>
     * 【调用链】Mapper接口方法被调用 → MyBatis代理 → SqlSession → 从本工厂的配置中找到XML里的SQL执行
     *
     * @param dataSource 数据源（DruidConfig中配置的Druid动态数据源自动注入）
     * @return 初始化完成的SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception
    {
        // 从application.yml读取mybatis三项配置：实体别名包、XML位置、全局配置文件位置
        String typeAliasesPackage = env.getProperty("mybatis.typeAliasesPackage");
        String mapperLocations = env.getProperty("mybatis.mapperLocations");
        String configLocation = env.getProperty("mybatis.configLocation");
        // 展开通配符为真实包路径
        typeAliasesPackage = setTypeAliasesPackage(typeAliasesPackage);
        // 注册SpringBoot虚拟文件系统，保证打成jar包后MyBatis仍能扫描到类路径下的资源
        VFS.addImplClass(SpringBootVFS.class);

        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliasesPackage(typeAliasesPackage);
        // 加载所有Mapper XML（classpath*:mapper/**/*.xml，覆盖system/quartz/generator各模块）
        sessionFactory.setMapperLocations(resolveMapperLocations(StringUtils.split(mapperLocations, ",")));
        // 加载 mybatis-config.xml 全局配置（如日志实现、下划线转驼峰等）
        sessionFactory.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));
        return sessionFactory.getObject();
    }
}