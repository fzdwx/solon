package org.noear.solon.cloud.extend.polaris.service;

import com.tencent.polaris.configuration.api.core.*;
import com.tencent.polaris.configuration.factory.ConfigFileServiceFactory;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudConfigHandler;
import org.noear.solon.cloud.CloudProps;
import org.noear.solon.cloud.exception.CloudConfigException;
import org.noear.solon.cloud.model.Config;
import org.noear.solon.cloud.service.CloudConfigObserverEntity;
import org.noear.solon.cloud.service.CloudConfigService;

import java.util.*;

public class CloudConfigServicePolarisImp implements CloudConfigService {
    private Map<CloudConfigHandler, CloudConfigObserverEntity> observerMap = new HashMap<>();
    private ConfigFileService real;


    public CloudConfigServicePolarisImp(CloudProps cloudProps) {
        String server = cloudProps.getConfigServer();

        ConfigurationImpl configuration = (ConfigurationImpl) ConfigAPIFactory.defaultConfig();

        configuration.getGlobal().getSystem().getConfigCluster()
                .setNamespace(Solon.cfg().appNamespace());
        configuration.getGlobal().getSystem().getConfigCluster()
                .setService(server);

        configuration.getConfigFile().getServerConnector()
                .setAddresses(Arrays.asList(server));

        this.real = ConfigFileServiceFactory.createConfigFileService(configuration);

    }

    /**
     * 拉取配置
     *
     * @param group 分组
     * @param name  配置名
     * @return
     */
    @Override
    public Config pull(String group, String name) {
        if (Utils.isEmpty(group)) {
            group = Solon.cfg().appGroup();
        }

        ConfigFile configFile = real.getConfigFile(Solon.cfg().appNamespace(), group, name);
        return new Config(group, name, configFile.getContent(), 0);
    }

    /**
     * 设置配置
     *
     * @param group 分组
     * @param name  配置名
     * @param value 值
     * @return
     */
    @Override
    public boolean push(String group, String name, String value) {
        throw new CloudConfigException("Polaris does not support config push");
    }

    /**
     * @param group 分组
     * @param name  配置名
     * @return
     */
    @Override
    public boolean remove(String group, String name) {
        throw new CloudConfigException("Polaris does not support config remove");
    }

    /**
     * 监听配置的修改
     *
     * @param group    分组
     * @param name     配置名
     * @param observer 观察者
     */
    @Override
    public void attention(String group, String name, CloudConfigHandler observer) {
        if (observerMap.containsKey(observer)) {
            return;
        }

        if (Utils.isEmpty(group)) {
            group = Solon.cfg().appGroup();
        }

        CloudConfigObserverEntity entity = new CloudConfigObserverEntity(group, name, observer);
        observerMap.put(observer, entity);


        ConfigFile configFile = real.getConfigFile(Solon.cfg().appNamespace(), group, name);

        configFile.addChangeListener(event -> {
            entity.handle(new Config(entity.group, entity.key, event.getNewValue(), System.currentTimeMillis()));
        });
    }
}