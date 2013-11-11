package com.korwe.kordapt;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tjad.clark@korwe.com>Tjad Clark</a>
 * */
public class ServiceFunction {
    private String name;
    private String description;
    private List<ServiceFunctionParameter> parameters;
    private String returnType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ServiceFunctionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ServiceFunctionParameter> parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public Boolean getMultiParam(){
        return this.parameters != null && this.parameters.size() > 1;
    }

    public String getParamNamesString(){
        List<String> names = new ArrayList();
        for(ServiceFunctionParameter p : this.parameters){
            names.add('"'+p.getName()+'"');
        }

        if(getMultiParam()){
            return "{"+Joiner.on(',').join(names)+"}";
        }
        return Joiner.on(',').join(names);
    }

    public String getParametersString(){
        List<String> names = new ArrayList();
        for(ServiceFunctionParameter p : this.parameters){
            names.add(p.getType()+" "+p.getName());
        }

        return Joiner.on(", ").join(names);
    }
}