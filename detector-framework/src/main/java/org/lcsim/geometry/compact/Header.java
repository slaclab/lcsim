package org.lcsim.geometry.compact;

import org.jdom.Element;

/**
 * The header of the compact detector description.
 * 
 * @author tonyj
 */
public class Header
{
    private String name;
    private String title = "NONE";
    private String author = "NONE";
    private String version = "NONE";
    private String url = "NONE";
    private String comment = "NONE";
    private String status = "NONE";

    protected Header( Element info )
    {
        if ( info.getAttributeValue( "name" ) != null )
        {
            name = info.getAttributeValue( "name" );
        }
        else
        {
            throw new RuntimeException( "The <info> element is missing the name field." );
        }

        if ( info.getAttribute( "author" ) != null )
        {
            author = info.getAttributeValue( "author" );
        }

        if ( info.getAttribute( "title" ) != null )
        {
            title = info.getAttributeValue( "title" );
        }

        if ( info.getAttribute( "version" ) != null )
        {
            version = info.getAttributeValue( "version" );
        }

        if ( info.getAttribute( "url" ) != null )
        {
            url = info.getAttributeValue( "url" );
        }

        if ( info.getAttribute( "status" ) != null )
        {
            status = info.getAttributeValue( "status" );
        }
        
        if ( info.getChild( "comment" ) != null )
        {
            comment = info.getChild( "comment" ).getTextNormalize();
        }

    }

    /**
     * Get the detector name.
     * 
     * @return The name.
     */
    public String getDetectorName()
    {
        return name;
    }

    /**
     * Get the author of this detector description.
     * 
     * @return The author.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Get the version of the detector.
     * 
     * @return The version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * The URL providing more information about this detector.
     * 
     * @return The URL.
     */
    public String getURL()
    {
        return url;
    }

    /**
     * A comment describing the detector
     * 
     * @return The comment.
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * Get the title of this detector.
     * 
     * @return The title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Get the production status of this detector.
     * 
     * @return The status description.
     */
    public String getStatus()
    {
        return status;
    }
}