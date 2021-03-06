package com.sxj.mybatis.shard.listener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.mybatis.orm.builder.GenericStatementBuilder;
import com.sxj.mybatis.shard.MybatisConfiguration;
import com.sxj.mybatis.shard.configuration.XmlReader;
import com.sxj.mybatis.shard.configuration.node.ShardRuleCfg;
import com.sxj.mybatis.shard.datasource.DataSourceFactory;
import com.sxj.mybatis.shard.mapper.MapperScanConfigurator;
import com.sxj.spring.modules.util.AnnotationUtils;

public class MybatisMapperProcessor implements
        ApplicationListener<ContextRefreshedEvent>
{
    private static boolean initialized = false;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        if (!initialized)
            try
            {
                MybatisConfiguration.initialize();
                registerXMLMappers();
                buildOrmMapper(event);
                DataSourceFactory.initDataSources();
                initialized = true;
            }
            catch (IOException | ClassNotFoundException ioe)
            {
                throw new RuntimeException(ioe);
            }
    }
    
    private void buildOrmMapper(ContextRefreshedEvent event)
            throws IOException, ClassNotFoundException
    {
        for (String clazzName : findEntityClassNames(event.getApplicationContext()))
        {
            GenericStatementBuilder builder = new GenericStatementBuilder(
                    MybatisConfiguration.getConfiguration(),
                    Class.forName(clazzName));
            builder.build();
        }
    }
    
    private Set<String> findEntityClassNames(
            ApplicationContext applicationContext) throws IOException,
            ClassNotFoundException
    {
        Set<String> classNames = new HashSet<String>();
        SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(
                applicationContext);
        String fieldValue = MapperScanConfigurator.getTypeAliasesPackage();
        fieldValue = fieldValue == null ? "" : fieldValue;
        String[] split = fieldValue.split(",");
        for (String value : split)
        {
            Resource[] resources = applicationContext.getResources("classpath*:"
                    + StringUtils.replaceChars(value, '.', '/') + "/**/*.class");
            for (Resource resource : resources)
            {
                if (resource.isReadable())
                {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    ClassMetadata classMetadata = metadataReader.getClassMetadata();
                    AnnotationMetadata classAnnotationMetadata = metadataReader.getAnnotationMetadata();
                    String entityAnnotation = Entity.class.getName();
                    if (classAnnotationMetadata.isAnnotated(entityAnnotation))
                    {
                        Field idFiled = AnnotationUtils.findDeclaredFieldWithAnnoation(Id.class,
                                Class.forName(classMetadata.getClassName()));
                        Table table = Class.forName(classMetadata.getClassName())
                                .getAnnotation(Table.class);
                        if (idFiled != null && table != null)
                        {
                            Id annotation = idFiled.getAnnotation(Id.class);
                            ShardRuleCfg rule = new ShardRuleCfg();
                            rule.setTableName(table.name());
                            rule.setColumn(annotation.column());
                            XmlReader.getRules().put(rule.getTableName(), rule);
                        }
                        classNames.add(classMetadata.getClassName());
                    }
                }
            }
        }
        
        return classNames;
    }
    
    private void registerXMLMappers()
    {
        if (MapperScanConfigurator.getMapperLocations() != null
                && MapperScanConfigurator.getMapperLocations().length > 0)
        {
            for (Resource mapperLocation : MapperScanConfigurator.getMapperLocations())
            {
                if (mapperLocation == null)
                {
                    continue;
                }
                
                try
                {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
                            mapperLocation.getInputStream(),
                            MybatisConfiguration.getConfiguration(),
                            mapperLocation.toString(),
                            MybatisConfiguration.getConfiguration()
                                    .getSqlFragments());
                    xmlMapperBuilder.parse();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(
                            "Failed to parse mapping resource: '"
                                    + mapperLocation + "'", e);
                }
                finally
                {
                    ErrorContext.instance().reset();
                }
                
            }
        }
        else
        {
            try
            {
                for (String clsName : MapperScanConfigurator.getMapperInterfaces())
                {
                    MybatisConfiguration.getConfiguration()
                            .addMapper(Class.forName(clsName));
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
}
