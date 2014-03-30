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

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

/* ------------------------------------------------------------ */
class JarFileResource extends JarResource
{
    private JarFile _jarFile;
    private String _path;

    /* ------------------------------------------------------------ */
    JarFileResource(URL url, boolean useCaches)
    {
        super(url, useCaches);
    }

    /* ------------------------------------------------------------ */
    @Override
    protected boolean checkConnection()
    {
        try
        {
            super.checkConnection();
        }
        finally
        {
            if (_jarConnection==null)
            {
                _jarFile=null;
            }
        }
        return _jarFile!=null;
    }

    /* ------------------------------------------------------------ */
    @Override
    protected synchronized void newConnection()
        throws IOException
    {
        super.newConnection();

        _jarFile=null;

        int sep = _urlString.indexOf("!/");
        _path=_urlString.substring(sep+2);
        if (_path.length()==0)
            _path=null;
        _jarFile=_jarConnection.getJarFile();
    }
}
