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
package com.oracle.truffle.js.builtins.temporal;

import static com.oracle.truffle.js.runtime.util.TemporalConstants.AUTO;
import static com.oracle.truffle.js.runtime.util.TemporalConstants.CALENDAR;
import static com.oracle.truffle.js.runtime.util.TemporalConstants.HALF_EXPAND;
import static com.oracle.truffle.js.runtime.util.TemporalConstants.NANOSECOND;
import static com.oracle.truffle.js.runtime.util.TemporalConstants.OFFSET;
import static com.oracle.truffle.js.runtime.util.TemporalConstants.PREFER;
import static com.oracle.truffle.js.runtime.util.TemporalConstants.TIME_ZONE;
import static com.oracle.truffle.js.runtime.util.TemporalConstants.TRUNC;
import static com.oracle.truffle.js.runtime.util.TemporalUtil.dtoi;
import static com.oracle.truffle.js.runtime.util.TemporalUtil.dtol;

import java.util.EnumSet;
import java.util.List;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.js.builtins.JSBuiltinsContainer;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeAddSubNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeCalendarGetterNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeEqualsNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeGetISOFieldsNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeGetterNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeRoundNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeStartOfDayNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeToInstantNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeToLocaleStringNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeToPlainDateNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeToPlainDateTimeNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeToPlainMonthDayNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeToPlainTimeNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeToPlainYearMonthNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeToStringNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeUntilSinceNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeValueOfNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeWithCalendarNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeWithNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeWithPlainDateNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeWithPlainTimeNodeGen;
import com.oracle.truffle.js.builtins.temporal.TemporalZonedDateTimePrototypeBuiltinsFactory.JSTemporalZonedDateTimeWithTimeZoneNodeGen;
import com.oracle.truffle.js.nodes.access.EnumerableOwnPropertyNamesNode;
import com.oracle.truffle.js.nodes.cast.JSNumberToBigIntNode;
import com.oracle.truffle.js.nodes.cast.JSToNumberNode;
import com.oracle.truffle.js.nodes.cast.JSToStringNode;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.nodes.function.JSBuiltinNode;
import com.oracle.truffle.js.nodes.temporal.CalendarMethodsRecordLookupNode;
import com.oracle.truffle.js.nodes.temporal.CreateTimeZoneMethodsRecordNode;
import com.oracle.truffle.js.nodes.temporal.TemporalCalendarDateFromFieldsNode;
import com.oracle.truffle.js.nodes.temporal.TemporalCalendarFieldsNode;
import com.oracle.truffle.js.nodes.temporal.TemporalCalendarGetterNode;
import com.oracle.truffle.js.nodes.temporal.TemporalDurationAddNode;
import com.oracle.truffle.js.nodes.temporal.TemporalGetOptionNode;
import com.oracle.truffle.js.nodes.temporal.TemporalMonthDayFromFieldsNode;
import com.oracle.truffle.js.nodes.temporal.TemporalRoundDurationNode;
import com.oracle.truffle.js.nodes.temporal.TemporalYearMonthFromFieldsNode;
import com.oracle.truffle.js.nodes.temporal.ToTemporalCalendarIdentifierNode;
import com.oracle.truffle.js.nodes.temporal.ToTemporalCalendarSlotValueNode;
import com.oracle.truffle.js.nodes.temporal.ToTemporalDateNode;
import com.oracle.truffle.js.nodes.temporal.ToTemporalDurationNode;
import com.oracle.truffle.js.nodes.temporal.ToTemporalTimeNode;
import com.oracle.truffle.js.nodes.temporal.ToTemporalTimeZoneNode;
import com.oracle.truffle.js.nodes.temporal.ToTemporalZonedDateTimeNode;
import com.oracle.truffle.js.runtime.BigInt;
import com.oracle.truffle.js.runtime.Boundaries;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRealm;
import com.oracle.truffle.js.runtime.JSRuntime;
import com.oracle.truffle.js.runtime.Strings;
import com.oracle.truffle.js.runtime.builtins.BuiltinEnum;
import com.oracle.truffle.js.runtime.builtins.JSOrdinary;
import com.oracle.truffle.js.runtime.builtins.temporal.CalendarMethodsRecord;
import com.oracle.truffle.js.runtime.builtins.temporal.ISODateRecord;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalDateTimeRecord;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalDuration;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalDurationObject;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalDurationRecord;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalInstant;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalInstantObject;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainDate;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainDateObject;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainDateTime;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainDateTimeObject;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainTime;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPlainTimeObject;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalPrecisionRecord;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalZonedDateTime;
import com.oracle.truffle.js.runtime.builtins.temporal.JSTemporalZonedDateTimeObject;
import com.oracle.truffle.js.runtime.builtins.temporal.TimeDurationRecord;
import com.oracle.truffle.js.runtime.objects.JSDynamicObject;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.js.runtime.objects.Undefined;
import com.oracle.truffle.js.runtime.util.TemporalConstants;
import com.oracle.truffle.js.runtime.util.TemporalErrors;
import com.oracle.truffle.js.runtime.util.TemporalUtil;
import com.oracle.truffle.js.runtime.util.TemporalUtil.Disambiguation;
import com.oracle.truffle.js.runtime.util.TemporalUtil.MatchBehaviour;
import com.oracle.truffle.js.runtime.util.TemporalUtil.OffsetBehaviour;
import com.oracle.truffle.js.runtime.util.TemporalUtil.OffsetOption;
import com.oracle.truffle.js.runtime.util.TemporalUtil.Overflow;
import com.oracle.truffle.js.runtime.util.TemporalUtil.RoundingMode;
import com.oracle.truffle.js.runtime.util.TemporalUtil.ShowCalendar;
import com.oracle.truffle.js.runtime.util.TemporalUtil.Unit;

public class TemporalZonedDateTimePrototypeBuiltins extends JSBuiltinsContainer.SwitchEnum<TemporalZonedDateTimePrototypeBuiltins.TemporalZonedDateTimePrototype> {

    public static final JSBuiltinsContainer BUILTINS = new TemporalZonedDateTimePrototypeBuiltins();

    protected TemporalZonedDateTimePrototypeBuiltins() {
        super(JSTemporalZonedDateTime.PROTOTYPE_NAME, TemporalZonedDateTimePrototype.class);
    }

