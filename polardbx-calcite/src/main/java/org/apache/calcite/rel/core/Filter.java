/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.rel.core;

import com.google.common.base.Preconditions;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.prepare.CalcitePrepareImpl;
import org.apache.calcite.rel.RelInput;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.SingleRel;
import org.apache.calcite.rel.metadata.JaninoRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMdUtil;
import org.apache.calcite.rel.metadata.RelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexChecker;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexProgram;
import org.apache.calcite.rex.RexShuttle;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.util.Litmus;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Relational expression that iterates over its input
 * and returns elements for which <code>condition</code> evaluates to
 * <code>true</code>.
 *
 * <p>If the condition allows nulls, then a null value is treated the same as
 * false.</p>
 *
 * @see org.apache.calcite.rel.logical.LogicalFilter
 */
public abstract class Filter extends SingleRel {
  //~ Instance fields --------------------------------------------------------

  protected final RexNode condition;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a filter.
   *
   * @param cluster   Cluster that this relational expression belongs to
   * @param traits    the traits of this rel
   * @param child     input relational expression
   * @param condition boolean expression which determines whether a row is
   *                  allowed to pass
   */
  protected Filter(
      RelOptCluster cluster,
      RelTraitSet traits,
      RelNode child,
      RexNode condition) {
    super(cluster, traits, child);
//    Preconditions.checkNotNull(condition, "Condition can't be null!");
    assert condition != null;
//    assert RexUtil.isFlat(condition) : condition;
    this.condition = condition;
    // Too expensive for everyday use:
    assert !CalcitePrepareImpl.DEBUG || isValid(Litmus.THROW, null);
  }

  /**
   * Creates a Filter by parsing serialized output.
   */
  protected Filter(RelInput input) {
    this(input.getCluster(), input.getTraitSet(), input.getInput(),
        input.getExpression("condition"));
  }

  //~ Methods ----------------------------------------------------------------

  @Override public final RelNode copy(RelTraitSet traitSet,
      List<RelNode> inputs) {
    return copy(traitSet, sole(inputs), getCondition());
  }

  public abstract Filter copy(RelTraitSet traitSet, RelNode input,
      RexNode condition);

  @Override public List<RexNode> getChildExps() {
    return ImmutableList.of(condition);
  }

  public RelNode accept(RexShuttle shuttle) {
    RexNode condition = shuttle.apply(this.condition);
    if (this.condition == condition) {
      return this;
    }
    return copy(traitSet, getInput(), condition);
  }

  public RexNode getCondition() {
    return condition;
  }

  @Override public boolean isValid(Litmus litmus, Context context) {
    if (RexUtil.isNullabilityCast(getCluster().getTypeFactory(), condition)) {
      return litmus.fail("Cast for just nullability not allowed");
    }
    final RexChecker checker =
        new RexChecker(getInput().getRowType(), context, litmus);
    condition.accept(checker);
    if (checker.getFailureCount() > 0) {
      return litmus.fail(null);
    }
    return litmus.succeed();
  }

  @Override public RelOptCost computeSelfCost(RelOptPlanner planner,
      RelMetadataQuery mq) {
    double dRows = mq.getRowCount(this);
    double dCpu = mq.getRowCount(getInput());
    double dMemory = 0;
    double dIo = 0;
    double dNet = 0;
    return planner.getCostFactory().makeCost(dRows, dCpu, dMemory, dIo,dNet);
  }

  @Override public double estimateRowCount(RelMetadataQuery mq) {
    return RelMdUtil.estimateFilteredRows(getInput(), condition, mq);
  }

  @Deprecated // to be removed before 2.0
  public static double estimateFilteredRows(RelNode child, RexProgram program) {
    final RelMetadataQuery mq;
    RelMetadataProvider provider = child.getCluster().getMetadataProvider();
    // provider in cluster cannot be a JaninoRelMetadataProvider,
    // for it doesn't support RelMetadataProvider.apply method
    mq = RelMetadataQuery.instance(JaninoRelMetadataProvider.of(provider));
    return RelMdUtil.estimateFilteredRows(child, program, mq);
  }

  @Deprecated // to be removed before 2.0
  public static double estimateFilteredRows(RelNode child, RexNode condition) {
    final RelMetadataQuery mq;
    RelMetadataProvider provider = child.getCluster().getMetadataProvider();
    // provider in cluster cannot be a JaninoRelMetadataProvider,
    // for it doesn't support RelMetadataProvider.apply method
    mq = RelMetadataQuery.instance(JaninoRelMetadataProvider.of(provider));
    return RelMdUtil.estimateFilteredRows(child, condition, mq);
  }

  public RelWriter explainTerms(RelWriter pw) {
    return super.explainTerms(pw)
        .item("condition", condition);
  }

//  @API(since = "1.24", status = API.Status.INTERNAL)
  @EnsuresNonNullIf(expression = "#1", result = true)
  protected boolean deepEquals0(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Filter o = (Filter) obj;
    return traitSet.equals(o.traitSet)
        && input.deepEquals(o.input)
        && condition.equals(o.condition)
        && getRowType().equalsSansFieldNames(o.getRowType());
  }

//  @API(since = "1.24", status = API.Status.INTERNAL)
  protected int deepHashCode0() {
    return Objects.hash(traitSet, input.deepHashCode(), condition);
  }
}

// End Filter.java
