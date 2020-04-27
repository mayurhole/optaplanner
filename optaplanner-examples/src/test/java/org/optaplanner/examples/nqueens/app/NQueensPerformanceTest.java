/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.nqueens.app;

import java.util.stream.Stream;

import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.nqueens.domain.NQueens;

public class NQueensPerformanceTest extends SolverPerformanceTest<NQueens> {

    @Override
    protected NQueensApp createCommonApp() {
        return new NQueensApp();
    }

    @Override
    protected Stream<TestData> testData() {
        return Stream.of(
                testData("data/nqueens/unsolved/16queens.xml", "0", EnvironmentMode.REPRODUCIBLE),
                testData("data/nqueens/unsolved/8queens.xml", "0", EnvironmentMode.FAST_ASSERT),
                testData("data/nqueens/unsolved/4queens.xml", "0", EnvironmentMode.FULL_ASSERT)
        );
    }
}
