/*
 * Copyright (c) 2021, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.js.nodes.temporal;

import java.util.List;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.access.IsObjectNode;
import com.oracle.truffle.js.nodes.cast.JSToStringNode;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRealm;
import com.oracle.truffle.js.runtime.builtins.temporal.CalendarMethodsRecord;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalDateTimeRecord;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalInstant;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainDate;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainDateObject;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainDateTime;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainDateTimeObject;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalZonedDateTime;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalZonedDateTimeObject;
import com.oracle.truffle.js.runtime.objects.JSDynamicObject;
import com.oracle.truffle.js.runtime.objects.Undefined;
import com.oracle.truffle.js.runtime.util.TemporalConstants;
import com.oracle.truffle.js.runtime.util.TemporalErrors;
import com.oracle.truffle.js.runtime.util.TemporalUtil;

/**
 * Implementation of ToTemporalDate() operation.
 */
public abstract class ToTemporalDateNode extends JavaScriptBaseNode {

    protected ToTemporalDateNode() {
    }

    public final JSTemporalPlainDateObject execute(Object value) {
        return execute(value, Undefined.instance);
    }

    public abstract JSTemporalPlainDateObject execute(Object value, JSDynamicObject options);

    @Specialization
    public JSTemporalPlainDateObject toTemporalDate(Object itemParam, JSDynamicObject options,
                    @Cached InlinedBranchProfile errorBranch,
                    @Cached InlinedConditionProfile isObjectProfile,
                    @Cached InlinedConditionProfile isPlainDateTimeProfile,
                    @Cached InlinedConditionProfile isZonedDateTimeProfile,
                    @Cached InlinedConditionProfile isPlainDateProfile,
                    @Cached IsObjectNode isObjectNode,
                    @Cached JSToStringNode toStringNode,
                    @Cached TemporalGetOptionNode getOptionNode,
                    @Cached GetTemporalCalendarSlotValueWithISODefaultNode getCalendarSlotValueWithISODefault,
                    @Cached("createDateFromFields()") CalendarMethodsRecordLookupNode lookupDateFromFields,
                    @Cached("createFields()") CalendarMethodsRecordLookupNode lookupFields,
                    @Cached TemporalCalendarFieldsNode calendarFieldsNode,
                    @Cached TemporalCalendarDateFromFieldsNode dateFromFieldsNode,
                    @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
        JSContext ctx = getLanguage().getJSContext();
        assert options != null;
        JSRealm realm = getRealm();
        if (isObjectProfile.profile(this, isObjectNode.executeBoolean(itemParam))) {
            JSDynamicObject item = (JSDynamicObject) itemParam;
            if (isPlainDateProfile.profile(this, JSTemporalPlainDate.isJSTemporalPlainDate(item))) {
                return (JSTemporalPlainDateObject) item;
            } else if (isZonedDateTimeProfile.profile(this, JSTemporalZonedDateTime.isJSTemporalZonedDateTime(item))) {
                TemporalUtil.toTemporalOverflow(options, getOptionNode);
                var zdt = (JSTemporalZonedDateTimeObject) item;
                var instant = JSTemporalInstant.create(ctx, realm, zdt.getNanoseconds());
                var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(zdt.getTimeZone());
                var plainDateTime = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(ctx, realm, timeZoneRec, instant, zdt.getCalendar());
                return JSTemporalPlainDate.create(ctx, realm, plainDateTime.getYear(), plainDateTime.getMonth(), plainDateTime.getDay(), plainDateTime.getCalendar(), this, errorBranch);
            } else if (isPlainDateTimeProfile.profile(this, JSTemporalPlainDateTime.isJSTemporalPlainDateTime(item))) {
                TemporalUtil.toTemporalOverflow(options, getOptionNode);
                var dt = (JSTemporalPlainDateTimeObject) item;
                return JSTemporalPlainDate.create(ctx, realm, dt.getYear(), dt.getMonth(), dt.getDay(), dt.getCalendar(), this, errorBranch);
            }
            Object calendar = getCalendarSlotValueWithISODefault.execute(item);
            Object dateFromFieldsMethod = lookupDateFromFields.execute(calendar);
            Object fieldsMethod = lookupFields.execute(calendar);
            CalendarMethodsRecord calendarRec = CalendarMethodsRecord.forDateFromFieldsAndFields(calendar, dateFromFieldsMethod, fieldsMethod);
            List<TruffleString> fieldNames = calendarFieldsNode.execute(calendarRec, TemporalUtil.listDMMCY);
            JSDynamicObject fields = TemporalUtil.prepareTemporalFields(ctx, item, fieldNames, TemporalUtil.listEmpty);
            return dateFromFieldsNode.execute(calendarRec, fields, options);
        }
        JSTemporalDateTimeRecord result = TemporalUtil.parseTemporalDateString(toStringNode.executeString(itemParam));
        assert TemporalUtil.isValidISODate(result.getYear(), result.getMonth(), result.getDay());
        TruffleString calendar = result.getCalendar();
        if (calendar == null) {
            calendar = TemporalConstants.ISO8601;
        } else if (!TemporalUtil.isBuiltinCalendar(calendar)) {
            errorBranch.enter(this);
            throw TemporalErrors.createRangeErrorCalendarNotSupported();
        }
        TemporalUtil.toTemporalOverflow(options, getOptionNode);
        return JSTemporalPlainDate.create(ctx, realm, result.getYear(), result.getMonth(), result.getDay(), calendar, this, errorBranch);
    }
}
