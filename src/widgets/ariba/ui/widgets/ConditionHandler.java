/*
    Copyright 1996-2008 Ariba, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    $Id: //ariba/platform/ui/widgets/ariba/ui/widgets/ConditionHandler.java#9 $
*/

package ariba.ui.widgets;

import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWRequestContext;

public abstract class ConditionHandler extends BaseHandler
{
    public static void setDefaultHandler (ConditionHandler handler)
    {
        BaseHandler.setDefaultHandler(ConditionHandler.class, handler);
    }

    public static void setHandler (String condition, ConditionHandler handler)
    {
        BaseHandler.setHandler(condition, ConditionHandler.class, handler);
    }
    
    public static ConditionHandler resolveHandlerInComponent (String condition, AWComponent component)
    {
        return (ConditionHandler)resolveHandler(component, condition, ConditionHandler.class);
    }

    public abstract boolean evaluateCondition (AWRequestContext requestContext);
}
