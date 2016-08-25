package icd2.model;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModelModifier<T extends ModelObject<?, ?>, V> implements ModelModifier<T, V> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractModelModifier.class);

	public AbstractModelModifier() {
		super();
	}

	@Override
	public void notify(IEventBroker eventBroker, String[] notifications, T obj) {
		for (String n : notifications) {
			eventBroker.send(n, obj);
			logger.info("Sending notification about {} to {}", n, obj);
		}
	}

}