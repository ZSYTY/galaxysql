/*
 * Copyright [2013-2021], Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.polardbx.executor.vectorized.convert;

import com.alibaba.polardbx.common.datatype.DecimalConverter;
import com.alibaba.polardbx.common.datatype.DecimalStructure;
import com.alibaba.polardbx.executor.chunk.DecimalBlock;
import com.alibaba.polardbx.executor.chunk.MutableChunk;
import com.alibaba.polardbx.executor.chunk.RandomAccessBlock;
import com.alibaba.polardbx.executor.vectorized.AbstractVectorizedExpression;
import com.alibaba.polardbx.executor.vectorized.EvaluationContext;
import com.alibaba.polardbx.executor.vectorized.VectorizedExpression;
import com.alibaba.polardbx.executor.vectorized.VectorizedExpressionUtils;
import com.alibaba.polardbx.executor.vectorized.metadata.ExpressionSignatures;
import com.alibaba.polardbx.optimizer.core.datatype.DataType;

import static com.alibaba.polardbx.common.datatype.DecimalTypeBase.DECIMAL_MEMORY_SIZE;
import static com.alibaba.polardbx.executor.vectorized.metadata.ArgumentKind.Variable;

@SuppressWarnings("unused")
@ExpressionSignatures(names = {"CastToDecimal", "ConvertToDecimal"}, argumentTypes = {"Decimal"},
    argumentKinds = {Variable})
public class CastDecimalToDecimalVectorizedExpression extends AbstractVectorizedExpression {

    public CastDecimalToDecimalVectorizedExpression(DataType<?> outputDataType, int outputIndex,
                                                    VectorizedExpression[] children) {
        super(outputDataType, outputIndex, children);
    }

    @Override
    public void eval(EvaluationContext ctx) {
        super.evalChildren(ctx);

        MutableChunk chunk = ctx.getPreAllocatedChunk();
        int batchSize = chunk.batchSize();
        boolean isSelectionInUse = chunk.isSelectionInUse();
        int[] sel = chunk.selection();

        RandomAccessBlock outputVectorSlot = chunk.slotIn(outputIndex, outputDataType);
        RandomAccessBlock inputVectorSlot = chunk.slotIn(children[0].getOutputIndex(), children[0].getOutputDataType());

        DecimalStructure tmpDecimal = new DecimalStructure();
        int precision = outputDataType.getPrecision();
        int scale = outputDataType.getScale();

        // handle nulls
        VectorizedExpressionUtils.mergeNulls(chunk, outputIndex, children[0].getOutputIndex());

        if (isSelectionInUse) {
            for (int i = 0; i < batchSize; i++) {
                int j = sel[i];

                int fromIndex = j * DECIMAL_MEMORY_SIZE;

                // The convert result will directly wrote to decimal memory segment
                DecimalStructure fromValue =
                    new DecimalStructure((inputVectorSlot.cast(DecimalBlock.class)).getRegion(j));
                DecimalStructure toValue =
                    new DecimalStructure((outputVectorSlot.cast(DecimalBlock.class)).getRegion(j));

                // do rescale operation
                DecimalConverter.rescale(fromValue, toValue, precision, scale, false);
            }
        } else {
            for (int i = 0; i < batchSize; i++) {
                int fromIndex = i * DECIMAL_MEMORY_SIZE;

                // The convert result will directly wrote to decimal memory segment
                DecimalStructure fromValue =
                    new DecimalStructure((inputVectorSlot.cast(DecimalBlock.class)).getRegion(i));
                DecimalStructure toValue =
                    new DecimalStructure((outputVectorSlot.cast(DecimalBlock.class)).getRegion(i));

                // do rescale operation
                DecimalConverter.rescale(fromValue, toValue, precision, scale, false);
            }
        }
        outputVectorSlot.cast(DecimalBlock.class).setFullState();
    }
}
