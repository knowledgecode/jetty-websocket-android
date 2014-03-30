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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;

import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/* ------------------------------------------------------------ */
/** File Resource.
 *
 * Handle resources of implied or explicit file type.
 * This class can check for aliasing in the filesystem (eg case
 * insensitivity).  By default this is turned on, or it can be controlled 
 * by calling the static method @see FileResource#setCheckAliases(boolean)
 * 
 */
public class FileResource extends URLResource
{
    private static final Logger LOG = Log.getLogger(FileResource.class);

    /* ------------------------------------------------------------ */
    private File _file;

    /* -------------------------------------------------------- */
    public FileResource(URL url)
        throws IOException, URISyntaxException
    {
        super(url,null);

        try
        {
            // Try standard API to convert URL to file.
            _file =new File(new URI(url.toString()));
        }
        catch (URISyntaxException e) 
        {
            throw e;
        }
        catch (Exception e)
        {
            LOG.ignore(e);
            try
            {
                // Assume that File.toURL produced unencoded chars. So try
                // encoding them.
                String file_url="file:"+URIUtil.encodePath(url.toString().substring(5));
                URI uri = new URI(file_url);
                if (uri.getAuthority()==null) 
                    _file = new File(uri);
                else
                    _file = new File("//"+uri.getAuthority()+URIUtil.decodePath(url.getFile()));
            }
            catch (Exception e2)
            {
                LOG.ignore(e2);

                // Still can't get the file.  Doh! try good old hack!
                checkConnection();
                Permission perm = _connection.getPermission();
                _file = new File(perm==null?url.getFile():perm.getName());
            }
        }
        if (_file.isDirectory())
        {
            if (!_urlString.endsWith("/"))
                _urlString=_urlString+"/";
        }
        else
        {
            if (_urlString.endsWith("/"))
                _urlString=_urlString.substring(0,_urlString.length()-1);
        }

    }

    /* -------------------------------------------------------- */
    FileResource(URL url, URLConnection connection, File file)
    {
        super(url,connection);
        _file=file;
        if (_file.isDirectory() && !_urlString.endsWith("/"))
            _urlString=_urlString+"/";
    }

    /* --------------------------------------------------------- */
    /**
     * Returns an input stream to the resource
     */
    @Override
    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(_file);
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param o
     * @return <code>true</code> of the object <code>o</code> is a {@link FileResource} pointing to the same file as this resource. 
     */
    @Override
    public boolean equals( Object o)
    {
        if (this == o)
            return true;

        if (null == o || ! (o instanceof FileResource))
            return false;

        FileResource f=(FileResource)o;
        return f._file == _file || (null != _file && _file.equals(f._file));
    }
}
