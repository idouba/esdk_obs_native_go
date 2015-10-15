/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
 *"The contents of this file are subject to the Sun Industry
 *Standards Source License Version 1.2 (the "License");
 *You may not use this file except in compliance with the
 *License. You may obtain a copy of the
 *License at http://wbemservices.sourceforge.net/license.html
 *
 *Software distributed under the License is distributed on
 *an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 *express or implied. See the License for the specific
 *language governing rights and limitations under the License.
 *
 *The Original Code is WBEM Services.
 *
 *The Initial Developer of the Original Code is:
 *Sun Microsystems, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): WBEM Solutions, Inc.
 *
 *
 */
package javax.wbem.cimxml;

import org.w3c.dom.Node;

/**
 * This is a helper class that walks the tree using only Node operations.  
 * This is to maintain compatibility with older and newer XML libraries 
 * without all of the classpath hassles (such as having to explicitly request 
 * Xerces on a Sun JVM, and having to use the endorsed mechanism, etc).
 * This class is complete as far as any wbemservices needs, but may not
 * contain every method available in the various JAXP versions of TreeWalker
 */
public class NodeTreeWalker
{
    Node curNode = null;
    Node topNode = null;

    /**
     * @param initialNode
     */
    public NodeTreeWalker(Node initialNode)
    {
        topNode = curNode = initialNode;
    }

    /**
     * @return
     */
    public Node getCurrentNode()
    {
        return curNode;
    }

    /**
     * 
     */
    public Node nextNode()
    {
        Node theNode = curNode;
        if (theNode != null)
        {
            curNode = theNode.getFirstChild();
            while (curNode == null && theNode != null && topNode != theNode)
            {
                curNode = theNode.getNextSibling();
                if (curNode == null)
                {
                    theNode = theNode.getParentNode();
                }
            }
        }
        return curNode;
    }

    /**
     * @return
     */
    public Node nextSibling()
    {
        if (curNode != topNode && curNode != null)
        {
            curNode = curNode.getNextSibling();
        }
        else
        {
            curNode = null;
        }
        return curNode;
    }

    /**
     * @param name
     */
    public Node getNextElement(String name)
    {
        Node nextNode = nextNode();
        if (name == null)
        {
            return nextNode;
        }
        while (curNode != null)
        {
        	if(null == nextNode)
        	{
        		throw new NullPointerException();
        	}
        	
        	if(!nextNode.getNodeName().equals(name))
        	{
        		nextNode = nextNode();
				
        	}
        	else
        	{
        		break;
        	}
            
        }
        return curNode;
    }

    /**
     * @return
     */
    public Node removeCurrent()
    {
        if (curNode != null)
        {
            Node parent = curNode.getParentNode();
            Node nextNode = curNode.getNextSibling();
            parent.removeChild(curNode);
            curNode = nextNode;
        }
        return curNode;
    }
}
