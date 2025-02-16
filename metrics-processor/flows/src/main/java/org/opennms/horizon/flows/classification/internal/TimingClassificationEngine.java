/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.horizon.flows.classification.internal;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.opennms.horizon.flows.classification.ClassificationEngine;
import org.opennms.horizon.flows.classification.ClassificationRequest;
import org.opennms.horizon.flows.classification.persistence.api.Rule;

import java.util.List;
import java.util.Objects;

public class TimingClassificationEngine implements ClassificationEngine {

    private final ClassificationEngine delegate;
    private final Timer classifyTimer;
    private final Timer reloadTimer;
    private final Timer getInvalidRulesTimer;

    public TimingClassificationEngine(MetricRegistry metricRegistry, ClassificationEngine delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.classifyTimer = metricRegistry.timer("classify");
        this.reloadTimer = metricRegistry.timer("reload");
        this.getInvalidRulesTimer = metricRegistry.timer("getInvalidrules");
    }
    
    @Override
    public String classify(ClassificationRequest classificationRequest) {
        try (final Timer.Context ctx = classifyTimer.time()) {
            return delegate.classify(classificationRequest);
        }
    }

    @Override
    public void reload() throws InterruptedException {
        try (final Timer.Context ctx = reloadTimer.time()) {
            delegate.reload();
        }
    }

    @Override
    public List<Rule> getInvalidRules() {
        try (final Timer.Context ctx = getInvalidRulesTimer.time()) {
            return delegate.getInvalidRules();
        }
    }

    public void addClassificationRulesReloadedListener(final ClassificationRulesReloadedListener classificationRulesReloadedListener) {
        this.delegate.addClassificationRulesReloadedListener(classificationRulesReloadedListener);
    }

    public void removeClassificationRulesReloadedListener(final ClassificationRulesReloadedListener classificationRulesReloadedListener) {
        this.delegate.removeClassificationRulesReloadedListener(classificationRulesReloadedListener);
    }
}
