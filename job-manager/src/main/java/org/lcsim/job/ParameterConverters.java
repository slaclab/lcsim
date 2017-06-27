package org.lcsim.job;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.lcsim.util.xml.JDOMExpressionFactory;

/**
 * Converter utilities for making Java Beans arguments from LCSim XML Driver parameters, using a JDOM factory to do
 * expression evaluation.
 * 
 * @author jeremym
 */
public class ParameterConverters {

    List<IParameterConverter> converters = new ArrayList<IParameterConverter>();
    JDOMExpressionFactory factory;

    public ParameterConverters(JDOMExpressionFactory factory) {
        this.factory = factory;

        add(new IntegerConverter());
        add(new StringConverter());
        add(new DoubleConverter());
        add(new FloatConverter());
        add(new BooleanConverter());
        add(new Hep3VectorConverter());
        add(new DoubleArray1DConverter());
        add(new IntegerArray1DConverter());
        add(new FloatArray1DConverter());
        add(new StringArray1DConverter());
        add(new BooleanArray1DConverter());
        add(new FileConverter());
        add(new URLConverter());
        add(new IntegerArray2DConverter());
        add(new DoubleArray2DConverter());
    }

    public Object convert(Class propertyType, Element parameterElement) {
        IParameterConverter p = getConverterForType(propertyType);
        if (p != null) {
            return p.convert(factory, parameterElement);
        } else {
            return null;
        }
    }

    protected void add(IParameterConverter converter) {
        converters.add(converter);
    }

    public List<IParameterConverter> getConverters() {
        return converters;
    }

    public IParameterConverter getConverterForType(Class propertyType) {
        for (IParameterConverter p : converters) {
            if (p.handles(propertyType))
                return p;
        }
        return null;
    }

