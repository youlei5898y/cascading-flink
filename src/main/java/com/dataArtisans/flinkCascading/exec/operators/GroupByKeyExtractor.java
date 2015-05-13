/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dataArtisans.flinkCascading.exec.operators;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.util.TupleBuilder;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;

public class GroupByKeyExtractor extends RichMapFunction<Tuple, Tuple3<Tuple, Tuple, Tuple>> {

	private Fields inputFields;
	private Fields groupingKeys;
	private Fields sortingKeys;

	private transient TupleBuilder groupKeyBuilder;
	private transient TupleBuilder sortKeyBuilder;
	private Tuple3<Tuple, Tuple, Tuple> outT;

	public GroupByKeyExtractor() {}

	public GroupByKeyExtractor(Fields inputFields, Fields groupingKeys, Fields sortingKeys) {
		this.inputFields = inputFields;
		this.groupingKeys = groupingKeys;
		this.sortingKeys = sortingKeys;
	}

	@Override
	public void open(Configuration parameters) throws Exception {

		this.groupKeyBuilder = getTupleBuilder(inputFields, groupingKeys);
		if(sortingKeys != null) {
			this.sortKeyBuilder = getTupleBuilder(inputFields, sortingKeys);
		}
		else {
			this.sortKeyBuilder = getNullTupleBuilder();
		}

		outT = new Tuple3<Tuple, Tuple, Tuple>();

	}

	@Override
	public Tuple3<Tuple, Tuple, Tuple> map(Tuple inT) throws Exception {

		outT.f0 = this.groupKeyBuilder.makeResult(inT, null);
		outT.f1 = this.sortKeyBuilder.makeResult(inT, null);
		outT.f2 = inT;

		return outT;
	}


	private TupleBuilder getTupleBuilder(final Fields inputFields, final Fields keyFields) {

		return new TupleBuilder() {

			@Override
			public Tuple makeResult(Tuple input, Tuple output) {
				return input.get( inputFields, keyFields );
			}
		};
	}

	private TupleBuilder getNullTupleBuilder() {
		return new TupleBuilder() {

			@Override
			public Tuple makeResult(Tuple input, Tuple output) {
				return Tuple.NULL;
			}
		};
	}
}
