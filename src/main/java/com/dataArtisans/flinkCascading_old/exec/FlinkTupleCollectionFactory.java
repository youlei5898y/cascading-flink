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

package com.dataArtisans.flinkCascading_old.exec;

import cascading.flow.FlowProcess;
import cascading.tuple.Tuple;
import cascading.tuple.collect.SpillableTupleList;
import cascading.tuple.collect.TupleCollectionFactory;
import cascading.tuple.hadoop.TupleSerialization;
import cascading.tuple.hadoop.collect.HadoopSpillableTupleList;
import java.util.Collection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;

public class FlinkTupleCollectionFactory implements TupleCollectionFactory<Configuration> {
	private int spillThreshold;
	private CompressionCodec codec;
	private TupleSerialization tupleSerialization;

	public FlinkTupleCollectionFactory() {
	}

	public void initialize(FlowProcess<? extends Configuration> flowProcess) {
		this.spillThreshold = SpillableTupleList.getThreshold(flowProcess, 10000);
		this.codec = new GzipCodec();
		this.tupleSerialization = new TupleSerialization(flowProcess);
	}

	public Collection<Tuple> create(FlowProcess<? extends Configuration> flowProcess) {

		// TODO: Replace by a FlinkSplillableTupleList ?

		return new HadoopSpillableTupleList(this.spillThreshold, this.tupleSerialization, this.codec);
	}
}
