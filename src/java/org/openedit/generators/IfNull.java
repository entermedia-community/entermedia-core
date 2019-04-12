package org.openedit.generators;

/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * @author <a href="mailto:shinobu@ieee.org">Shinobu Kawai</a>
 * @version $Id: $
 */
public class IfNull extends Directive
{

    /* (non-Javadoc)
     * @see org.apache.velocity.runtime.directive.Directive#getName()
     */
    public String getName()
    {
        return "ifnull";
    }

    /* (non-Javadoc)
     * @see org.apache.velocity.runtime.directive.Directive#getType()
     */
    public int getType()
    {
        return BLOCK;
    }

    /* (non-Javadoc)
     * @see org.apache.velocity.runtime.directive.Directive#render(org.apache.velocity.context.InternalContextAdapter, java.io.Writer, org.apache.velocity.runtime.parser.node.Node)
     */
    public boolean render(InternalContextAdapter context, Writer writer,
            Node node) throws IOException, ResourceNotFoundException,
            ParseErrorException, MethodInvocationException
    {
        Object value = node.jjtGetChild(0).value(context);
        if (value == null)
        {
            Node content = node.jjtGetChild(1);
            content.render(context, writer);
        }
        return true;
    }

}

