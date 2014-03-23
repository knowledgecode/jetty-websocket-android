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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* ------------------------------------------------------------ */
/** Lazy List creation.
 * A List helper class that attempts to avoid unnecessary List
 * creation.   If a method needs to create a List to return, but it is
 * expected that this will either be empty or frequently contain a
 * single item, then using LazyList will avoid additional object
 * creations by using {@link Collections#EMPTY_LIST} or
 * {@link Collections#singletonList(Object)} where possible.
 * <p>
 * LazyList works by passing an opaque representation of the list in
 * and out of all the LazyList methods.  This opaque object is either
 * null for an empty list, an Object for a list with a single entry
 * or an {@link ArrayList} for a list of items.
 *
 * <p><h4>Usage</h4>
 * <pre>
 *   Object lazylist =null;
 *   while(loopCondition)
 *   {
 *     Object item = getItem();
 *     if (item.isToBeAdded())
 *         lazylist = LazyList.add(lazylist,item);
 *   }
 *   return LazyList.getList(lazylist);
 * </pre>
 *
 * An ArrayList of default size is used as the initial LazyList.
 *
 * @see java.util.List
 */
public class LazyList
    implements Cloneable, Serializable
{
    private static final long serialVersionUID = 3858918581954056484L;

    /* ------------------------------------------------------------ */
    private LazyList()
    {}

    /* ------------------------------------------------------------ */
    /** Add an item to a LazyList 
     * @param list The list to add to or null if none yet created.
     * @param item The item to add.
     * @return The lazylist created or added to.
     */
    @SuppressWarnings("unchecked")
    public static Object add(Object list, Object item)
    {
        if (list==null)
        {
            if (item instanceof List || item==null)
            {
                List<Object> l = new ArrayList<Object>();
                l.add(item);
                return l;
            }

            return item;
        }

        if (list instanceof List)
        {
            ((List<Object>)list).add(item);
            return list;
        }

        List<Object> l=new ArrayList<Object>();
        l.add(list);
        l.add(item);
        return l;
    }

    /* ------------------------------------------------------------ */
    /** Get the real List from a LazyList.
     * 
     * @param list A LazyList returned from LazyList.add(Object) or null
     * @param nullForEmpty If true, null is returned instead of an
     * empty list.
     * @return The List of added items, which may be null, an EMPTY_LIST
     * or a SingletonList.
     */
    @SuppressWarnings("unchecked")
    public static<E> List<E> getList(Object list, boolean nullForEmpty)
    {
        if (list==null)
        {
            if (nullForEmpty)
                return null;
            return Collections.emptyList();
        }
        if (list instanceof List)
            return (List<E>)list;

        return (List<E>)Collections.singletonList(list);
    }
}
