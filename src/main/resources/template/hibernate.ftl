<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC "-//Hibernate/Hibernate Configuration DTD 3.0//EN" "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>
        <!-- Hibernate Connection Properties -->
        <property name="hibernate.dialect">com.sdm.core.hibernate.SundewMySQLDialect</property>
        <property name="hibernate.connection.driver_class">com.mysql.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://${db.host}/${db.schema}?zeroDateTimeBehavior=convertToNull&amp;useUnicode=true&amp;characterEncoding=UTF-8</property>
        <property name="hibernate.connection.username">${db.user}</property>
        <property name="hibernate.connection.password">${db.password}</property>
        <property name="hibernate.default_schema">${db.schema}</property>
        <property name="hibernate.hbm2ddl.auto">update</property>

        <!-- Hibernate Enver properties -->
        <property name="org.hibernate.envers.audit_table_suffix">_history</property>
        <property name="org.hibernate.envers.revision_field_name">version</property>
        <property name="org.hibernate.envers.revision_type_field_name">action</property>

        <!-- Hibernate C3P0 properties -->
        <property name="hibernate.c3p0.min_size">3</property>
        <property name="hibernate.c3p0.max_size">20</property>
        <property name="hibernate.c3p0.max_statements">100</property>
        <property name="hibernate.c3p0.timeout">180</property>
        <property name="hibernate.c3p0.acquire_increment">1</property>
        <property name="hibernate.c3p0.acquireRetryAttempts">1</property>
        <property name="hibernate.c3p0.acquireRetryDelay">250</property>
        <property name="hibernate.c3p0.idle_test_period">1800</property>

        <!-- Audit entity mapping -->
        <mapping class="com.sdm.core.hibernate.entity.AuditEntity" />

        <#list modules as module>
        <!-- ${module.name?cap_first} Entities Mapping -->
        <#list module.entityClasses as entity>
        <mapping class="${entity}" />
        </#list>
        
        </#list>
    </session-factory>
</hibernate-configuration>
