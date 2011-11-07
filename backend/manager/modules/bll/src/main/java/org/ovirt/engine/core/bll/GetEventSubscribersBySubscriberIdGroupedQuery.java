package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.queries.GetEventSubscribersBySubscriberIdParameters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetEventSubscribersBySubscriberIdGroupedQuery<P extends GetEventSubscribersBySubscriberIdParameters>
        extends QueriesCommandBase<P> {
    public GetEventSubscribersBySubscriberIdGroupedQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<event_subscriber> list = DbFacade
                .getInstance()
                .getEventDAO()
                .getAllForSubscriber(
                        getParameters().getSubscriberId());
        if (list.size() > 0) {
            java.util.HashMap<String, event_subscriber> dic = new java.util.HashMap<String, event_subscriber>();

            for (event_subscriber ev : list) {
                // event_subscriber foundEv = groupedList.FirstOrDefault(a =>
                // a.event_up_name == ev.event_up_name);
                if (dic.containsKey(ev.getevent_up_name())) {
                    dic.get(ev.getevent_up_name()).settag_name(
                            dic.get(ev.getevent_up_name()).gettag_name() + ", " + ev.gettag_name());
                } else {
                    dic.put(ev.getevent_up_name(), ev);
                }
            }

            java.util.ArrayList<event_subscriber> groupedList = new ArrayList<event_subscriber>(dic.values());
            for (event_subscriber event : groupedList) {
                event.settag_name(StringHelper.trim(event.gettag_name(), new char[] { ',', ' ' }));
            }
            getQueryReturnValue().setReturnValue(groupedList);
        } else {
            getQueryReturnValue().setReturnValue(list);
        }
    }
}
