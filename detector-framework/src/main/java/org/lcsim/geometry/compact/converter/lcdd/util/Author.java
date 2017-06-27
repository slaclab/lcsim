package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;


/**
 *
 * @author tonyj
 */
public class Author extends Element
{
   
   /** Creates a new instance of Author */
   public Author()
   {
      super("author");
   }

   public void setAuthorName(String name)
   {
	  if ( name == null )
	  {
		  name = "UNKNOWN";
	  }
      setAttribute("name",name);
   }
   
   public void setAuthorEmail(String email)
   {
	 if ( email == null )
	 {
		 email = "NONE";
	 }
	 setAttribute("email",email);
   }  
}
