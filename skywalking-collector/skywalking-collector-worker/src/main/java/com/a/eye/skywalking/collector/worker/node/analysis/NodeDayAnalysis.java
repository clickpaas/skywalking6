package com.a.eye.skywalking.collector.worker.node.analysis;

import com.a.eye.skywalking.collector.actor.AbstractLocalAsyncWorkerProvider;
import com.a.eye.skywalking.collector.actor.ClusterWorkerContext;
import com.a.eye.skywalking.collector.actor.LocalWorkerContext;
import com.a.eye.skywalking.collector.actor.selector.RollingSelector;
import com.a.eye.skywalking.collector.actor.selector.WorkerSelector;
import com.a.eye.skywalking.collector.worker.WorkerConfig;
import com.a.eye.skywalking.collector.worker.node.persistence.NodeDayAgg;
import com.a.eye.skywalking.collector.worker.segment.SegmentPost;
import com.a.eye.skywalking.collector.worker.storage.RecordData;
import com.a.eye.skywalking.trace.TraceSegment;

/**
 * @author pengys5
 */
public class NodeDayAnalysis extends AbstractNodeAnalysis {

    public NodeDayAnalysis(com.a.eye.skywalking.collector.actor.Role role, ClusterWorkerContext clusterContext, LocalWorkerContext selfContext) {
        super(role, clusterContext, selfContext);
    }

    @Override
    public void analyse(Object message) throws Exception {
        if (message instanceof SegmentPost.SegmentWithTimeSlice) {
            SegmentPost.SegmentWithTimeSlice segmentWithTimeSlice = (SegmentPost.SegmentWithTimeSlice) message;
            TraceSegment segment = segmentWithTimeSlice.getTraceSegment();
            analyseSpans(segment, segmentWithTimeSlice.getDay());
        }
    }

    @Override
    protected void aggregation() throws Exception {
        RecordData oneRecord;
        while ((oneRecord = pushOne()) != null) {
            getClusterContext().lookup(NodeDayAgg.Role.INSTANCE).tell(oneRecord);
        }
    }

    public static class Factory extends AbstractLocalAsyncWorkerProvider<NodeDayAnalysis> {
        public static Factory INSTANCE = new Factory();

        @Override
        public Role role() {
            return Role.INSTANCE;
        }

        @Override
        public NodeDayAnalysis workerInstance(ClusterWorkerContext clusterContext) {
            return new NodeDayAnalysis(role(), clusterContext, new LocalWorkerContext());
        }

        @Override
        public int queueSize() {
            return WorkerConfig.Queue.Node.NodeDayAnalysis.Size;
        }
    }

    public enum Role implements com.a.eye.skywalking.collector.actor.Role {
        INSTANCE;

        @Override
        public String roleName() {
            return NodeDayAnalysis.class.getSimpleName();
        }

        @Override
        public WorkerSelector workerSelector() {
            return new RollingSelector();
        }
    }
}