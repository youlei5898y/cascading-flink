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

import cascading.flow.planner.Scope;
import cascading.operation.Buffer;
import cascading.operation.ConcreteCall;
import cascading.pipe.Every;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.util.TupleBuilder;
import com.dataArtisans.flinkCascading.exec.FlinkArgumentsIterator;
import com.dataArtisans.flinkCascading.exec.FlinkCollector;
import com.dataArtisans.flinkCascading.exec.FlinkFlowProcess;
import org.apache.flink.api.common.functions.RichGroupReduceFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class BufferReducer extends RichGroupReduceFunction<Tuple3<Tuple,Tuple,Tuple>, Tuple> {

	private Every every;
	private Scope outgoingScope;
	private Scope incomingScope;

	private transient Buffer buffer;
	private transient TupleEntry argumentsEntry;
	private transient TupleBuilder argumentsBuilder;
	private transient TupleBuilder outgoingBuilder;
	private transient ConcreteCall call;
	private transient FlinkFlowProcess ffp;


	public BufferReducer(Every every, Scope incoming, Scope outgoing) {

		this.every = every;
		this.incomingScope = incoming;
		this.outgoingScope = outgoing;
	}

	@Override
	public void open(Configuration config) {

		this.ffp = new FlinkFlowProcess(this.getRuntimeContext());
		this.buffer = this.every.getBuffer();

		this.call = new ConcreteCall(outgoingScope.getArgumentsDeclarator(), outgoingScope.getOperationDeclaredFields());

		Fields argumentsSelector = outgoingScope.getArgumentsSelector();
		Fields remainderFields = outgoingScope.getRemainderPassThroughFields();
		Fields outgoingSelector = outgoingScope.getOutGroupingSelector();

		argumentsEntry = new TupleEntry(outgoingScope.getArgumentsDeclarator(), true);
		argumentsBuilder = TupleBuilderBuilder.createArgumentsBuilder(
				incomingScope.getIncomingBufferArgumentFields(), argumentsSelector);
		outgoingBuilder = TupleBuilderBuilder.createOutgoingBuilder(
				every, incomingScope.getIncomingBufferPassThroughFields(), argumentsSelector,
				remainderFields, outgoingScope.getOperationDeclaredFields(), outgoingSelector);

		call.setArguments(argumentsEntry);

		buffer.prepare(ffp, call);

	}

	@Override
	public void reduce(Iterable<Tuple3<Tuple, Tuple, Tuple>> vals, Collector<Tuple> collector) throws Exception {

		FlinkCollector flinkCollector = new FlinkCollector(collector, outgoingBuilder, outgoingScope.getOperationDeclaredFields());
		FlinkArgumentsIterator argIt = new FlinkArgumentsIterator(vals, this.argumentsEntry, this.argumentsBuilder);
		argIt.setTupleBuildingCollector(flinkCollector);

		call.setArgumentsIterator( argIt );

		call.setOutputCollector( flinkCollector );
		call.setGroup( new TupleEntry(argIt.getKey()) );

		buffer.operate( ffp, call );

	}


}
