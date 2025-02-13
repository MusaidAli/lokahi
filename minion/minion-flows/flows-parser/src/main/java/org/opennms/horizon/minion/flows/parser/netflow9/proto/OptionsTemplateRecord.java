/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.minion.flows.parser.netflow9.proto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;
import org.opennms.horizon.minion.flows.parser.InvalidPacketException;

public final class OptionsTemplateRecord implements Record {

    public final OptionsTemplateSet set;  // Enclosing set

    public final OptionsTemplateRecordHeader header;

    public final List<ScopeFieldSpecifier> scopes;
    public final List<FieldSpecifier> fields;

    public OptionsTemplateRecord(final OptionsTemplateSet set,
                                 final OptionsTemplateRecordHeader header,
                                 final ByteBuf buffer) throws InvalidPacketException {
        this.set = Objects.requireNonNull(set);

        this.header = Objects.requireNonNull(header);

        final List<ScopeFieldSpecifier> scopeFields = new LinkedList<>();
        for (int i = 0; i < this.header.optionScopeLength; i += ScopeFieldSpecifier.SIZE) {
            final ScopeFieldSpecifier scopeField = new ScopeFieldSpecifier(buffer);

            // Ignore scope fields without a value so they will always match during scope resolution
            if (scopeField.fieldLength == 0) {
                continue;
            }

            scopeFields.add(scopeField);
        }

        final List<FieldSpecifier> fields = new LinkedList<>();
        for (int i = 0; i < this.header.optionLength; i += FieldSpecifier.SIZE) {
            final FieldSpecifier field = new FieldSpecifier(buffer);
            fields.add(field);
        }

        this.scopes = Collections.unmodifiableList(scopeFields);
        this.fields = Collections.unmodifiableList(fields);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("scopeFields", scopes)
                .add("fields", fields)
                .toString();
    }
}
