package org.lcsim.util.fourvec;

public class Momentum4Vector implements Lorentz4Vector
{
    //
    //  Basic Lorentz 4-vector contains only x,y,z and ct (px,py,pz and E)
    //
    private double _E;
    private double  _px;
    private double  _py;
    private double  _pz;
    
    // Default Constructor
    public  Momentum4Vector()
    {
    }
    // fully qualified Constructor
    public Momentum4Vector(double f1,double f2,double f3, double f4)
    {
        _px = f1;
        _py = f2;
        _pz=f3;
        _E=f4;
    }
    
    // copy constructor
    public Momentum4Vector(Lorentz4Vector p)
    {
        _px = p.px();
        _py = p.py();
        _pz = p.pz();
        _E = p.E();
    }
    
    // equality operator
    public boolean equals(Object o)
    {
        if(o instanceof Momentum4Vector) return equals((Momentum4Vector) o);
        else return false;
    }
    
    //hashcode
    public int hashcode()
    {
        int result = 17;
        long code = Double.doubleToLongBits(_px);
        result = 37*result + (int)(code^(code>>>32));
        code = Double.doubleToLongBits(_py);
        result = 37*result + (int)(code^(code>>>32));
        Double.doubleToLongBits(_pz);
        result = 37*result + (int)(code^(code>>>32));
        Double.doubleToLongBits(_E);
        result = 37*result + (int)(code^(code>>>32));
        return result;
    }
    
    public boolean  equals(Momentum4Vector y)
    {
        return _px==y.px() && _py==y.py() && _pz==y.pz() && _E==y.E();
    }
    
    //sum operator
    public Lorentz4Vector plus(Lorentz4Vector y2)
    {
        return new Momentum4Vector(_px+y2.px(),_py+y2.py(),_pz+y2.pz(),
        _E+y2.E());
    }
    
    // sum in place operator
    public void plusEquals(Lorentz4Vector y1)
    {
        _px+=y1.px();
        _py+=y1.py();
        _pz+=y1.pz();
        _E+=y1.E();
    }
    
    public void plusEquals(double f1,double f2,double f3, double f4)
    {
        _px+=f1;
        _py+=f2;
        _pz+=f3;
        _E+=f4;
    }
    
    // subtract operator
    public Lorentz4Vector minus(Lorentz4Vector y1,Lorentz4Vector y2)
    {
        return new Momentum4Vector(y1.px()-y2.px(),y1.py()-y2.py(),y1.pz()-y2.pz(),
        y1.E()-y2.E());
    }
    // multiply by constant
    public Lorentz4Vector times(double a,Lorentz4Vector y)
    {
        return new Momentum4Vector(a*y.px(),a*y.py(),a*y.pz(),a*y.E());
    }
    
    public Lorentz4Vector times(Lorentz4Vector y,double a)
    {
        return times(a,y);
    }
    // divide by constant
    public Lorentz4Vector divide(Lorentz4Vector y, double a)
    {
        return new Momentum4Vector(y.px()/a,y.py()/a,y.pz()/a,y.E()/a);
    }
    // output operator
    public String toString()
    {
        return "\n"+_px+", "+_py+", "+_pz+", "+_E+", "+mass();
    }
    
    // dot product
    public double dot(Lorentz4Vector y1)
    {
        return y1.E()*_E-y1.px()*_px-y1.py()*_py -y1.pz()*_pz;
    }
    // space components  dot product
    public double vec3dot(Lorentz4Vector y1)
    {
        return y1.px()*_px + y1.py()*_py + y1.pz()*_pz;
    }
    
    public double px()
    {
        return _px;
    }
    public double py()
    {
        return _py;
    }
    public double pz()
    {
        return _pz;
    }
    public double E()
    {
        return _E;
    }
    public double pT()
    {
        return Math.sqrt(_px*_px+_py*_py);
    }
    public double p()
    {
        return Math.sqrt(_px*_px+_py*_py+_pz*_pz);
    }
    public double phi()
    {
        double ph=Math.atan2(_py,_px);
        return (ph<0.) ?  ph+2.*Math.acos(-1) : ph;
    }
    public double theta()
    {
        return Math.acos(_pz/p());
    }
    public double mass2()
    {
        return dot(this);
    }
    //  pseudo-rapidity
    //  public double eta()const;
    //  true rapidity
    //  public double y()const;
    //float Momentum4Vector::eta()const{
    //  float pt=std::max ((float)sqrt(_px*_px + _py*_py), (float)1e-10);
    //  return (_pz>0) ?
    //    log((sqrt(_px*_px + _py*_py + _pz*_pz) + _pz) / pt) :
    //    log(pt / (sqrt(pt*pt + _pz*_pz) - _pz));
    //}
    
    //float Momentum4Vector::y()const{
    //  return 0.5 * log ((_E+_pz+(float)1e-10)/(_E-_pz+(float)1e-10));
    //}
    
    public double mass()
    {
        double m;
        m=mass2();
        return (m < 0) ? -Math.sqrt(-m) : Math.sqrt(m);
    }
    
    public Lorentz4Vector boost(Lorentz4Vector prest)
    {
        double mrest=prest.mass();
        double Enew=dot(prest)/mrest;
        double f=(Enew+_E)/(prest.E()+mrest);
        double pxnew=_px-f*prest.px();
        double pynew=_py-f*prest.py();
        double pznew=_pz-f*prest.pz();
        return new Momentum4Vector(-pxnew,-pynew,-pznew,Enew);
    }
    
    public Lorentz4Vector[] twobodyDecay(double m1, double m2)
    {
        double m0 = mass();
        double e0 = (m0*m0+m1*m1-m2*m2)/(2.*m0);
        double p0 = Math.sqrt(Math.abs((e0-m1)*(e0+m1)));
        // generate random decay in cms
        java.util.Random ran = new java.util.Random();
        double costh = 2.*ran.nextDouble()-1.;
        double sinth = Math.sqrt((1.-costh)*(1.+costh));
        
        double phi = 2.*Math.PI*ran.nextDouble();
        // polar coordinates to momentum components
        Lorentz4Vector[] vec = new Lorentz4Vector[2];
        double p1 = p0*sinth*Math.cos(phi);
        double p2 = p0*sinth*Math.sin(phi);
        double p3 = p0*costh;
        double p4 = e0;
        vec[0] = (new Momentum4Vector(p1,p2,p3,p4)).boost(this);
        p4 = Math.sqrt(p1*p1+p2*p2+p3*p3+m2*m2);
        vec[1] = (new Momentum4Vector(-p1,-p2,-p3,p4)).boost(this);
        return vec;
    }
}