    public enum TemporalZonedDateTimePrototype implements BuiltinEnum<TemporalZonedDateTimePrototype> {
        // getters
        calendarId(0),
        timeZone(0),
        year(0),
        month(0),
        monthCode(0),
        day(0),
        hour(0),
        minute(0),
        second(0),
        millisecond(0),
        microsecond(0),
        nanosecond(0),
        epochSeconds(0),
        epochMilliseconds(0),
        epochMicroseconds(0),
        epochNanoseconds(0),
        dayOfWeek(0),
        dayOfYear(0),
        weekOfYear(0),
        hoursInDay(0),
        daysInWeek(0),
        daysInMonth(0),
        daysInYear(0),
        monthsInYear(0),
        inLeapYear(0),
        offsetNanoseconds(0),
        offset(0),

        // methods
        with(1),
        withPlainTime(0),
        withPlainDate(1),
        withTimeZone(1),
        withCalendar(1),
        add(1),
        subtract(1),
        until(1),
        since(1),
        round(1),
        equals(1),
        toString(0),
        toLocaleString(0),
        toJSON(0),
        valueOf(0),
        startOfDay(0),
        toInstant(0),
        toPlainDate(0),
        toPlainTime(0),
        toPlainDateTime(0),
        toPlainYearMonth(0),
        toPlainMonthDay(0),
        getISOFields(0);

        private final int length;

        TemporalZonedDateTimePrototype(int length) {
            this.length = length;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public boolean isGetter() {
            return EnumSet.range(calendarId, offset).contains(this);
        }
    }

    @Override
    protected Object createNode(JSContext context, JSBuiltin builtin, boolean construct, boolean newTarget, TemporalZonedDateTimePrototype builtinEnum) {
        switch (builtinEnum) {
            case calendarId:
                return JSTemporalZonedDateTimeCalendarGetterNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
            case timeZone:
            case year:
            case month:
            case monthCode:
            case day:
            case hour:
            case minute:
            case second:
            case millisecond:
            case microsecond:
            case nanosecond:
            case epochSeconds:
            case epochMilliseconds:
            case epochMicroseconds:
            case epochNanoseconds:
            case dayOfWeek:
            case dayOfYear:
            case weekOfYear:
            case hoursInDay:
            case daysInWeek:
            case daysInMonth:
            case daysInYear:
            case monthsInYear:
            case inLeapYear:
            case offsetNanoseconds:
            case offset:
                return JSTemporalZonedDateTimeGetterNodeGen.create(context, builtin, builtinEnum, args().withThis().createArgumentNodes(context));

            case toString:
                return JSTemporalZonedDateTimeToStringNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case toLocaleString:
                return JSTemporalZonedDateTimeToLocaleStringNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case toJSON:
                return JSTemporalZonedDateTimeToLocaleStringNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
            case valueOf:
                return JSTemporalZonedDateTimeValueOfNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
            case with:
                return JSTemporalZonedDateTimeWithNodeGen.create(context, builtin, args().withThis().fixedArgs(2).createArgumentNodes(context));
            case withPlainTime:
                return JSTemporalZonedDateTimeWithPlainTimeNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case withPlainDate:
                return JSTemporalZonedDateTimeWithPlainDateNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case withTimeZone:
                return JSTemporalZonedDateTimeWithTimeZoneNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case withCalendar:
                return JSTemporalZonedDateTimeWithCalendarNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case add:
                return JSTemporalZonedDateTimeAddSubNodeGen.create(context, builtin, TemporalUtil.ADD, args().withThis().fixedArgs(2).createArgumentNodes(context));
            case subtract:
                return JSTemporalZonedDateTimeAddSubNodeGen.create(context, builtin, TemporalUtil.SUBTRACT, args().withThis().fixedArgs(2).createArgumentNodes(context));
            case until:
                return JSTemporalZonedDateTimeUntilSinceNodeGen.create(context, builtin, TemporalUtil.UNTIL, args().withThis().fixedArgs(2).createArgumentNodes(context));
            case since:
                return JSTemporalZonedDateTimeUntilSinceNodeGen.create(context, builtin, TemporalUtil.SINCE, args().withThis().fixedArgs(2).createArgumentNodes(context));
            case round:
                return JSTemporalZonedDateTimeRoundNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case equals:
                return JSTemporalZonedDateTimeEqualsNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case startOfDay:
                return JSTemporalZonedDateTimeStartOfDayNodeGen.create(context, builtin, args().withThis().fixedArgs(0).createArgumentNodes(context));
            case toInstant:
                return JSTemporalZonedDateTimeToInstantNodeGen.create(context, builtin, args().withThis().fixedArgs(0).createArgumentNodes(context));
            case toPlainDate:
                return JSTemporalZonedDateTimeToPlainDateNodeGen.create(context, builtin, args().withThis().fixedArgs(0).createArgumentNodes(context));
            case toPlainTime:
                return JSTemporalZonedDateTimeToPlainTimeNodeGen.create(context, builtin, args().withThis().fixedArgs(0).createArgumentNodes(context));
            case toPlainDateTime:
                return JSTemporalZonedDateTimeToPlainDateTimeNodeGen.create(context, builtin, args().withThis().fixedArgs(0).createArgumentNodes(context));
            case toPlainYearMonth:
                return JSTemporalZonedDateTimeToPlainYearMonthNodeGen.create(context, builtin, args().withThis().fixedArgs(0).createArgumentNodes(context));
            case toPlainMonthDay:
                return JSTemporalZonedDateTimeToPlainMonthDayNodeGen.create(context, builtin, args().withThis().fixedArgs(0).createArgumentNodes(context));
            case getISOFields:
                return JSTemporalZonedDateTimeGetISOFieldsNodeGen.create(context, builtin, args().withThis().fixedArgs(0).createArgumentNodes(context));
        }
        return null;
    }

    public abstract static class JSTemporalZonedDateTimeCalendarGetterNode extends JSBuiltinNode {

