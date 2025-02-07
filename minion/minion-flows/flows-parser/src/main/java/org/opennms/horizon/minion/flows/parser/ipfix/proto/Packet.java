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

package org.opennms.horizon.minion.flows.parser.ipfix.proto;

import static org.opennms.horizon.minion.flows.listeners.utils.BufferUtils.slice;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.opennms.horizon.minion.flows.parser.ie.values.UnsignedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import io.netty.buffer.ByteBuf;
import org.opennms.horizon.minion.flows.parser.InvalidPacketException;
import org.opennms.horizon.minion.flows.parser.MissingTemplateException;
import org.opennms.horizon.minion.flows.parser.ie.RecordProvider;
import org.opennms.horizon.minion.flows.parser.ie.Value;
import org.opennms.horizon.minion.flows.parser.session.Session;
import org.opennms.horizon.minion.flows.parser.session.Template;

public final class Packet implements Iterable<FlowSet<?>>, RecordProvider {
    private static final Logger LOG = LoggerFactory.getLogger(Packet.class);

    /*
     +----------------------------------------------------+
     | Message Header                                     |
     +----------------------------------------------------+
     | Set                                                |
     +----------------------------------------------------+
     | Set                                                |
     +----------------------------------------------------+
      ...
     +----------------------------------------------------+
     | Set                                                |
     +----------------------------------------------------+
    */

    public final Header header;

    public final List<TemplateSet> templateSets;
    public final List<OptionsTemplateSet> optionTemplateSets;
    public final List<DataSet> dataSets;

    public Packet(final Session session,
                  final Header header,
                  final ByteBuf buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        final List<TemplateSet> templateSets = new LinkedList();
        final List<OptionsTemplateSet> optionTemplateSets = new LinkedList();
        final List<DataSet> dataSets = new LinkedList();

        while (buffer.isReadable()) {
            final ByteBuf headerBuffer = slice(buffer, FlowSetHeader.SIZE);
            final FlowSetHeader setHeader = new FlowSetHeader(headerBuffer);

            final ByteBuf payloadBuffer = slice(buffer, setHeader.length - FlowSetHeader.SIZE);
            switch (setHeader.getType()) {
                case TEMPLATE_SET: {
                    final TemplateSet templateSet = new TemplateSet(this, setHeader, payloadBuffer);

                    for (final TemplateRecord record : templateSet) {
                        if (record.header.fieldCount == 0) {
                            // Empty template means revocation
                            if (record.header.templateId == FlowSetHeader.TEMPLATE_SET_ID) {
                                // Remove all templates
                                session.removeAllTemplate(this.header.observationDomainId, Template.Type.TEMPLATE);

                            } else if (record.header.fieldCount == 0) {
                                // Empty template means revocation
                                session.removeTemplate(this.header.observationDomainId, record.header.templateId);
                            }

                        } else {
                            session.addTemplate(this.header.observationDomainId,
                                    Template.builder(record.header.templateId, Template.Type.TEMPLATE)
                                            .withFields(record.fields)
                                            .build());
                        }
                    }

                    templateSets.add(templateSet);
                    break;
                }

                case OPTIONS_TEMPLATE_SET: {
                    final OptionsTemplateSet optionsTemplateSet = new OptionsTemplateSet(this, setHeader, payloadBuffer);

                    for (final OptionsTemplateRecord record : optionsTemplateSet) {
                        if (record.header.fieldCount == 0) {
                            // Empty template means revocation
                            if (record.header.templateId == FlowSetHeader.OPTIONS_TEMPLATE_SET_ID) {
                                // Remove all templates
                                session.removeAllTemplate(this.header.observationDomainId, Template.Type.OPTIONS_TEMPLATE);

                            } else if (record.header.fieldCount == 0) {
                                // Empty template means revocation
                                session.removeTemplate(this.header.observationDomainId, record.header.templateId);
                            }

                        } else {
                            session.addTemplate(this.header.observationDomainId,
                                    Template.builder(record.header.templateId, Template.Type.OPTIONS_TEMPLATE)
                                            .withScopes(record.scopes)
                                            .withFields(record.fields)
                                            .build());
                        }
                    }

                    optionTemplateSets.add(optionsTemplateSet);
                    break;
                }

                case DATA_SET: {
                    final Session.Resolver resolver = session.getResolver(header.observationDomainId);

                    final DataSet dataSet;
                    try {
                        dataSet = new DataSet(this, setHeader, resolver, payloadBuffer);
                    } catch (final MissingTemplateException ex) {
                        LOG.debug("Skipping data-set due to missing template: {}", ex.getMessage());
                        break;
                    }

                    if (dataSet.template.type == Template.Type.OPTIONS_TEMPLATE) {
                        for (final DataRecord record : dataSet) {
                            session.addOptions(this.header.observationDomainId, dataSet.template.id, record.scopes, record.fields);
                        }
                    } else {
                        dataSets.add(dataSet);
                    }

                    break;
                }

                default: {
                    throw new InvalidPacketException(buffer, "Invalid Set ID: %d", setHeader.setId);
                }
            }
        }

        this.templateSets = Collections.unmodifiableList(templateSets);
        this.optionTemplateSets = Collections.unmodifiableList(optionTemplateSets);
        this.dataSets = Collections.unmodifiableList(dataSets);
    }

    @Override
    public Iterator<FlowSet<?>> iterator() {
        return Iterators.concat(this.templateSets.iterator(),
                                this.optionTemplateSets.iterator(),
                                this.dataSets.iterator());
    }

    @Override
    public Stream<Iterable<Value<?>>> getRecords() {
        final int recordCount = this.dataSets.stream()
                .mapToInt(s -> s.records.size())
                .sum();

        return this.dataSets.stream()
                .flatMap(s -> s.records.stream())
                .map(r -> Iterables.concat(
                        ImmutableList.of(
                                new UnsignedValue("@recordCount", recordCount),
                                new UnsignedValue("@sequenceNumber", this.header.sequenceNumber),
                                new UnsignedValue("@exportTime", this.header.exportTime),
                                new UnsignedValue("@observationDomainId", this.header.observationDomainId)),
                        r.fields,
                        r.options
                ));
    }

    @Override
    public long getObservationDomainId() {
        return this.header.observationDomainId;
    }

    @Override
    public long getSequenceNumber() {
        return this.header.sequenceNumber;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("templateSets", this.templateSets)
                .add("optionTemplateSets", this.optionTemplateSets)
                .add("dataTemplateSets", this.dataSets)
                .toString();
    }
}
