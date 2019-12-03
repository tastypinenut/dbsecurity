package xyz.tpn.dbsecurity.model;

import java.io.Serializable;

public class ParamInfo implements Serializable {
    private Long id;
    private String paramName;
    private String paramValue;
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParamInfo{");
        sb.append("id=").append(id);
        sb.append(", paramName=").append(paramName);
        sb.append(", paramValue=").append(paramValue);
        sb.append(", type='").append(type);
        sb.append('}');
        return sb.toString();
    }
}