        protected JSTemporalZonedDateTimeCalendarGetterNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected TruffleString calendarId(JSTemporalZonedDateTimeObject zonedDateTime,
                        @Cached ToTemporalCalendarIdentifierNode toCalendarIdentifier) {
            return toCalendarIdentifier.executeString(zonedDateTime.getCalendar());
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(zonedDateTime)")
        static TruffleString invalidReceiver(@SuppressWarnings("unused") Object zonedDateTime) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeGetterNode extends JSBuiltinNode {

        protected final TemporalZonedDateTimePrototype property;

        protected JSTemporalZonedDateTimeGetterNode(JSContext context, JSBuiltin builtin, TemporalZonedDateTimePrototype property) {
            super(context, builtin);
            this.property = property;
        }

        @Specialization
        protected Object zonedDateTimeGetter(JSTemporalZonedDateTimeObject zdt,
                        @Cached TemporalCalendarGetterNode calendarGetterNode,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            switch (property) {
                case timeZone:
                    return zdt.getTimeZone();
                case year:
                case month:
                case monthCode:
                case day:
                case hour:
                case minute:
                case second:
                case millisecond:
                case microsecond:
                case nanosecond:
                case dayOfWeek:
                case dayOfYear:
                case weekOfYear:
                case daysInWeek:
                case daysInMonth:
                case daysInYear:
                case monthsInYear:
                case inLeapYear:
                    return getterCalendarDetails(zdt, calendarGetterNode, createTimeZoneMethodsRecord);

                case hoursInDay:
                    return getterHoursInDay(zdt, errorBranch, createTimeZoneMethodsRecord);
                case epochSeconds:
                    return getterEpoch(zdt, BigInt.valueOf(1_000_000_000L)).doubleValue();
                case epochMilliseconds:
                    return getterEpoch(zdt, BigInt.valueOf(1_000_000L)).doubleValue();
                case epochMicroseconds:
                    return getterEpoch(zdt, BigInt.valueOf(1000L));
                case epochNanoseconds:
                    return getterEpoch(zdt, BigInt.ONE);
                case offsetNanoseconds:
                    return getterOffsetNanoseconds(zdt, createTimeZoneMethodsRecord);
                case offset:
                    return getterOffset(zdt, createTimeZoneMethodsRecord);
            }
            throw Errors.shouldNotReachHere();
        }

        private Object getterOffset(JSTemporalZonedDateTimeObject zdt, CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), getRealm(), zdt.getNanoseconds());
            JSDynamicObject timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            return TemporalUtil.builtinTimeZoneGetOffsetStringFor(timeZoneRec, instant);
        }

        private Object getterOffsetNanoseconds(JSTemporalZonedDateTimeObject zdt, CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), getRealm(), zdt.getNanoseconds());
            JSDynamicObject timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            return TemporalUtil.getOffsetNanosecondsFor(timeZoneRec, instant);
        }

