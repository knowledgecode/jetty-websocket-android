//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.util.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/* ------------------------------------------------------------ */
/** 
 * Abstract resource class.
 */
public abstract class Resource
{
    private static final Logger LOG = Log.getLogger(Resource.class);
    public static boolean __defaultUseCaches = true;
    volatile Object _associate;

    /* ------------------------------------------------------------ */
    /** Construct a resource from a url.
     * @param url A URL.
     * @return A Resource object.
     * @throws IOException Problem accessing URL
     */
    public static Resource newResource(URL url)
        throws IOException
    {
        return newResource(url, __defaultUseCaches);
    }

    /* ------------------------------------------------------------ */
    /**
     * Construct a resource from a url.
     * @param url the url for which to make the resource
     * @param useCaches true enables URLConnection caching if applicable to the type of resource
     * @return
     */
    static Resource newResource(URL url, boolean useCaches)
    {
        if (url==null)
            return null;

        String url_string=url.toExternalForm();
        if( url_string.startsWith( "file:"))
        {
            try
            {
                FileResource fileResource= new FileResource(url);
                return fileResource;
            }
            catch(Exception e)
            {
                LOG.debug(Log.EXCEPTION,e);
                return new BadResource(url,e.toString());
            }
        }
        else if( url_string.startsWith( "jar:file:"))
        {
            return new JarFileResource(url, useCaches);
        }
        else if( url_string.startsWith( "jar:"))
        {
            return new JarResource(url, useCaches);
        }

        return new URLResource(url,null,useCaches);
    }

    /* ------------------------------------------------------------ */
    /** Construct a resource from a string.
     * @param resource A URL or filename.
     * @return A Resource object.
     */
    public static Resource newResource(String resource)
        throws MalformedURLException, IOException
    {
        return newResource(resource, __defaultUseCaches);
    }

    /* ------------------------------------------------------------ */
    /** Construct a resource from a string.
     * @param resource A URL or filename.
     * @param useCaches controls URLConnection caching
     * @return A Resource object.
     */
    public static Resource newResource (String resource, boolean useCaches)
    throws MalformedURLException, IOException
    {
        URL url=null;
        try
        {
            // Try to format as a URL?
            url = new URL(resource);
        }
        catch(MalformedURLException e)
        {
            if(!resource.startsWith("ftp:") &&
               !resource.startsWith("file:") &&
               !resource.startsWith("jar:"))
            {
                try
                {
                    // It's a file.
                    if (resource.startsWith("./"))
                        resource=resource.substring(2);

                    File file=new File(resource).getCanonicalFile();
                    url=Resource.toURL(file);

                    URLConnection connection=url.openConnection();
                    connection.setUseCaches(useCaches);
                    return new FileResource(url,connection,file);
                }
                catch(Exception e2)
                {
                    LOG.debug(Log.EXCEPTION,e2);
                    throw e;
                }
            }
            else
            {
                LOG.warn("Bad Resource: "+resource);
                throw e;
            }
        }

        return newResource(url);
    }

    /* ------------------------------------------------------------ */
    /**
     * Returns an input stream to the resource
     */
    public abstract InputStream getInputStream()
        throws java.io.IOException;

    /* ------------------------------------------------------------ */
    /** Generate a properly encoded URL from a {@link File} instance.
     * @param file Target file. 
     * @return URL of the target file.
     * @throws MalformedURLException 
     */
    public static URL toURL(File file) throws MalformedURLException
    {
        return file.toURI().toURL();
    }
}
