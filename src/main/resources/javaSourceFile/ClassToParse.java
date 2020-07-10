package some.test;

import java.awt.Point;
import java.util.List;
import java.util.logging.Logger;


/**
 *
 * @author
 */
public class ClassToParse
{

    private static final Logger LOG = Logger.getLogger(ClassToParse.class.getName());

    private String name, firstname;
    private int age;
    private List<String> technicalErrors;
    private String technicalType = "B";
    private java.lang.Integer technicalCounter = Integer.valueOf("0");
    private Point pt;


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getFirstname()
    {
        return firstname;
    }


    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }


    public int getAge()
    {
        return age;
    }


    public void setAge(int age)
    {
        this.age = age;
    }


    public List<String> getTechnicalErrors()
    {
        return technicalErrors;
    }


    public void setTechnicalErrors(List<String> technicalErrors)
    {
        this.technicalErrors = technicalErrors;
    }


    public String getTechnicalType()
    {
        return technicalType;
    }


    public void setTechnicalType(String technicalType)
    {
        this.technicalType = technicalType;
    }


    public Integer getTechnicalCounter()
    {
        return technicalCounter;
    }


    public void setTechnicalCounter(Integer technicalCounter)
    {
        this.technicalCounter = technicalCounter;
    }


    public Point getPt()
    {
        return pt;
    }


    public void setPt(Point pt)
    {
        this.pt = pt;
    }

}