        private Object getterHoursInDay(JSTemporalZonedDateTimeObject zdt, InlinedBranchProfile errorBranch, CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            JSDynamicObject timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeFull(timeZone);
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zdt.getNanoseconds());
            JSDynamicObject isoCalendar = TemporalUtil.getISO8601Calendar(getContext(), realm);
            JSTemporalPlainDateTimeObject temporalDateTime = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, isoCalendar);
            int year = temporalDateTime.getYear();
            int month = temporalDateTime.getMonth();
            int day = temporalDateTime.getDay();
            JSTemporalPlainDateTimeObject today = JSTemporalPlainDateTime.create(getContext(), realm,
                            year, month, day, 0, 0, 0, 0, 0, 0, isoCalendar, this, errorBranch);
            ISODateRecord tomorrowFields = TemporalUtil.addISODate(year, month, day, 0, 0, 0, 1, Overflow.REJECT);
            JSTemporalPlainDateTimeObject tomorrow = JSTemporalPlainDateTime.create(getContext(), realm,
                            tomorrowFields.year(), tomorrowFields.month(), tomorrowFields.day(),
                            0, 0, 0, 0, 0, 0, isoCalendar, this, errorBranch);
            JSTemporalInstantObject todayInstant = TemporalUtil.builtinTimeZoneGetInstantFor(getContext(), realm, timeZoneRec, today, Disambiguation.COMPATIBLE);
            JSTemporalInstantObject tomorrowInstant = TemporalUtil.builtinTimeZoneGetInstantFor(getContext(), realm, timeZoneRec, tomorrow, Disambiguation.COMPATIBLE);
            BigInt diffNs = tomorrowInstant.getNanoseconds().subtract(todayInstant.getNanoseconds());
            return diffNs.divide(BigInt.valueOf(36_000_000_000_000L));
        }

        private static BigInt getterEpoch(JSTemporalZonedDateTimeObject zdt, BigInt factor) {
            BigInt ns = zdt.getNanoseconds();
            BigInt s = ns.divide(factor); // roundTowardsZero is a no-op
            return s;
        }

        private Object getterCalendarDetails(JSTemporalZonedDateTimeObject zdt, TemporalCalendarGetterNode calendarGetterNode, CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            JSDynamicObject timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zdt.getNanoseconds());
            Object calendar = zdt.getCalendar();
            JSTemporalPlainDateTimeObject tdt = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, calendar);
            switch (property) {
                case year:
                    return TemporalUtil.calendarYear(calendarGetterNode, calendar, tdt);
                case month:
                    return TemporalUtil.calendarMonth(calendarGetterNode, calendar, tdt);
                case monthCode:
                    return TemporalUtil.calendarMonthCode(calendarGetterNode, calendar, tdt);
                case day:
                    return TemporalUtil.calendarDay(calendarGetterNode, calendar, tdt);
                case hour:
                    return tdt.getHour();
                case minute:
                    return tdt.getMinute();
                case second:
                    return tdt.getSecond();
                case millisecond:
                    return tdt.getMillisecond();
                case microsecond:
                    return tdt.getMicrosecond();
                case nanosecond:
                    return tdt.getNanosecond();
                case dayOfWeek:
                    return TemporalUtil.calendarDayOfWeek(calendarGetterNode, calendar, tdt);
                case dayOfYear:
                    return TemporalUtil.calendarDayOfYear(calendarGetterNode, calendar, tdt);
                case weekOfYear:
                    return TemporalUtil.calendarWeekOfYear(calendarGetterNode, calendar, tdt);
                case daysInWeek:
                    return TemporalUtil.calendarWeekOfYear(calendarGetterNode, calendar, tdt);
                case daysInMonth:
                    return TemporalUtil.calendarDaysInMonth(calendarGetterNode, calendar, tdt);
                case daysInYear:
                    return TemporalUtil.calendarDaysInYear(calendarGetterNode, calendar, tdt);
                case monthsInYear:
                    return TemporalUtil.calendarMonthsInYear(calendarGetterNode, calendar, tdt);
                case inLeapYear:
                    return TemporalUtil.calendarInLeapYear(calendarGetterNode, calendar, tdt);
            }
            throw Errors.shouldNotReachHere();
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeToString extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeToString(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected TruffleString toString(JSTemporalZonedDateTimeObject zonedDateTime, Object optionsParam,
                        @Cached JSToStringNode toStringNode,
                        @Cached TruffleString.EqualNode equalNode,
                        @Cached TemporalGetOptionNode getOptionNode,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached InlinedConditionProfile optionUndefined) {
            JSDynamicObject options = getOptionsObject(optionsParam, this, errorBranch, optionUndefined);
            JSTemporalPrecisionRecord precision = TemporalUtil.toSecondsStringPrecision(options, toStringNode, getOptionNode, equalNode);
            RoundingMode roundingMode = toTemporalRoundingMode(options, TemporalConstants.TRUNC, equalNode, getOptionNode);
            ShowCalendar showCalendar = TemporalUtil.toShowCalendarOption(options, getOptionNode, equalNode);
            TruffleString showTimeZone = TemporalUtil.toShowTimeZoneNameOption(options, getOptionNode);
            TruffleString showOffset = TemporalUtil.toShowOffsetOption(options, getOptionNode);
            return TemporalUtil.temporalZonedDateTimeToString(getContext(), getRealm(), zonedDateTime, precision.getPrecision(), showCalendar, showTimeZone, showOffset, precision.getIncrement(),
                            precision.getUnit(), roundingMode);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object optionsParam) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeToLocaleString extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeToLocaleString(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected TruffleString toLocaleString(JSTemporalZonedDateTimeObject zonedDateTime) {
            return TemporalUtil.temporalZonedDateTimeToString(getContext(), getRealm(), zonedDateTime, AUTO, ShowCalendar.AUTO, AUTO, AUTO);
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeValueOf extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeValueOf(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object valueOf(@SuppressWarnings("unused") Object thisObj) {
            throw Errors.createTypeError("Not supported.");
        }
    }

    public abstract static class JSTemporalZonedDateTimeWith extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeWith(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object with(JSTemporalZonedDateTimeObject zonedDateTime, Object temporalZonedDateTimeLike, Object optionsParam,
                        @Cached("createDateFromFields()") CalendarMethodsRecordLookupNode lookupDateFromFields,
                        @Cached("createFields()") CalendarMethodsRecordLookupNode lookupFields,
                        @Cached("createMergeFields()") CalendarMethodsRecordLookupNode lookupMergeFields,
                        @Cached TemporalGetOptionNode getOptionNode,
                        @Cached TruffleString.EqualNode equalNode,
                        @Cached TemporalCalendarFieldsNode calendarFieldsNode,
                        @Cached TemporalCalendarDateFromFieldsNode dateFromFieldsNode,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached InlinedConditionProfile optionUndefined,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            if (!isObject(temporalZonedDateTimeLike)) {
                errorBranch.enter(this);
                throw Errors.createTypeError("object expected");
            }
            JSDynamicObject temporalZDTLike = TemporalUtil.toJSDynamicObject(temporalZonedDateTimeLike, this, errorBranch);
            TemporalUtil.rejectTemporalCalendarType(temporalZDTLike, this, errorBranch);
            Object calendarProperty = JSObject.get(temporalZDTLike, CALENDAR);
            if (calendarProperty != Undefined.instance) {
                errorBranch.enter(this);
                throw TemporalErrors.createTypeErrorUnexpectedCalendar();
            }
            Object timeZoneProperty = JSObject.get(temporalZDTLike, TIME_ZONE);
            if (timeZoneProperty != Undefined.instance) {
                errorBranch.enter(this);
                throw TemporalErrors.createTypeErrorUnexpectedTimeZone();
            }

            Object calendarSlotValue = zonedDateTime.getCalendar();
            Object dateFromFieldsMethod = lookupDateFromFields.execute(calendarSlotValue);
            Object fieldsMethod = lookupFields.execute(calendarSlotValue);
            Object mergeFieldsMethod = lookupMergeFields.execute(calendarSlotValue);
            CalendarMethodsRecord calendarRec = CalendarMethodsRecord.forDateFromFieldsAndFieldsAndMergeFields(calendarSlotValue, dateFromFieldsMethod, fieldsMethod, mergeFieldsMethod);

            JSDynamicObject timeZone = zonedDateTime.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeFull(timeZone);
            List<TruffleString> fieldNames = calendarFieldsNode.execute(calendarRec, TemporalUtil.listDHMMMMMNSY);
            fieldNames = Boundaries.listToEditable(fieldNames);
            Boundaries.listAdd(fieldNames, OFFSET);
            JSDynamicObject partialZonedDateTime = TemporalUtil.preparePartialTemporalFields(getContext(), temporalZDTLike, fieldNames);
            JSDynamicObject options = getOptionsObject(optionsParam, this, errorBranch, optionUndefined);
            Disambiguation disambiguation = TemporalUtil.toTemporalDisambiguation(options, getOptionNode, equalNode);
            OffsetOption offset = TemporalUtil.toTemporalOffset(options, PREFER, getOptionNode, equalNode);
            Boundaries.listAdd(fieldNames, TIME_ZONE);
            JSDynamicObject fields = TemporalUtil.prepareTemporalFields(getContext(), zonedDateTime, fieldNames, TemporalUtil.listTimeZoneOffset);
            fields = TemporalUtil.calendarMergeFields(getContext(), getRealm(), calendarRec, fields,
                            partialZonedDateTime, this, errorBranch);
            fields = TemporalUtil.prepareTemporalFields(getContext(), fields, fieldNames, TemporalUtil.listTimeZone);
            Object offsetString = JSObject.get(fields, OFFSET);
            JSTemporalDateTimeRecord dateTimeResult = TemporalUtil.interpretTemporalDateTimeFields(calendarRec, fields, options, getOptionNode, dateFromFieldsNode);
            long offsetNanoseconds = TemporalUtil.parseTimeZoneOffsetString((TruffleString) offsetString);
            JSRealm realm = getRealm();
            BigInt epochNanoseconds = TemporalUtil.interpretISODateTimeOffset(getContext(), realm, dateTimeResult.getYear(), dateTimeResult.getMonth(), dateTimeResult.getDay(),
                            dateTimeResult.getHour(), dateTimeResult.getMinute(), dateTimeResult.getSecond(),
                            dateTimeResult.getMillisecond(), dateTimeResult.getMicrosecond(), dateTimeResult.getNanosecond(), OffsetBehaviour.OPTION,
                            offsetNanoseconds, timeZoneRec, disambiguation, offset, MatchBehaviour.MATCH_EXACTLY);
            return JSTemporalZonedDateTime.create(getContext(), realm, epochNanoseconds, timeZone, calendarRec.receiver());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object temporalZonedDateTimeLike, Object optionsParam) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeWithPlainTime extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeWithPlainTime(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected JSDynamicObject withPlainTime(JSTemporalZonedDateTimeObject zonedDateTime, Object plainTimeLike,
                        @Cached ToTemporalTimeNode toTemporalTime,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            JSRealm realm = getRealm();
            JSTemporalPlainTimeObject plainTime;
            if (plainTimeLike == Undefined.instance) {
                plainTime = JSTemporalPlainTime.create(getContext(), realm, 0, 0, 0, 0, 0, 0, this, errorBranch);
            } else {
                plainTime = toTemporalTime.execute(plainTimeLike, null);
            }
            JSDynamicObject timeZone = zonedDateTime.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeFull(timeZone);
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zonedDateTime.getNanoseconds());
            Object calendar = zonedDateTime.getCalendar();
            JSTemporalPlainDateTimeObject plainDateTime = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, calendar);
            JSTemporalPlainDateTimeObject resultPlainDateTime = JSTemporalPlainDateTime.create(getContext(), realm, plainDateTime.getYear(), plainDateTime.getMonth(), plainDateTime.getDay(),
                            plainTime.getHour(), plainTime.getMinute(), plainTime.getSecond(), plainTime.getMillisecond(), plainTime.getMicrosecond(), plainTime.getNanosecond(), calendar,
                            this, errorBranch);
            instant = TemporalUtil.builtinTimeZoneGetInstantFor(getContext(), realm, timeZoneRec, resultPlainDateTime, Disambiguation.COMPATIBLE);
            return JSTemporalZonedDateTime.create(getContext(), realm, instant.getNanoseconds(), timeZone, calendar);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object plainTimeLike) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeWithPlainDate extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeWithPlainDate(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected JSDynamicObject withPlainDate(JSTemporalZonedDateTimeObject zonedDateTime, Object plainDateLike,
                        @Cached ToTemporalDateNode toTemporalDate,
                        @Cached ToTemporalCalendarIdentifierNode toCalendarIdentifier,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            JSTemporalPlainDateObject plainDate = toTemporalDate.execute(plainDateLike);
            JSDynamicObject timeZone = zonedDateTime.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeFull(timeZone);
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zonedDateTime.getNanoseconds());
            JSTemporalPlainDateTimeObject plainDateTime = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, zonedDateTime.getCalendar());
            Object calendar = TemporalUtil.consolidateCalendars(zonedDateTime.getCalendar(), plainDate.getCalendar(), toCalendarIdentifier);
            JSTemporalPlainDateTimeObject resultPlainDateTime = JSTemporalPlainDateTime.create(getContext(), realm, plainDate.getYear(), plainDate.getMonth(), plainDate.getDay(),
                            plainDateTime.getHour(), plainDateTime.getMinute(), plainDateTime.getSecond(), plainDateTime.getMillisecond(), plainDateTime.getMicrosecond(),
                            plainDateTime.getNanosecond(), calendar, this, errorBranch);
            instant = TemporalUtil.builtinTimeZoneGetInstantFor(getContext(), realm, timeZoneRec, resultPlainDateTime, Disambiguation.COMPATIBLE);
            return JSTemporalZonedDateTime.create(getContext(), realm, instant.getNanoseconds(), timeZone, calendar);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object plainDateLike) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeWithTimeZone extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeWithTimeZone(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected JSDynamicObject withTimeZone(JSTemporalZonedDateTimeObject zonedDateTime, Object timeZoneLike,
                        @Cached ToTemporalTimeZoneNode toTemporalTimeZone) {
            JSDynamicObject timeZone = toTemporalTimeZone.execute(timeZoneLike);
            return JSTemporalZonedDateTime.create(getContext(), getRealm(), zonedDateTime.getNanoseconds(), timeZone, zonedDateTime.getCalendar());
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object timeZoneLike) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeWithCalendar extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeWithCalendar(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected JSDynamicObject withCalendar(JSTemporalZonedDateTimeObject zonedDateTime, Object calendarLike,
                        @Cached ToTemporalCalendarSlotValueNode toCalendarSlotValue) {
            Object calendar = toCalendarSlotValue.execute(calendarLike);
            return JSTemporalZonedDateTime.create(getContext(), getRealm(), zonedDateTime.getNanoseconds(), zonedDateTime.getTimeZone(), calendar);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object calendarLike) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeAddSubNode extends JSTemporalBuiltinOperation {

        private final int sign;

        protected JSTemporalZonedDateTimeAddSubNode(JSContext context, JSBuiltin builtin, int sign) {
            super(context, builtin);
            this.sign = sign;
        }

        @Specialization
        protected JSTemporalZonedDateTimeObject addDurationToOrSubtractDurationFromZonedDateTime(
                        JSTemporalZonedDateTimeObject zonedDateTime, Object temporalDurationLike, Object optionsParam,
                        @Cached JSNumberToBigIntNode toBigInt,
                        @Cached ToTemporalDurationNode toTemporalDurationNode,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached InlinedConditionProfile optionUndefined,
                        @Cached("createDateAdd()") CalendarMethodsRecordLookupNode lookupDateAdd,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            JSTemporalDurationObject duration = toTemporalDurationNode.execute(temporalDurationLike);
            JSDynamicObject options = getOptionsObject(optionsParam, this, errorBranch, optionUndefined);
            JSDynamicObject timeZone = zonedDateTime.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeFull(timeZone);
            Object calendar = zonedDateTime.getCalendar();
            Object dateAddMethod = lookupDateAdd.execute(calendar);
            CalendarMethodsRecord calendarRec = CalendarMethodsRecord.forDateAdd(calendar, dateAddMethod);
            JSRealm realm = getRealm();
            BigInt epochNanoseconds = TemporalUtil.addZonedDateTime(getContext(), realm, zonedDateTime.getNanoseconds(), timeZoneRec, calendarRec, sign * dtol(duration.getYears()),
                            sign * dtol(duration.getMonths()),
                            sign * dtol(duration.getWeeks()), sign * dtol(duration.getDays()), sign * dtol(duration.getHours()), sign * dtol(duration.getMinutes()), sign * dtol(duration.getSeconds()),
                            sign * dtol(duration.getMilliseconds()),
                            sign * dtol(duration.getMicroseconds()), toBigInt.executeBigInt(sign * duration.getNanoseconds()).bigIntegerValue(),
                            null, options);
            return JSTemporalZonedDateTime.create(getContext(), realm, epochNanoseconds, timeZone, calendar);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object temporalDurationLike, Object optionsParam) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeUntilSinceNode extends JSTemporalBuiltinOperation {

        private final int sign;

        protected JSTemporalZonedDateTimeUntilSinceNode(JSContext context, JSBuiltin builtin, int sign) {
            super(context, builtin);
            this.sign = sign;
        }

        @SuppressWarnings("truffle-static-method")
        @Specialization
        protected Object differenceTemporalZonedDateTime(JSTemporalZonedDateTimeObject zonedDateTime, Object otherParam, Object optionsParam,
                        @Bind("this") Node node,
                        @Cached JSToNumberNode toNumber,
                        @Cached("createKeys(getContext())") EnumerableOwnPropertyNamesNode namesNode,
                        @Cached ToTemporalZonedDateTimeNode toTemporalZonedDateTime,
                        @Cached ToTemporalCalendarIdentifierNode toCalendarIdentifier,
                        @Cached JSToStringNode toStringNode,
                        @Cached TruffleString.EqualNode equalNode,
                        @Cached TemporalDurationAddNode durationAddNode,
                        @Cached TemporalRoundDurationNode roundDurationNode,
                        @Cached TemporalGetOptionNode getOptionNode,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord,
                        @Cached("createDateAdd()") CalendarMethodsRecordLookupNode lookupDateAdd,
                        @Cached("createDateUntil()") CalendarMethodsRecordLookupNode lookupDateUntil,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached InlinedConditionProfile optionUndefined) {
            JSTemporalZonedDateTimeObject other = toTemporalZonedDateTime.execute(otherParam, Undefined.instance);
            var calendar = zonedDateTime.getCalendar();
            if (!TemporalUtil.calendarEquals(calendar, other.getCalendar(), toCalendarIdentifier)) {
                errorBranch.enter(node);
                throw TemporalErrors.createRangeErrorIdenticalCalendarExpected();
            }
            JSDynamicObject options = getOptionsObject(optionsParam, node, errorBranch, optionUndefined);
            Unit smallestUnit = toSmallestTemporalUnit(options, TemporalUtil.listEmpty, NANOSECOND, equalNode, getOptionNode, node, errorBranch);
            Unit defaultLargestUnit = TemporalUtil.largerOfTwoTemporalUnits(Unit.HOUR, smallestUnit);
            Unit largestUnit = toLargestTemporalUnit(options, TemporalUtil.listEmpty, AUTO, defaultLargestUnit, equalNode, getOptionNode, node, errorBranch);
            TemporalUtil.validateTemporalUnitRange(largestUnit, smallestUnit);
            RoundingMode roundingMode = toTemporalRoundingMode(options, TRUNC, equalNode, getOptionNode);
            Double maximum = TemporalUtil.maximumTemporalDurationRoundingIncrement(smallestUnit);
            double roundingIncrement = TemporalUtil.toTemporalRoundingIncrement(options, maximum, false, isObjectNode, toNumber);
            JSRealm realm = getRealm();
            if (!(Unit.YEAR == largestUnit || Unit.MONTH == largestUnit || Unit.WEEK == largestUnit || Unit.DAY == largestUnit)) {
                TimeDurationRecord result = TemporalUtil.differenceInstant(zonedDateTime.getNanoseconds(), other.getNanoseconds(),
                                roundingIncrement, smallestUnit, largestUnit, roundingMode, roundDurationNode);
                return JSTemporalDuration.createTemporalDuration(getContext(), realm, 0, 0, 0, 0,
                                sign * result.hours(), sign * result.minutes(), sign * result.seconds(),
                                sign * result.milliseconds(), sign * result.microseconds(), sign * result.nanoseconds(), node, errorBranch);
            }
            var timeZone = zonedDateTime.getTimeZone();
            if (!TemporalUtil.timeZoneEquals(timeZone, other.getTimeZone(), toStringNode)) {
                errorBranch.enter(node);
                throw TemporalErrors.createRangeErrorIdenticalTimeZoneExpected();
            }
            if (zonedDateTime.getNanoseconds().compareTo(other.getNanoseconds()) == 0) {
                return JSTemporalDuration.createTemporalDuration(getContext(), realm, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, node, errorBranch);
            }

            var timeZoneRec = createTimeZoneMethodsRecord.executeFull(timeZone);
            Object dateAddMethod = lookupDateAdd.execute(calendar);
            Object dateUntilMethod = lookupDateUntil.execute(calendar);
            CalendarMethodsRecord calendarRec = CalendarMethodsRecord.forDateAddDateUntil(calendar, dateAddMethod, dateUntilMethod);

            var instant = JSTemporalInstant.create(getContext(), realm, zonedDateTime.getNanoseconds());
            var precalculatedPlainDateTime = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, options);
            var plainRelativeTo = JSTemporalPlainDate.create(getContext(), realm,
                            precalculatedPlainDateTime.getYear(), precalculatedPlainDateTime.getMonth(), precalculatedPlainDateTime.getDay(),
                            calendar, node, errorBranch);

            JSDynamicObject untilOptions = TemporalUtil.mergeLargestUnitOption(getContext(), namesNode, options, largestUnit);
            JSTemporalDurationRecord difference = TemporalUtil.differenceZonedDateTime(getContext(), realm, namesNode,
                            zonedDateTime.getNanoseconds(), other.getNanoseconds(), timeZoneRec, calendarRec, largestUnit,
                            precalculatedPlainDateTime, untilOptions);
            JSTemporalDurationRecord roundResult = roundDurationNode.execute(difference.getYears(), difference.getMonths(), difference.getWeeks(),
                            difference.getDays(),
                            difference.getHours(), difference.getMinutes(), difference.getSeconds(),
                            difference.getMilliseconds(), difference.getMicroseconds(), difference.getNanoseconds(),
                            (long) roundingIncrement, smallestUnit, roundingMode,
                            plainRelativeTo, zonedDateTime, calendarRec, timeZoneRec, precalculatedPlainDateTime);
            JSTemporalDurationRecord result = TemporalUtil.adjustRoundedDurationDays(getContext(), realm, durationAddNode, roundDurationNode,
                            roundResult.getYears(), roundResult.getMonths(), roundResult.getWeeks(), roundResult.getDays(),
                            roundResult.getHours(), roundResult.getMinutes(), roundResult.getSeconds(),
                            roundResult.getMilliseconds(), roundResult.getMicroseconds(), roundResult.getNanoseconds(), (long) roundingIncrement, smallestUnit, roundingMode,
                            zonedDateTime, calendarRec, timeZoneRec, precalculatedPlainDateTime);
            return JSTemporalDuration.createTemporalDuration(getContext(), realm,
                            sign * result.getYears(), sign * result.getMonths(), sign * result.getWeeks(), sign * result.getDays(),
                            sign * result.getHours(), sign * result.getMinutes(), sign * result.getSeconds(),
                            sign * result.getMilliseconds(), sign * result.getMicroseconds(), sign * result.getNanoseconds(), node, errorBranch);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object otherParam, Object optionsParam) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeRound extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeRound(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected JSDynamicObject round(JSTemporalZonedDateTimeObject zonedDateTime, Object roundToParam,
                        @Cached JSToNumberNode toNumber,
                        @Cached TruffleString.EqualNode equalNode,
                        @Cached TemporalGetOptionNode getOptionNode,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached InlinedConditionProfile optionUndefined,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            if (roundToParam == Undefined.instance) {
                errorBranch.enter(this);
                throw TemporalErrors.createTypeErrorOptionsUndefined();
            }
            JSDynamicObject roundTo;
            if (Strings.isTString(roundToParam)) {
                roundTo = JSOrdinary.createWithNullPrototype(getContext());
                JSRuntime.createDataPropertyOrThrow(roundTo, TemporalConstants.SMALLEST_UNIT, JSRuntime.toStringIsString(roundToParam));
            } else {
                roundTo = getOptionsObject(roundToParam, this, errorBranch, optionUndefined);
            }
            Unit smallestUnit = toSmallestTemporalUnit(roundTo, TemporalUtil.listYMW, null, equalNode, getOptionNode, this, errorBranch);
            if (smallestUnit == Unit.EMPTY) {
                errorBranch.enter(this);
                throw TemporalErrors.createRangeErrorSmallestUnitExpected();
            }
            RoundingMode roundingMode = toTemporalRoundingMode(roundTo, HALF_EXPAND, equalNode, getOptionNode);
            double roundingIncrement = TemporalUtil.toTemporalDateTimeRoundingIncrement(roundTo, smallestUnit, isObjectNode, toNumber);
            JSDynamicObject timeZone = zonedDateTime.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeFull(timeZone);
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zonedDateTime.getNanoseconds());
            Object calendar = zonedDateTime.getCalendar();
            long offsetNanoseconds = TemporalUtil.getOffsetNanosecondsFor(timeZoneRec, instant);
            JSTemporalPlainDateTimeObject tdt = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, instant, calendar, offsetNanoseconds);
            Object isoCalendar = TemporalUtil.getISO8601Calendar(getContext(), realm);
            JSTemporalPlainDateTimeObject dtStart = JSTemporalPlainDateTime.create(getContext(), realm,
                            tdt.getYear(), tdt.getMonth(), tdt.getDay(), 0, 0, 0, 0, 0, 0, isoCalendar, this, errorBranch);
            JSTemporalInstantObject instantStart = TemporalUtil.builtinTimeZoneGetInstantFor(getContext(), realm, timeZoneRec, dtStart, Disambiguation.COMPATIBLE);
            BigInt startNs = instantStart.getNanoseconds();
            BigInt endNs = TemporalUtil.addDaysToZonedDateTime(getContext(), realm, instantStart, dtStart, timeZoneRec, 1).epochNanoseconds();
            BigInt dayLengthNs = endNs.subtract(startNs);
            if (dayLengthNs.compareValueTo(0) == 0) {
                errorBranch.enter(this);
                throw Errors.createRangeError("day length of zero now allowed");
            }
            JSTemporalDurationRecord roundResult = TemporalUtil.roundISODateTime(tdt.getYear(), tdt.getMonth(), tdt.getDay(), tdt.getHour(), tdt.getMinute(), tdt.getSecond(), tdt.getMillisecond(),
                            tdt.getMicrosecond(), tdt.getNanosecond(), roundingIncrement, smallestUnit, roundingMode, TemporalUtil.bigIntToLong(dayLengthNs));
            BigInt epochNanoseconds = TemporalUtil.interpretISODateTimeOffset(getContext(), realm, dtoi(roundResult.getYears()), dtoi(roundResult.getMonths()), dtoi(roundResult.getDays()),
                            dtoi(roundResult.getHours()), dtoi(roundResult.getMinutes()), dtoi(roundResult.getSeconds()), dtoi(roundResult.getMilliseconds()), dtoi(roundResult.getMicroseconds()),
                            dtoi(roundResult.getNanoseconds()), OffsetBehaviour.OPTION, offsetNanoseconds, timeZoneRec, Disambiguation.COMPATIBLE, OffsetOption.PREFER, MatchBehaviour.MATCH_EXACTLY);
            return JSTemporalZonedDateTime.create(getContext(), realm, epochNanoseconds, timeZone, calendar);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(Object thisObj, Object roundToParam) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeEquals extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeEquals(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected boolean equals(JSTemporalZonedDateTimeObject thisZonedDateTime, Object otherParam,
                        @Cached ToTemporalZonedDateTimeNode toTemporalZonedDateTime,
                        @Cached JSToStringNode toStringNode,
                        @Cached ToTemporalCalendarIdentifierNode toCalendarIdentifier) {
            JSTemporalZonedDateTimeObject otherZonedDateTime = toTemporalZonedDateTime.execute(otherParam, Undefined.instance);
            if (!thisZonedDateTime.getNanoseconds().equals(otherZonedDateTime.getNanoseconds())) {
                return false;
            }
            if (!TemporalUtil.timeZoneEquals(thisZonedDateTime.getTimeZone(), otherZonedDateTime.getTimeZone(), toStringNode)) {
                return false;
            }
            return TemporalUtil.calendarEquals(thisZonedDateTime.getCalendar(), otherZonedDateTime.getCalendar(), toCalendarIdentifier);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static boolean invalidReceiver(Object thisObj, Object otherParam) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeStartOfDay extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeStartOfDay(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object startOfDay(JSTemporalZonedDateTimeObject zonedDateTime,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            var timeZone = zonedDateTime.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeFull(timeZone);
            Object calendar = zonedDateTime.getCalendar();
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zonedDateTime.getNanoseconds());
            JSTemporalPlainDateTimeObject dt = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, calendar);
            JSTemporalPlainDateTimeObject startDateTime = JSTemporalPlainDateTime.create(getContext(), realm,
                            dt.getYear(), dt.getMonth(), dt.getDay(), 0, 0, 0, 0, 0, 0, calendar, this, errorBranch);
            JSTemporalInstantObject startInstant = TemporalUtil.builtinTimeZoneGetInstantFor(getContext(), realm, timeZoneRec, startDateTime, Disambiguation.COMPATIBLE);
            return JSTemporalZonedDateTime.create(getContext(), realm, startInstant.getNanoseconds(), timeZone, calendar);
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeToInstant extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeToInstant(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object toInstant(JSTemporalZonedDateTimeObject zonedDateTime) {
            return JSTemporalInstant.create(getContext(), getRealm(), zonedDateTime.getNanoseconds());
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeToPlainDate extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeToPlainDate(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object toPlainDate(JSTemporalZonedDateTimeObject zdt,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            var timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), getRealm(), zdt.getNanoseconds());
            Object calendar = zdt.getCalendar();
            JSTemporalPlainDateTimeObject dt = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), getRealm(), timeZoneRec, instant, calendar);
            return JSTemporalPlainDate.create(getContext(), getRealm(), dt.getYear(), dt.getMonth(), dt.getDay(), calendar, this, errorBranch);
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeToPlainTime extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeToPlainTime(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object toPlainTime(JSTemporalZonedDateTimeObject zdt,
                        @Cached InlinedBranchProfile errorBranch,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            var timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zdt.getNanoseconds());
            Object calendar = zdt.getCalendar();
            JSTemporalPlainDateTimeObject dt = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, calendar);
            return JSTemporalPlainTime.create(getContext(), realm, dt.getHour(), dt.getMinute(), dt.getSecond(), dt.getMillisecond(), dt.getMicrosecond(), dt.getNanosecond(), this, errorBranch);
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeToPlainDateTime extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeToPlainDateTime(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object toPlainDateTime(JSTemporalZonedDateTimeObject zdt,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            var timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zdt.getNanoseconds());
            Object calendar = zdt.getCalendar();
            return TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, calendar);
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeToPlainYearMonth extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeToPlainYearMonth(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object toPlainYearMonth(JSTemporalZonedDateTimeObject zdt,
                        @Cached("createFields()") CalendarMethodsRecordLookupNode lookupFields,
                        @Cached("createYearMonthFromFields()") CalendarMethodsRecordLookupNode lookupYearMonthFromFields,
                        @Cached TemporalYearMonthFromFieldsNode yearMonthFromFieldsNode,
                        @Cached TemporalCalendarFieldsNode calendarFieldsNode,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            Object calendar = zdt.getCalendar();
            Object fieldsMethod = lookupFields.execute(calendar);
            Object yearMonthFromFieldsMethod = lookupYearMonthFromFields.execute(calendar);
            CalendarMethodsRecord calendarRec = CalendarMethodsRecord.forFieldsAndYearMonthFromFields(calendar, fieldsMethod, yearMonthFromFieldsMethod);

            var timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zdt.getNanoseconds());
            JSTemporalPlainDateTimeObject temporalDateTime = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, calendar);
            List<TruffleString> fieldNames = calendarFieldsNode.execute(calendarRec, TemporalUtil.listMCY);
            JSDynamicObject fields = TemporalUtil.prepareTemporalFields(getContext(), temporalDateTime, fieldNames, TemporalUtil.listEmpty);
            return yearMonthFromFieldsNode.execute(calendarRec, fields, Undefined.instance);
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeToPlainMonthDay extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeToPlainMonthDay(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object toPlainMonthDay(JSTemporalZonedDateTimeObject zonedDateTime,
                        @Cached("createFields()") CalendarMethodsRecordLookupNode lookupFields,
                        @Cached("createMonthDayFromFields()") CalendarMethodsRecordLookupNode lookupMonthDayFromFields,
                        @Cached TemporalMonthDayFromFieldsNode monthDayFromFieldsNode,
                        @Cached TemporalCalendarFieldsNode calendarFieldsNode,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            Object calendar = zonedDateTime.getCalendar();
            Object fieldsMethod = lookupFields.execute(calendar);
            Object monthDayFromFieldsMethod = lookupMonthDayFromFields.execute(calendar);
            CalendarMethodsRecord calendarRec = CalendarMethodsRecord.forFieldsAndMonthDayFromFields(calendar, fieldsMethod, monthDayFromFieldsMethod);

            var timeZone = zonedDateTime.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            JSRealm realm = getRealm();
            JSTemporalInstantObject instant = JSTemporalInstant.create(getContext(), realm, zonedDateTime.getNanoseconds());
            JSTemporalPlainDateTimeObject temporalDateTime = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, calendar);
            List<TruffleString> fieldNames = calendarFieldsNode.execute(calendarRec, TemporalUtil.listDMC);
            JSDynamicObject fields = TemporalUtil.prepareTemporalFields(getContext(), temporalDateTime, fieldNames, TemporalUtil.listEmpty);
            return monthDayFromFieldsNode.execute(calendarRec, fields, Undefined.instance);
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static Object invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

    public abstract static class JSTemporalZonedDateTimeGetISOFields extends JSTemporalBuiltinOperation {

        protected JSTemporalZonedDateTimeGetISOFields(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected JSObject getISOFields(JSTemporalZonedDateTimeObject zdt,
                        @Cached CreateTimeZoneMethodsRecordNode createTimeZoneMethodsRecord) {
            JSRealm realm = getRealm();
            JSObject obj = JSOrdinary.create(getContext(), realm);
            var instant = JSTemporalInstant.create(getContext(), realm, zdt.getNanoseconds());
            var calendar = zdt.getCalendar();
            var timeZone = zdt.getTimeZone();
            var timeZoneRec = createTimeZoneMethodsRecord.executeOnlyGetOffsetNanosecondsFor(timeZone);
            JSTemporalPlainDateTimeObject dt = TemporalUtil.builtinTimeZoneGetPlainDateTimeFor(getContext(), realm, timeZoneRec, instant, calendar);
            TruffleString offset = TemporalUtil.builtinTimeZoneGetOffsetStringFor(timeZoneRec, instant);

            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, CALENDAR, calendar);
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_DAY, dt.getDay());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_HOUR, dt.getHour());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_MICROSECOND, dt.getMicrosecond());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_MILLISECOND, dt.getMillisecond());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_MINUTE, dt.getMinute());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_MONTH, dt.getMonth());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_NANOSECOND, dt.getNanosecond());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_SECOND, dt.getSecond());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.ISO_YEAR, dt.getYear());
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.OFFSET, offset);
            TemporalUtil.createDataPropertyOrThrow(getContext(), obj, TemporalConstants.TIME_ZONE, timeZone);
            return obj;
        }

        @Specialization(guards = "!isJSTemporalZonedDateTime(thisObj)")
        static JSObject invalidReceiver(@SuppressWarnings("unused") Object thisObj) {
            throw TemporalErrors.createTypeErrorTemporalZonedDateTimeExpected();
        }
    }

}
