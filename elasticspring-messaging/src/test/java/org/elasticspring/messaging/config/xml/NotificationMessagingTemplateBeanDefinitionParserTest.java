/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticspring.messaging.config.xml;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.elasticspring.context.config.xml.GlobalBeanDefinitionUtils;
import org.elasticspring.core.config.AmazonWebserviceClientConfigurationUtils;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

/**
 * @author Alain Sahli
 */
public class NotificationMessagingTemplateBeanDefinitionParserTest {

	@Test
	public void parseInternal_withMinimalConfig_shouldCreateDefaultTemplate() throws Exception {
		//Arrange
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);

		//Act
		reader.loadBeanDefinitions(new ClassPathResource(getClass().getSimpleName() + "-minimal.xml", getClass()));

		//Assert
		BeanDefinition notificationMessagingTemplateBeanDefinition = registry.getBeanDefinition("notificationMessagingTemplate");
		assertEquals(AmazonWebserviceClientConfigurationUtils.getBeanName(AmazonWebserviceClientConfigurationUtils.getBeanName("com.amazonaws.services.sns.AmazonSNSClient")),
				((RuntimeBeanReference) notificationMessagingTemplateBeanDefinition.getConstructorArgumentValues().getArgumentValue(0, RuntimeBeanReference.class).getValue()).getBeanName());
		assertEquals(GlobalBeanDefinitionUtils.RESOURCE_ID_RESOLVER_BEAN_NAME, ((RuntimeBeanReference) notificationMessagingTemplateBeanDefinition
				.getConstructorArgumentValues().getArgumentValue(1, RuntimeBeanReference.class).getValue()).getBeanName());
		String jacksonConverter = "org.springframework.messaging.converter.MappingJackson2MessageConverter";
		assertEquals(jacksonConverter, ((RootBeanDefinition) notificationMessagingTemplateBeanDefinition.getPropertyValues()
				.getPropertyValue("messageConverter").getValue()).getBeanClassName());
	}

	@Test
	public void parseInternal_withCustomAmazonSnsClient_shouldPassItAsConstructorArg() throws Exception {
		//Arrange
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);

		//Act
		reader.loadBeanDefinitions(new ClassPathResource(getClass().getSimpleName() + "-custom-amazon-sns.xml", getClass()));

		//Assert
		BeanDefinition notificationMessagingTemplateBeanDefinition = registry.getBeanDefinition("notificationMessagingTemplate");
		assertEquals("mySnsClient", ((RuntimeBeanReference) notificationMessagingTemplateBeanDefinition.getConstructorArgumentValues()
				.getArgumentValue(0, RuntimeBeanReference.class).getValue()).getBeanName());
	}

	@Test
	public void parseInternal_withCustomConverter_mustBeSetOnTemplate() throws Exception {
		//Arrange
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);

		//Act
		reader.loadBeanDefinitions(new ClassPathResource(getClass().getSimpleName() + "-custom-converter.xml", getClass()));

		//Assert
		BeanDefinition notificationMessagingTemplateBeanDefinition = registry.getBeanDefinition("notificationMessagingTemplate");
		assertEquals("myCustomConverter", ((RuntimeBeanReference) notificationMessagingTemplateBeanDefinition.getPropertyValues()
				.getPropertyValue("messageConverter").getValue()).getBeanName());
	}

	@Test
	public void parseInternal_withDefaultDestination_mustBeSetOnTemplate() throws Exception {
		//Arrange
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);

		//Act
		reader.loadBeanDefinitions(new ClassPathResource(getClass().getSimpleName() + "-with-default-destination.xml", getClass()));

		//Assert
		BeanDefinition notificationMessagingTemplateBeanDefinition = registry.getBeanDefinition("notificationMessagingTemplate");
		assertEquals("myDefaultDestination", ((RuntimeBeanReference) notificationMessagingTemplateBeanDefinition.getPropertyValues()
				.getPropertyValue("defaultDestination").getValue()).getBeanName());
	}

	@Test
	public void parseInternal_withCustomRegion_shouldConfigureDefaultClientWithCustomRegion() throws Exception {
		//Arrange
		DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);

		//Act
		reader.loadBeanDefinitions(new ClassPathResource(getClass().getSimpleName() + "-custom-region.xml", getClass()));

		//Assert
		AmazonSNSClient amazonSns = registry.getBean(AmazonSNSClient.class);
		assertEquals("https://" + Region.getRegion(Regions.EU_WEST_1).getServiceEndpoint("sns"), ReflectionTestUtils.getField(amazonSns, "endpoint").toString());
	}

	@Test
	public void parseInternal_withCustomRegionProvider_shouldConfigureDefaultClientWithCustomRegionReturnedByProvider() throws Exception {
		//Arrange
		DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);

		//Act
		reader.loadBeanDefinitions(new ClassPathResource(getClass().getSimpleName() + "-custom-region-provider.xml", getClass()));

		//Assert
		AmazonSNSClient amazonSns = registry.getBean(AmazonSNSClient.class);
		assertEquals("https://" + Region.getRegion(Regions.CN_NORTH_1).getServiceEndpoint("sns"), ReflectionTestUtils.getField(amazonSns, "endpoint").toString());
	}

}