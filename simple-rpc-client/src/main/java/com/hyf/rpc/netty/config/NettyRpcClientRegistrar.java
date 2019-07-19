package com.hyf.rpc.netty.config;
import com.hyf.rpc.netty.anno.EnableNettyRPC;
import com.hyf.rpc.netty.anno.NettyRPC;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * 自定义注册带@NettyRPC注解的接口，利用动态代理使接口有实现
 * 然后在有@Configuration注解的配置类上使用@Import导入,不然不能注入这些实现@NettyRPC接口的BeanDefinition
 * @author Howinfun
 * @date 2019-07-18
 */
public class NettyRpcClientRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        ClassPathScanningCandidateComponentProvider scan = getScanner();

        //指定注解，类似于Feign注解
        scan.addIncludeFilter(new AnnotationTypeFilter(NettyRPC.class));

        Set<BeanDefinition> candidateComponents = new HashSet<>();
        for (String basePackage : getBasePackages(importingClassMetadata)) {
            candidateComponents.addAll(scan.findCandidateComponents(basePackage));
        }
        candidateComponents.stream().forEach(beanDefinition -> {
            if (!registry.containsBeanDefinition(beanDefinition.getBeanClassName())) {
                if (beanDefinition instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                    AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(), "@NettyRpcClient can only be specified on an interface");
                    Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(NettyRPC.class.getCanonicalName());

                    this.registerNettyRpcClient(registry, annotationMetadata,attributes);
                }
            }
        });
    }

    private void registerNettyRpcClient(BeanDefinitionRegistry registry,
                                        AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        // 指定工厂，使用@NettyRPC注解的接口，当代码中注入时，是从指定工厂获取，而这里的工厂返回的是代理
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
                .genericBeanDefinition(NettyClientFactoryBean.class);
        // @Autowrie：根据类型注入
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        // 注定type属性
        definition.addPropertyValue("type", className);
        String name = attributes.get("name") == null ? "" :(String)(attributes.get("name"));
        // 别名
        String alias = name + "NettyRpcClient";
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setPrimary(true);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
                new String[] { alias });
        // 注册BeanDefinition
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }



    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                // 判断候选人的条件:必须是独立的，然后是接口
                if (beanDefinition.getMetadata().isIndependent() && beanDefinition.getMetadata().isInterface()){
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * 获取指定扫描@NettyRPC注解的包路径
     * @param importingClassMetadata
     * @return
     */
    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableNettyRPC.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        // 如果指定的包路径为空，则
        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }
}