    public class IntegerConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(int.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            return Integer.valueOf((int) factory.computeDouble(parameterElement.getValue()));
        }
    }

    public class StringConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(String.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            return parameterElement.getValue();
        }
    }

    public class DoubleConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(double.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            return Double.valueOf(factory.computeDouble(parameterElement.getValue()));
        }
    }

    public class FloatConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(float.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            return Float.valueOf(factory.computeFloat(parameterElement.getValue()));
        }
    }

    public class BooleanConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(boolean.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            return Boolean.valueOf(parameterElement.getValue());
        }
    }

    public class Hep3VectorConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(Hep3Vector.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            StringTokenizer tokenize = new StringTokenizer(parameterElement.getValue());
            double x = Double.valueOf(factory.computeDouble(tokenize.nextToken()));
            double y = Double.valueOf(factory.computeDouble(tokenize.nextToken()));
            double z = Double.valueOf(factory.computeDouble(tokenize.nextToken()));
            return new BasicHep3Vector(x, y, z);
        }
    }

    public class DoubleArray1DConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.getName().equals("[D");
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            StringTokenizer tokenize = new StringTokenizer(parameterElement.getValue());
            int size = tokenize.countTokens();
            double da[] = new double[size];
            int i = 0;
            while (tokenize.hasMoreTokens()) {
                da[i] = Double.valueOf(factory.computeDouble(tokenize.nextToken()));
                ++i;
            }
            return da;
        }
    }

    public class IntegerArray1DConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.getName().equals("[I");
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            StringTokenizer tokenize = new StringTokenizer(parameterElement.getValue());
            int size = tokenize.countTokens();
            int ia[] = new int[size];
            int i = 0;
            while (tokenize.hasMoreTokens()) {
                ia[i] = Integer.valueOf((int) factory.computeDouble(tokenize.nextToken()));
                ++i;
            }
            return ia;
        }
    }

    public class FloatArray1DConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.getName().equals("[F");
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            StringTokenizer tokenize = new StringTokenizer(parameterElement.getValue());
            int size = tokenize.countTokens();
            float fa[] = new float[size];
            int i = 0;
            while (tokenize.hasMoreTokens()) {
                fa[i] = Float.valueOf(factory.computeFloat(tokenize.nextToken()));
                ++i;
            }
            return fa;
        }
    }

    public class StringArray1DConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.getName().equals("[Ljava.lang.String;");
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            StringTokenizer tokenize = new StringTokenizer(parameterElement.getValue());
            int size = tokenize.countTokens();
            String sa[] = new String[size];
            int i = 0;
            while (tokenize.hasMoreTokens()) {
                sa[i] = tokenize.nextToken();
                ++i;
            }
            return sa;
        }
    }

    public class BooleanArray1DConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.getName().equals("[Z");
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            StringTokenizer tokenize = new StringTokenizer(parameterElement.getValue());
            int size = tokenize.countTokens();
            boolean ba[] = new boolean[size];
            int i = 0;
            while (tokenize.hasMoreTokens()) {
                ba[i] = Boolean.valueOf(tokenize.nextToken());
                ++i;
            }
            return ba;
        }
    }

    public class FileConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(File.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            return new File(parameterElement.getValue());
        }
    }

    public class URLConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(URL.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            try {
                return new URL(parameterElement.getValue());
            } catch (MalformedURLException x) {
                throw new RuntimeException("Bad URL " + parameterElement.getValue() + " in XML job description.", x);
            }
        }
    }

    public class DoubleArray2DConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.getName().equals("[[D");
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            // Parse into a list of list of doubles.
            StringTokenizer tokenize = new StringTokenizer(parameterElement.getValue(), ";");
            int length = tokenize.countTokens();
            int ir = 0;
            List<List<Double>> rows = new ArrayList<List<Double>>(length);
            while (tokenize.hasMoreTokens()) {
                String rowStr = tokenize.nextToken();
                StringTokenizer tokenize2 = new StringTokenizer(rowStr);
                rows.add(new ArrayList<Double>());
                List<Double> row = rows.get(ir);
                while (tokenize2.hasMoreTokens()) {
                    String entry = tokenize2.nextToken();
                    Double d = factory.computeDouble(entry);
                    row.add(d);
                }
                ++ir;
            }

            // Convert list of doubles into 2D array, checking for wrong sized rows.
            double arr[][] = new double[rows.size()][rows.get(0).size()];
            int jcheck = rows.get(0).size() - 1;
            int i = 0;
            int j = 0;
            for (List<Double> aa : rows) {
                for (Double a : aa) {
                    if (j > jcheck) {
                        throw new RuntimeException("Row " + j + " of array " + parameterElement.getName()
                                + "with length " + aa.size() + " is too long.");
                    }
                    arr[i][j] = a;
                    ++j;
                }
                if (j < jcheck) {
                    throw new RuntimeException("Row " + j + " of array " + parameterElement.getName() + "with length "
                            + aa.size() + "is too short.");
                }
                j = 0;
                ++i;
            }
            return arr;
        }
    }

    public class IntegerArray2DConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.getName().equals("[[I");
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            // Parse into a list of list of doubles.
            StringTokenizer tokenize = new StringTokenizer(parameterElement.getValue(), ";");
            int length = tokenize.countTokens();
            int ir = 0;
            List<List<Integer>> rows = new ArrayList<List<Integer>>(length);
            while (tokenize.hasMoreTokens()) {
                String rowStr = tokenize.nextToken();
                StringTokenizer tokenize2 = new StringTokenizer(rowStr);
                rows.add(new ArrayList<Integer>());
                List<Integer> row = rows.get(ir);
                while (tokenize2.hasMoreTokens()) {
                    String entry = tokenize2.nextToken();
                    Integer d = Integer.valueOf(entry);
                    row.add(d);
                }
                ++ir;
            }

            // Convert list of doubles into 2D array, checking for wrong sized rows.
            int arr[][] = new int[rows.size()][rows.get(0).size()];
            int jcheck = rows.get(0).size() - 1;
            int i = 0;
            int j = 0;
            for (List<Integer> aa : rows) {
                for (Integer a : aa) {
                    if (j > jcheck) {
                        throw new RuntimeException("Row " + j + " of array " + parameterElement.getName()
                                + "with length " + aa.size() + " is too long.");
                    }
                    arr[i][j] = a;
                    ++j;
                }
                if (j < jcheck) {
                    throw new RuntimeException("Row " + j + " of array " + parameterElement.getName() + "with length "
                            + aa.size() + "is too short.");
                }
                j = 0;
                ++i;
            }
            return arr;
        }
    }

    public class ElementConverter implements IParameterConverter {

        public boolean handles(Class propertyType) {
            return propertyType.equals(Element.class);
        }

        public Object convert(JDOMExpressionFactory factory, Element parameterElement) {
            return parameterElement;
        }
    }
}