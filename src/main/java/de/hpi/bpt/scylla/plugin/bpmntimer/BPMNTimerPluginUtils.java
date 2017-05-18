package de.hpi.bpt.scylla.plugin.bpmntimer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import desmoj.core.simulator.TimeSpan;

class BPMNTimerPluginUtils {

    static final String PLUGIN_NAME = "bpmntimerevent";

    /**
     * Returns TimeSpan object (duration and timeUnit) representing time until next event. Available for timer events.
     * 
     * @param processModel
     * @param nodeId
     * @return
     * @return
     */
    static TimeSpan getTimeSpanUntilNextEvent(ProcessModel processModel, int nodeId) {
        TimeSpan timeSpan = null;
        Map<EventDefinitionType, Map<String, String>> definitions = processModel.getEventDefinitions().get(nodeId);
        if (definitions != null) {
            if (definitions.get(EventDefinitionType.TIMER) != null) {
                Map<String, String> eventAttributes = definitions.get(EventDefinitionType.TIMER);
                // String timeDate = eventAttributes.get("timeDate");
                // if (timeDate != null) {
                // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                // Date date = formatter.parse(timeDate);
                // }
                // String timeCycle = eventAttributes.get("timeCycle");
                String timeDuration = eventAttributes.get("timeDuration");
                Duration jtDuration = Duration.parse(timeDuration);
                long duration = jtDuration.getSeconds();
                TimeUnit timeUnit = TimeUnit.SECONDS;
                timeSpan = new TimeSpan(duration, timeUnit);
            }
        }
        return timeSpan;
    }
}
