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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;

import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/* ------------------------------------------------------------ */
public class JarResource extends URLResource
{
    private static final Logger LOG = Log.getLogger(JarResource.class);
    protected JarURLConnection _jarConnection;

    /* ------------------------------------------------------------ */
    JarResource(URL url, boolean useCaches)
    {
        super(url, null, useCaches);
    }

    /* ------------------------------------------------------------ */
    @Override
    protected synchronized boolean checkConnection()
    {
        super.checkConnection();
        try
        {
            if (_jarConnection!=_connection)
                newConnection();
        }
        catch(IOException e)
        {
            LOG.ignore(e);
            _jarConnection=null;
        }

        return _jarConnection!=null;
    }

    /* ------------------------------------------------------------ */
    /**
     * @throws IOException Sub-classes of <code>JarResource</code> may throw an IOException (or subclass) 
     */
    protected void newConnection() throws IOException
    {
        _jarConnection=(JarURLConnection)_connection;
    }

    /* ------------------------------------------------------------ */
    @Override
    public InputStream getInputStream()
        throws java.io.IOException
    {
        checkConnection();
        if (!_urlString.endsWith("!/"))
            return new FilterInputStream(super.getInputStream()) 
            {
                @Override
                public void close() throws IOException {this.in=IO.getClosedStream();}
            };

        URL url = new URL(_urlString.substring(4,_urlString.length()-2));
        InputStream is = url.openStream();
        return is;
    }
}
