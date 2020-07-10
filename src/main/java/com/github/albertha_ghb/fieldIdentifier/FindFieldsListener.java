package com.github.albertha_ghb.fieldIdentifier;

import com.github.albertha_ghb.fieldIdentifier.Java8Parser.UnannClassOrInterfaceTypeContext;
import com.github.albertha_ghb.fieldIdentifier.Java8Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext;
import com.github.albertha_ghb.fieldIdentifier.Java8Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Finds all fields/attributes in a Java source file and saves them in a {@link List} of {@link Field} objects
 *
 * @author
 */
public class FindFieldsListener extends Java8ParserBaseListener
{

    private static final Logger LOG = LoggerFactory.getLogger(FindFieldsListener.class);

    private final Map<String, String> mapSimpleclassnameToFqClassname;
    private final List<Field> fields;
    private boolean isStarImportPresent;


    public FindFieldsListener()
    {
        this.fields = new ArrayList<>();
        mapSimpleclassnameToFqClassname = new HashMap<>();
    }


    @Override
    public void enterTypeImportOnDemandDeclaration(Java8Parser.TypeImportOnDemandDeclarationContext ctx)
    {
        if(!isStarImportPresent)
        {
            LOG.warn("at least one 'star import' is present. finding out exact types might go wrong.");
        }
        isStarImportPresent = true;
    }



    @Override
    public void enterSingleTypeImportDeclaration(Java8Parser.SingleTypeImportDeclarationContext ctx)
    {
        String simpleClassname;
        String fullqualifiedClassname = ctx.typeName().getText();
        if(fullqualifiedClassname.contains("."))
        {
            simpleClassname = fullqualifiedClassname.substring(fullqualifiedClassname.lastIndexOf(".") + 1);
        }
        else
        {
            simpleClassname = fullqualifiedClassname;
        }
        LOG.debug("found import of {} ({})", simpleClassname, fullqualifiedClassname);
        mapSimpleclassnameToFqClassname.put(simpleClassname, fullqualifiedClassname);
    }


    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx)
    {
        String type = ctx.unannType().getText();
        Class<?> exactType = null;
        if(ctx.unannType().unannPrimitiveType() != null)
        {
            exactType = findPrimitiveType(
                    ctx.unannType().unannPrimitiveType().getText());
        }
        if(ctx.unannType().unannReferenceType() != null)
        {
            if(ctx.unannType().unannReferenceType().unannClassOrInterfaceType() != null)
            {
                String className = tryGetTypeAsString(ctx.unannType().unannReferenceType().unannClassOrInterfaceType());
                exactType = findCommonClasses(className);
            }
        }

        for(Java8Parser.VariableDeclaratorContext current
                : ctx.variableDeclaratorList().variableDeclarator())
        {
            if(isRegularField(ctx))
            {
                fields.add(
                        new Field(type, exactType,
                                current.variableDeclaratorId().getText()));
            }
        }
    }


    private String tryGetTypeAsString(UnannClassOrInterfaceTypeContext ctx)
    {
        String type = "";
        if(ctx.unannClassType_lfno_unannClassOrInterfaceType() != null)
        {
            type += ctx.unannClassType_lfno_unannClassOrInterfaceType().Identifier().getText();
        }
        else
        {
            type += ctx.unannInterfaceType_lfno_unannClassOrInterfaceType().unannClassType_lfno_unannClassOrInterfaceType().Identifier().getText();
        }
        if(ctx.unannClassType_lf_unannClassOrInterfaceType() != null)
        {
            for(UnannClassType_lf_unannClassOrInterfaceTypeContext current : ctx.unannClassType_lf_unannClassOrInterfaceType())
            {
                type += "." + current.Identifier().getText();
            }
        }
        else if(ctx.unannInterfaceType_lf_unannClassOrInterfaceType() != null)
        {
            for(UnannInterfaceType_lf_unannClassOrInterfaceTypeContext current : ctx.unannInterfaceType_lf_unannClassOrInterfaceType())
            {
                type += "." + current.unannClassType_lf_unannClassOrInterfaceType().Identifier().getText();
            }
        }
        return type;
    }


    private boolean isRegularField(Java8Parser.FieldDeclarationContext ctx)
    {
        List<Java8Parser.FieldModifierContext> modifiers = ctx.fieldModifier();
        if(modifiers != null)
        {
            for(Java8Parser.FieldModifierContext current : modifiers)
            {
                if("static".equalsIgnoreCase(current.getText()))
                {
                    return false;
                }
            }
        }
        return true;
    }


    private Class<?> findCommonClasses(String name)
    {
        if(name == null)
        {
            return null;
        }
        Optional<String> foundNameInImports = mapSimpleclassnameToFqClassname.entrySet().stream()
                .filter(entry -> entry.getValue().endsWith(name))
                .map(entry -> entry.getValue())
                .findFirst();
        if(foundNameInImports.isPresent())
        {
            try
            {
                return Class.forName(foundNameInImports.get());
            }
            catch(ClassNotFoundException e)
            {
                LOG.warn("given class '{}' found in import '{}' but can not create Class for.",
                        name, foundNameInImports.get());
                return null;
            }
        }

        try
        {
            if(name.contains("."))
            {
                return Class.forName(name);
            }
            else
            {
                return Class.forName("java.lang." + name);
            }
        }
        catch(Exception e)
        {
            return null;
        }

    }
    
    private Class<?> findPrimitiveType(String typeName)
    {
        switch(typeName){
            case "int" :
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "short":
                return short.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "boolean":
                return boolean.class;
            default:
                throw new IllegalStateException(String.format("primitive type expected; given '%s'", typeName));
        }
    }


    public List<Field> getFields()
    {
        return fields;
    }


    public static class Field
    {

        private final String type;
        private final Class<?> exactType;
        private final String name;


        public Field(String type, Class<?> exactType, String name)
        {
            this.type = Objects.requireNonNull(type);
            this.exactType = exactType;
            this.name = Objects.requireNonNull(name);
        }


        @Override
        public String toString()
        {
            return "Field{"
                    + "type=" + type
                    + ", exactType=" + (exactType != null ? exactType.getName() : null)
                    + ", name=" + name
                    + '}';
        }


        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 17 * hash + Objects.hashCode(this.type);
            hash = 17 * hash + Objects.hashCode(this.exactType);
            hash = 17 * hash + Objects.hashCode(this.name);
            return hash;
        }


        @Override
        public boolean equals(Object obj)
        {
            if(this == obj)
            {
                return true;
            }
            if(obj == null)
            {
                return false;
            }
            if(getClass() != obj.getClass())
            {
                return false;
            }
            final Field other = (Field) obj;
            if(!Objects.equals(this.type, other.type))
            {
                return false;
            }
            if(!Objects.equals(this.name, other.name))
            {
                return false;
            }
            if(!Objects.equals(this.exactType, other.exactType))
            {
                return false;
            }
            return true;
        }


        public String getType()
        {
            return type;
        }


        /**
         *
         * @return class of the field, may be {@code null}
         */
        public Class<?> getExactType()
        {
            return exactType;
        }


        public String getName()
        {
            return name;
        }

    }
}
