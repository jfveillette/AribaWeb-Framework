/*
    Copyright 2008 Craig Federighi

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
    file except in compliance with the License.
    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    $Id: //ariba/platform/ui/metaui/ariba/ui/meta/core/MetaIncludeComponent.java#6 $
*/
package ariba.ui.meta.core;

import ariba.ui.aribaweb.core.AWBindable;
import ariba.ui.aribaweb.core.AWBinding;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWElement;
import ariba.ui.aribaweb.core.AWIncludeComponent;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.core.AWElementIdPath;
import ariba.ui.aribaweb.core.AWApplication;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.aribaweb.util.AWGenericException;
import ariba.util.core.Assert;
import ariba.util.core.Fmt;
import ariba.util.fieldvalue.FieldPath;

import java.util.Map;
import java.util.List;

public class MetaIncludeComponent extends AWIncludeComponent
{
    private AWBinding _awpropertyMap;

    public void init (String tagName, Map bindingsHashtable)
    {
        _awpropertyMap = (AWBinding)bindingsHashtable.remove("awpropertyMap");
        super.init(tagName, bindingsHashtable);
    }


    protected void assertValidBindings ()
    {
        // override parent check to no-op
    }

    protected Meta.PropertyMap properties (AWComponent component)
    {
        Meta.PropertyMap map = (_awpropertyMap != null)
                ? (Meta.PropertyMap)_awpropertyMap.value(component) : null;
        if (map == null) {
            map = MetaContext.currentContext(component).allProperties();
        }
        return map;
    }

    public void renderResponse(AWRequestContext requestContext, AWComponent component)
    {
        // if in debugTracing mode, push context info
        if (requestContext.componentPathDebuggingEnabled()) {
            Context context = MetaContext.currentContext(component);
            requestContext.debugTrace().pushMetadata(null, context.debugTracePropertyProvider(), true);
        }

        try {
            super.renderResponse(requestContext, component);
        }
        catch (Exception e) {
            throw AWGenericException.augmentedExceptionWithMessage(Fmt.S("Exception during MetaInclude with context: %s",
                    MetaContext.currentContext(component)),
                    e);
        }
    }

    public AWResponseGenerating invokeAction(AWRequestContext requestContext, AWComponent component)
    {
        AWResponseGenerating actionResults =  super.invokeAction(requestContext, component);

        // ComponentPath inspection
        if (actionResults != null && requestContext.isPathDebugRequest()) {
            Context context = MetaContext.currentContext(component);
            requestContext.debugTrace().pushMetadata(null, context.debugTracePropertyProvider(), true);
        }

        return actionResults;
    }    

    protected String componentName (AWComponent component)
    {
        Object name = properties(component).get(UIMeta.KeyComponentName);
        return (name instanceof String)
                ? (String)name 
                : ((String)MetaContext.currentContext(component).resolveValue(name));
    }

    protected Map bindingsForNewReference (AWComponent component)
    {
        Map result = super.bindingsForNewReference(component);
        Map <String, Object> more = (Map)properties(component).get(UIMeta.KeyBindings);
        if (more != null) {
            for (Map.Entry <String, Object> entry : more.entrySet()) {
                AWBinding binding;
                Object value = entry.getValue();
                if (value instanceof FieldPath) {
                    if (((FieldPath)value).car().equals("component")) {
                        binding = AWBinding.bindingWithNameAndKeyPath(entry.getKey(),
                                ((FieldPath)value).cdr().fieldPathString());
                    }
                    else {
                        binding = new ContextFieldPathBinding();
                        ((ContextFieldPathBinding)binding).init(entry.getKey(), (FieldPath)value);
                    }
                }
                else if (value instanceof Context.DynamicPropertyValue) {
                    binding = new DynamicValueBinding();
                    ((DynamicValueBinding)binding).init(entry.getKey(), (Context.DynamicPropertyValue)value);
                }
                // Todo: ack!  Need a more formal packaging of deferred bindings...
                else if (value instanceof List && ((List)value).size() == 1 && (((List)value).get(0) instanceof Context.DynamicPropertyValue)) {
                    binding = new DynamicValueBinding();
                    ((DynamicValueBinding)binding).init(entry.getKey(), (Context.DynamicPropertyValue)((List)value).get(0));
                }
                else {
                    binding = AWBinding.bindingWithNameAndConstant(entry.getKey(), value);
                }
                result.put(entry.getKey(), binding);
            }
        }
        return result;
    }

    private static final String ComponentReferenceMapKey = "MSComMap";

    protected Map componentReferenceMap (AWComponent component)
    {
        Meta meta = MetaContext.currentContext(component).meta();
        return meta.identityCache();
    }

    protected AWBindable lookupElementReference(AWComponent component)
    {
        // we use the Property Map (identity) as our key
        Map properties = properties(component);
        return (AWBindable)componentReferenceMap(component).get(properties);
    }

    protected void storeElementReference(AWElement componentReference,
                                                            String componentName, AWComponent component)
    {
        Map properties = properties(component);
        componentReferenceMap(component).put(properties, componentReference);
    }

    /*
    protected int elementReferenceId(AWBindable componentReference, AWComponent component, boolean canCreate)
    {
        return pageScopedReferenceId(componentReference, component);
    }
*/

    protected int computeIndexForElement (AWBindable element, AWComponent component)
    {
        // for stability, our id is based on the contents of our property map
        int index = properties(component).hashCode() % AWElementIdPath.LevelMaxSize;
        if (index < 0) index *= -1;
        return index;
    }

    static class DynamicValueBinding extends AWBinding
    {
        private Context.DynamicPropertyValue _dynamicValue;

        public void init (String bindingName, Context.DynamicPropertyValue value)
        {
            this.init(bindingName);
            _dynamicValue = value;

        }

        public boolean isConstantValue ()
        {
            return false;
        }

        public boolean isSettableInComponent (Object object)
        {
            return false;
        }

        public void setValue (Object value, Object object)
        {
            Assert.that(false, "Can't set through a Dynamic Value");
        }

        public Object value (Object object)
        {
            Context context = MetaContext.currentContext((AWComponent)object);
            return _dynamicValue.evaluate(context);
        }

        protected String bindingDescription ()
        {
            return "DynamicValue:" + _dynamicValue.toString();
        }
    }

    static class ContextFieldPathBinding extends AWBinding
    {
        private FieldPath _fieldPath;

        public void init (String bindingName, FieldPath fieldPath)
        {
            this.init(bindingName);
            _fieldPath = fieldPath;

        }

        public boolean isConstantValue ()
        {
            return false;
        }

        public boolean isSettableInComponent (Object object)
        {
            return true;
        }

        public void setValue (Object value, Object object)
        {
            Context context = MetaContext.currentContext((AWComponent)object);
            _fieldPath.setFieldValue(context, value);
        }

        public Object value (Object object)
        {
            Context context = MetaContext.currentContext((AWComponent)object);
            return _fieldPath.getFieldValue(context);
        }

        protected String bindingDescription ()
        {
            return "FieldPath:" + _fieldPath.toString();
        }
    }
}
