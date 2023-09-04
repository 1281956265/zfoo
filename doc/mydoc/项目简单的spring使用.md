# 项目简单的spring使用

项目中使用了spring自定义标签：

通过xml实现，通过xsd定义属性，元素，数据类型，
Spring 在解析 xml 文件中的标签的时候会区分当前的标签是四种基本标签（
import、alias、bean和beans）还是自定义标签，如果是自定义标签，
则会按照自定义标签的逻辑解析当前的标签。

扩展 Spring 自定义标签配置一般需要以下几个步骤：

**先以event模块为例子, event模块的自定义标签有注解**
1. 编写.schemas文件，通知spring容器我们定义的xsd文件在哪里

   **spring.schemas**

2. 定义一个 XSD 文件，用于描述组件内容 属性

   **event-1.0.xsd**

3. 创建一个实现 AbstractSingleBeanDefinitionParser 接口的类，又或者创建一个实现 BeanDefinitionParser 接口的类，用来解析 XSD 文件中的定义和组件定义。这两种实现方式对应不同的 XSD 文件配置方式。
    
    **EventDefinitionParser**    

4. 创建一个 Handler，继承 NamespaceHandlerSupport ，用于将组件注册到 Spring 容器

    **NamespaceHandler**    

5. 编写.handlers 文件，扩展NamespaceHandler命名空间注册器和定义及解析器

   **spring.handlers**