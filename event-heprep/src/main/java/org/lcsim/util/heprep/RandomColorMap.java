package org.lcsim.util.heprep;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomColorMap
{
    List<Color> colors;
 
    public RandomColorMap(int ncolors)
    {
        colors = new ArrayList<Color>(ncolors);
        Random rand = new Random();
        for (int i=0; i<ncolors; i++)
        {
            colors.add(i, new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
        }
        Collections.shuffle(colors);
    }

    public void shuffle()
    {
        Collections.shuffle(colors);
    }

    public int size()
    {
        return colors.size();
    }

    public Color getColor(int index)
    {
        return colors.get(index);
    }   
    
    public void reset(int ncolors)
    {
        colors = new ArrayList<Color>(ncolors);
        Random rand = new Random();
        for (int i=0; i<ncolors; i++)
        {
            colors.add(i, new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
        }
        Collections.shuffle(colors);
    }
}