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

    $Id: //ariba/platform/ui/aribaweb/ariba/ui/aribaweb/test/AnnotationUtil.java#1 $
*/

package ariba.ui.aribaweb.test;

import java.lang.annotation.Annotation;

public class AnnotationUtil
{
    static public String getAnnotationTypeList (Annotation annotation)
    {
        String typeList = null;
        if (TestLink.class.isAssignableFrom(annotation.annotationType())) {
            TestLink testLink = (TestLink)annotation;
            typeList = testLink.typeList();
        }
        return typeList;
    }

    static public String getAnnotationSuperType (Annotation annotation)
    {
        String superType = null;
        if (TestLink.class.isAssignableFrom(annotation.annotationType())) {
            TestLink testLink = (TestLink)annotation;
            superType = testLink.superType();
        }
        return superType;
    }

    static public String getAnnotationName  (Annotation annotation)
    {
        String name = null;
        if (TestLink.class.isAssignableFrom(annotation.annotationType())) {
            TestLink testLink = (TestLink)annotation;
            name = testLink.name();
        }
        return name;
    }
}
