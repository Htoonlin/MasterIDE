package ${entity.moduleName}.dao;

import org.hibernate.Session;

import com.sdm.core.hibernate.dao.RestDAO;
import ${entity.moduleName}.entity.${entity.name};

/**
 *
 * @author ${author}
 */
public class ${entity.baseName}DAO extends RestDAO {

    public ${entity.baseName}DAO(int userId) {
        super(${entity.name}.class.getName(), userId);
    }

    public ${entity.baseName}DAO(Session session, int userId) {
        super(session, ${entity.name}.class.getName(), userId);
    }
}
