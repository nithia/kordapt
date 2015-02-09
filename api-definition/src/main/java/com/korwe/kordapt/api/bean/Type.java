package com.korwe.kordapt.api.bean;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import java.util.List;

/**
 * @author <a href="mailto:tjad.clark@korwe.com>Tjad Clark</a>
 */
public class Type {
    private String name;
    private String packageName;
    private Type inheritsFrom;
    private List<Attribute> attributes;
    private List<Type> typeArguments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getInheritsFrom() {
        return inheritsFrom;
    }

    public void setInheritsFrom(Type inheritsFrom) {
        this.inheritsFrom = inheritsFrom;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }


    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFullQualifiedName(){
        return this.packageName == null ? this.name : this.packageName + '.' + this.name;
    }

    public List<Type> getTypeArguments() {
        return typeArguments;
    }

    public void setTypeArguments(List<Type> typeArguments) {
        this.typeArguments = typeArguments;
    }

    public String getDeclarationString(){
        String typeDeclarationString = name;
        typeDeclarationString += getTypeArgumentsString(
            new Function<Type, String>(){
                @Override
                public String apply(Type input) {
                    return input.getDeclarationString();
                }
            }
        );
        return typeDeclarationString;
    }

    public String getDefinitionString(){
        String qualifiedName = packageName == null ? name : packageName + "." +name;
        qualifiedName += getTypeArgumentsString(
            new Function<Type, String>(){
                @Override
                public String apply(Type input) {
                    return input.getDefinitionString();
                }
            }
        );
        return  qualifiedName;
    }

    public String getTypeArgumentsString(Function f){
        String typeArgumentString = "";
        if(!(typeArguments == null || typeArguments.isEmpty())){
            typeArgumentString += "<" +
                    Joiner.on(", ").join(Iterables.transform(typeArguments, f)) +
                    ">";
        }
        return typeArgumentString;
    }

    @Override
    public String toString(){ //Overriden to support Joiner in getDefinitionString()
        return getDeclarationString();
    }
}