package ${entity.moduleName}.resource;

import javax.annotation.PostConstruct;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import com.sdm.core.resource.RestResource;
import com.sdm.core.hibernate.dao.RestDAO;
import ${entity.moduleName}.dao.${entity.baseName}DAO;
import ${entity.moduleName}.entity.${entity.name};

/**
 *
 * @author ${author}
 */
@Path("${entity.resourcePath}")
public class ${entity.baseName}Resource extends RestResource<${entity.name}, ${entity.primaryType}> {

    private static final Logger LOG = Logger.getLogger(${entity.baseName}Resource.class.getName());
    private ${entity.baseName}DAO mainDAO;

    @Override
    protected Logger getLogger() {
        return ${entity.baseName}Resource.LOG;
    }

    @PostConstruct
    private void init() {
        mainDAO = new ${entity.baseName}DAO(getUserId());
    }

    @Override
    protected RestDAO getDAO() {
        return this.mainDAO;
    }

}
