package ${entity.moduleName}.entity;

<#list entity.imports as import>
import ${import};
</#list>

/**
 *
 * @author ${author}
 * 
 */
<#if entity.auditable>
@Audited
</#if>
<#if entity.dynamicUpdate>
@DynamicUpdate(value = true)
</#if>
@Entity(name = "${entity.entityName}")
@Table(name = "${entity.tableName}")
public class ${entity.name} extends DefaultEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = ${serializeId};

    public ${entity.name}() {
    }

    @NotAudited
    @JsonIgnore
    @Formula(value = "concat(${entity.searchFields?join(",")})")
    private String search;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    <#list entity.properties as property>
    <#if property.primary>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    <#assign primaryField>${property.name}</#assign>
    </#if>
    <#if !property.auditable>
    @NotAudited
    </#if>
    <#if property.jsonIgnore>
    @JsonIgnore
    </#if>
    <#list property.validations as validate>
    ${validate.annotation}
    </#list>
    @UIStructure(order = ${property.index}, label = "${property.label}", inputType = UIInputType.${property.inputType}<#if property.hideInGrid>, hideInGrid = true</#if> <#if property.readOnly>, readOnly = true</#if>)
    @Column(name = "${property.columnName}", nullable = ${property.required?string("false", "true")}, columnDefinition = "${property.columnDef}")
    private ${property.type} ${property.name}; 	

    public ${property.type} get${property.name?cap_first}() {
        return this.${property.name};
    }

    public void set${property.name?cap_first}(${property.type} value) {
        this.${property.name} = value;
    }

    </#list>

    <#if primaryField??>
    @JsonGetter("&detail_link")
    public LinkModel getSelfLink() {
        String selfLink = UriBuilder.fromResource(${entity.baseName}Resource.class).build().toString();
        selfLink += "/" + this.${primaryField} + "/";
        return new LinkModel(selfLink);
    }
    </#if>
}	
	