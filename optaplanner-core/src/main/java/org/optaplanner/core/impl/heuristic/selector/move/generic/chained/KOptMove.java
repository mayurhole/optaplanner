/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.impl.domain.valuerange.descriptor.ValueRangeDescriptor;
import org.optaplanner.core.impl.domain.variable.anchor.AnchorVariableSupply;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class KOptMove extends AbstractMove {

    protected final GenuineVariableDescriptor variableDescriptor;
    protected final SingletonInverseVariableSupply inverseVariableSupply;
    protected final AnchorVariableSupply anchorVariableSupply;

    protected final Object entity;
    protected final Object[] values;

    public KOptMove(GenuineVariableDescriptor variableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply, AnchorVariableSupply anchorVariableSupply,
            Object entity, Object[] values) {
        this.variableDescriptor = variableDescriptor;
        this.inverseVariableSupply = inverseVariableSupply;
        this.anchorVariableSupply = anchorVariableSupply;
        this.entity = entity;
        this.values = values;
    }

    public Object getEntity() {
        return entity;
    }

    public Object[] getValues() {
        return values;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public int getK() {
        return 1 + values.length;
    }

    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        Object firstAnchor = anchorVariableSupply.getAnchor(entity);
        Object firstValue = variableDescriptor.getValue(entity);
        Object formerAnchor = firstAnchor;
        Object formerValue = firstValue;
        for (Object value : values) {
            Object anchor = variableDescriptor.isValuePotentialAnchor(value)
                    ? value : anchorVariableSupply.getAnchor(value);
            if (anchor == formerAnchor && compareValuesInSameChain(formerValue, value) >= 0) {
                return false;
            }
            formerAnchor = anchor;
            formerValue = value;
        }
        if (firstAnchor == formerAnchor && compareValuesInSameChain(formerValue, firstValue) >= 0) {
            return false;
        }
        return true;
    }

    protected int compareValuesInSameChain(Object a, Object b) {
        if (a == b) {
            return 0;
        }
        Object afterA = inverseVariableSupply.getInverseSingleton(a);
        while (afterA != null) {
            if (afterA == b) {
                return 1;
            }
            afterA = inverseVariableSupply.getInverseSingleton(afterA);
        }
        return -1;
    }

    public Move createUndoMove(ScoreDirector scoreDirector) {
        Object[] undoValues = new Object[values.length];
        undoValues[0] = variableDescriptor.getValue(entity);
        for (int i = 1; i < values.length; i++) {
            undoValues[i] = values[values.length - i];
        }
        return new KOptMove(variableDescriptor, inverseVariableSupply, anchorVariableSupply,
                entity, undoValues);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector scoreDirector) {
        Object firstValue = variableDescriptor.getValue(entity);
        Object formerEntity = entity;
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (formerEntity != null) {
                scoreDirector.changeVariableFacade(variableDescriptor, formerEntity, value);
            }
            formerEntity = inverseVariableSupply.getInverseSingleton(value);
        }
        if (formerEntity != null) {
            scoreDirector.changeVariableFacade(variableDescriptor, formerEntity, firstValue);
        }
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    public Collection<? extends Object> getPlanningEntities() {
        List<Object> allEntityList = new ArrayList<Object>(values.length + 1);
        allEntityList.add(entity);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            allEntityList.add(inverseVariableSupply.getInverseSingleton(value));
        }
        return allEntityList;
    }

    public Collection<? extends Object> getPlanningValues() {
        List<Object> allValueList = new ArrayList<Object>(values.length + 1);
        allValueList.add(variableDescriptor.getValue(entity));
        Collections.addAll(allValueList, values);
        return allValueList;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof KOptMove) {
            KOptMove other = (KOptMove) o;
            return new EqualsBuilder()
                    .append(entity, other.entity)
                    .append(values, other.values)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(entity)
                .append(values)
                .toHashCode();
    }

    public String toString() {
        Object leftValue = variableDescriptor.getValue(entity);
        StringBuilder builder = new StringBuilder(80 * values.length);
        builder.append(entity).append(" {").append(leftValue);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            Object oldEntity = inverseVariableSupply.getInverseSingleton(value);
            builder.append("} -kOpt-> ").append(oldEntity).append(" {").append(value);
        }
        builder.append("}");
        return builder.toString();
    }

}