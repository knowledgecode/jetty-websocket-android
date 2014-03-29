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

package org.eclipse.jetty.util;

import java.io.UnsupportedEncodingException;

/* ------------------------------------------------------------ */
/** URI Holder.
 * This class assists with the decoding and encoding or HTTP URI's.
 * It differs from the java.net.URL class as it does not provide
 * communications ability, but it does assist with query string
 * formatting.
 * <P>UTF-8 encoding is used by default for % encoded characters. This
 * may be overridden with the org.eclipse.jetty.util.URI.charset system property.
 * @see UrlEncoded
 * 
 */
public class URIUtil
    implements Cloneable
{
    public static final String SLASH="/";

    // Use UTF-8 as per http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars
    public static final String __CHARSET=System.getProperty("org.eclipse.jetty.util.URI.charset",StringUtil.__UTF8);

    private URIUtil()
    {}

    /* ------------------------------------------------------------ */
    /** Encode a URI path.
     * This is the same encoding offered by URLEncoder, except that
     * the '/' character is not encoded.
     * @param path The path the encode
     * @return The encoded path
     */
    public static String encodePath(String path)
    {
        if (path==null || path.length()==0)
            return path;

        StringBuilder buf = encodePath(null,path);
        return buf==null?path:buf.toString();
    }

    /* ------------------------------------------------------------ */
    /** Encode a URI path.
     * @param path The path the encode
     * @param buf StringBuilder to encode path into (or null)
     * @return The StringBuilder or null if no substitutions required.
     */
    public static StringBuilder encodePath(StringBuilder buf, String path)
    {
        byte[] bytes=null;
        if (buf==null)
        {
        loop:
            for (int i=0;i<path.length();i++)
            {
                char c=path.charAt(i);
                switch(c)
                {
                    case '%':
                    case '?':
                    case ';':
                    case '#':
                    case '\'':
                    case '"':
                    case '<':
                    case '>':
                    case ' ':
                        buf=new StringBuilder(path.length()*2);
                        break loop;
                    default:
                        if (c>127)
                        {
                            try
                            {
                                bytes=path.getBytes(URIUtil.__CHARSET);
                            }
                            catch (UnsupportedEncodingException e)
                            {
                                throw new IllegalStateException(e);
                            }
                            buf=new StringBuilder(path.length()*2);
                            break loop;
                        }
                }
            }
            if (buf==null)
                return null;
        }

        synchronized(buf)
        {
            if (bytes!=null)
            {
                for (int i=0;i<bytes.length;i++)
                {
                    byte c=bytes[i];
                    switch(c)
                    {
                      case '%':
                          buf.append("%25");
                          continue;
                      case '?':
                          buf.append("%3F");
                          continue;
                      case ';':
                          buf.append("%3B");
                          continue;
                      case '#':
                          buf.append("%23");
                          continue;
                      case '"':
                          buf.append("%22");
                          continue;
                      case '\'':
                          buf.append("%27");
                          continue;
                      case '<':
                          buf.append("%3C");
                          continue;
                      case '>':
                          buf.append("%3E");
                          continue;
                      case ' ':
                          buf.append("%20");
                          continue;
                      default:
                          if (c<0)
                          {
                              buf.append('%');
                              TypeUtil.toHex(c,buf);
                          }
                          else
                              buf.append((char)c);
                          continue;
                    }
                }
            }
            else
            {
                for (int i=0;i<path.length();i++)
                {
                    char c=path.charAt(i);
                    switch(c)
                    {
                        case '%':
                            buf.append("%25");
                            continue;
                        case '?':
                            buf.append("%3F");
                            continue;
                        case ';':
                            buf.append("%3B");
                            continue;
                        case '#':
                            buf.append("%23");
                            continue;
                        case '"':
                            buf.append("%22");
                            continue;
                        case '\'':
                            buf.append("%27");
                            continue;
                        case '<':
                            buf.append("%3C");
                            continue;
                        case '>':
                            buf.append("%3E");
                            continue;
                        case ' ':
                            buf.append("%20");
                            continue;
                        default:
                            buf.append(c);
                            continue;
                    }
                }
            }
        }

        return buf;
    }

    /* ------------------------------------------------------------ */
    /* Decode a URI path and strip parameters
     * @param path The path the encode
     * @param buf StringBuilder to encode path into
     */
    public static String decodePath(String path)
    {
        if (path==null)
            return null;
        // Array to hold all converted characters
        char[] chars=null;
        int n=0;
        // Array to hold a sequence of %encodings
        byte[] bytes=null;
        int b=0;

        int len=path.length();

        for (int i=0;i<len;i++)
        {
            char c = path.charAt(i);

            if (c=='%' && (i+2)<len)
            {
                if (chars==null)
                {
                    chars=new char[len];
                    bytes=new byte[len];
                    path.getChars(0,i,chars,0);
                }
                bytes[b++]=(byte)(0xff&TypeUtil.parseInt(path,i+1,2,16));
                i+=2;
                continue;
            }
            else if (c==';')
            {
                if (chars==null)
                {
                    chars=new char[len];
                    path.getChars(0,i,chars,0);
                    n=i;
                }
                break;
            }
            else if (bytes==null)
            {
                n++;
                continue;
            }

            // Do we have some bytes to convert?
            if (b>0)
            {
                // convert series of bytes and add to chars
                String s;
                try
                {
                    s=new String(bytes,0,b,__CHARSET);
                }
                catch (UnsupportedEncodingException e)
                {
                    s=new String(bytes,0,b);
                }
                s.getChars(0,s.length(),chars,n);
                n+=s.length();
                b=0;
            }

            chars[n++]=c;
        }

        if (chars==null)
            return path;

        // if we have a remaining sequence of bytes
        if (b>0)
        {
            // convert series of bytes and add to chars
            String s;
            try
            {
                s=new String(bytes,0,b,__CHARSET);
            }
            catch (UnsupportedEncodingException e)
            {
                s=new String(bytes,0,b);
            }
            s.getChars(0,s.length(),chars,n);
            n+=s.length();
        }

        return new String(chars,0,n);
    }

    /* ------------------------------------------------------------ */
    /** Add two URI path segments.
     * Handles null and empty paths, path and query params (eg ?a=b or
     * ;JSESSIONID=xxx) and avoids duplicate '/'
     * @param p1 URI path segment (should be encoded)
     * @param p2 URI path segment (should be encoded)
     * @return Legally combined path segments.
     */
    public static String addPaths(String p1, String p2)
    {
        if (p1==null || p1.length()==0)
        {
            if (p1!=null && p2==null)
                return p1;
            return p2;
        }
        if (p2==null || p2.length()==0)
            return p1;

        int split=p1.indexOf(';');
        if (split<0)
            split=p1.indexOf('?');
        if (split==0)
            return p2+p1;
        if (split<0)
            split=p1.length();

        StringBuilder buf = new StringBuilder(p1.length()+p2.length()+2);
        buf.append(p1);

        if (buf.charAt(split-1)=='/')
        {
            if (p2.startsWith(URIUtil.SLASH))
            {
                buf.deleteCharAt(split-1);
                buf.insert(split-1,p2);
            }
            else
                buf.insert(split,p2);
        }
        else
        {
            if (p2.startsWith(URIUtil.SLASH))
                buf.insert(split,p2);
            else
            {
                buf.insert(split,'/');
                buf.insert(split+1,p2);
            }
        }

        return buf.toString();
    }

    /* ------------------------------------------------------------ */
    /** Convert a path to a cananonical form.
     * All instances of "." and ".." are factored out.  Null is returned
     * if the path tries to .. above its root.
     * @param path 
     * @return path or null.
     */
    public static String canonicalPath(String path)
    {
        if (path==null || path.length()==0)
            return path;

        int end=path.length();
        int start = path.lastIndexOf('/', end);

    search:
        while (end>0)
        {
            switch(end-start)
            {
              case 2: // possible single dot
                  if (path.charAt(start+1)!='.')
                      break;
                  break search;
              case 3: // possible double dot
                  if (path.charAt(start+1)!='.' || path.charAt(start+2)!='.')
                      break;
                  break search;
            }

            end=start;
            start=path.lastIndexOf('/',end-1);
        }

        // If we have checked the entire string
        if (start>=end)
            return path;

        StringBuilder buf = new StringBuilder(path);
        int delStart=-1;
        int delEnd=-1;
        int skip=0;

        while (end>0)
        {
            switch(end-start)
            {
              case 2: // possible single dot
                  if (buf.charAt(start+1)!='.')
                  {
                      if (skip>0 && --skip==0)
                      {
                          delStart=start>=0?start:0;
                          if(delStart>0 && delEnd==buf.length() && buf.charAt(delEnd-1)=='.')
                              delStart++;
                      }
                      break;
                  }

                  if(start<0 && buf.length()>2 && buf.charAt(1)=='/' && buf.charAt(2)=='/')
                      break;

                  if(delEnd<0)
                      delEnd=end;
                  delStart=start;
                  if (delStart<0 || delStart==0&&buf.charAt(delStart)=='/')
                  {
                      delStart++;
                      if (delEnd<buf.length() && buf.charAt(delEnd)=='/')
                          delEnd++;
                      break;
                  }
                  if (end==buf.length())
                      delStart++;

                  end=start--;
                  while (start>=0 && buf.charAt(start)!='/')
                      start--;
                  continue;

              case 3: // possible double dot
                  if (buf.charAt(start+1)!='.' || buf.charAt(start+2)!='.')
                  {
                      if (skip>0 && --skip==0)
                      {   delStart=start>=0?start:0;
                          if(delStart>0 && delEnd==buf.length() && buf.charAt(delEnd-1)=='.')
                              delStart++;
                      }
                      break;
                  }

                  delStart=start;
                  if (delEnd<0)
                      delEnd=end;

                  skip++;
                  end=start--;
                  while (start>=0 && buf.charAt(start)!='/')
                      start--;
                  continue;

              default:
                  if (skip>0 && --skip==0)
                  {
                      delStart=start>=0?start:0;
                      if(delEnd==buf.length() && buf.charAt(delEnd-1)=='.')
                          delStart++;
                  }
            }

            // Do the delete
            if (skip<=0 && delStart>=0 && delEnd>=delStart)
            {
                buf.delete(delStart,delEnd);
                delStart=delEnd=-1;
                if (skip>0)
                    delEnd=end;
            }

            end=start--;
            while (start>=0 && buf.charAt(start)!='/')
                start--;
        }

        // Too many
        if (skip>0)
            return null;

        // Do the delete
        if (delEnd>=0)
            buf.delete(delStart,delEnd);

        return buf.toString();
    }
}
