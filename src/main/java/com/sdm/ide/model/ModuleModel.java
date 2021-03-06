package com.sdm.ide.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ModuleModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4914283651375117166L;
    private String name;
    private Set<String> entityClasses;

    public ModuleModel() {
        this.entityClasses = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getEntityClasses() {
        return entityClasses;
    }

    public void setEntityClasses(Set<String> entityClasses) {
        this.entityClasses = entityClasses;
    }

    public void addEntityClass(String entityClass) {
        this.entityClasses.add(entityClass);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ModuleModel other = (ModuleModel) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
