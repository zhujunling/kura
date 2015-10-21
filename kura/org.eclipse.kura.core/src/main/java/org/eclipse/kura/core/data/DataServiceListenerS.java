package org.eclipse.kura.core.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kura.data.DataServiceListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DataServiceListenerS implements ServiceListener, DataServiceListener {

	private static final Logger s_logger = LoggerFactory.getLogger(DataServiceListenerS.class);
	
	private BundleContext m_ctx;
	private Map<ServiceReference<DataServiceListener>, DataServiceListener> m_serviceReferences;
	private List<DataServiceListener> m_listeners;
	private Object m_lock;
	
	public DataServiceListenerS(BundleContext ctx) {
		m_ctx = ctx;
		m_serviceReferences = new HashMap<ServiceReference<DataServiceListener>, DataServiceListener>();
		m_listeners = new ArrayList<DataServiceListener>();
		m_lock = new Object();
	}
	
	public void start() {
		try {
			m_ctx.addServiceListener(this, "(objectClass="+DataServiceListener.class.getName()+")");
		} catch (InvalidSyntaxException e) {
			// should never happen
			s_logger.error("Failed to addServiceListener", e);
		}
		// get the initial service references
		Collection<ServiceReference<DataServiceListener>> srS = null;
		try {
			srS = m_ctx.getServiceReferences(DataServiceListener.class, null);
		} catch (InvalidSyntaxException e) {
			// should never happen
			s_logger.error("Failed to get ServiceReferenceS", e);
		}
		if (srS != null) {
			synchronized (m_lock) {
				for (ServiceReference<DataServiceListener> sr : srS) {
					m_serviceReferences.put(sr, m_ctx.getService(sr));
					s_logger.info("Add ServiceReference {}", sr.getProperty("component.name"));
				}
				s_logger.info("There are {} ServiceReferenceS", m_serviceReferences.size());
			}
		}
	}
	
	public void stop() {
		m_ctx.removeServiceListener(this);
		synchronized (m_lock) {
			for (ServiceReference<DataServiceListener> sr : m_serviceReferences.keySet()) {
				m_ctx.ungetService(sr);
				s_logger.info("Unget ServiceReference {}", sr);
			}			
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void serviceChanged(ServiceEvent arg0) {
		ServiceReference<?> sr = arg0.getServiceReference();
		switch (arg0.getType()) {
		case ServiceEvent.REGISTERED:
			s_logger.info("REGISTERED ServiceReference {}", sr.getProperty("component.name"));
			synchronized (m_lock) {
				m_serviceReferences.put((ServiceReference<DataServiceListener>) sr,
						(DataServiceListener) m_ctx.getService(sr));
				s_logger.info("There are {} ServiceReferenceS", m_serviceReferences.size());
			}
			break;
		case ServiceEvent.UNREGISTERING:
			s_logger.info("UNREGISTERING ServiceReference {}", sr.getProperty("component.name"));
			synchronized (m_lock) {
				m_serviceReferences.remove(sr);
				s_logger.info("There are {} ServiceReferenceS", m_serviceReferences.size());
			}
			break;
		default:
			// Ignore
			break;
		}
	}
	
	@Override
	public void onConnectionEstablished() {
		DataServiceListener[] listeners = getDataServiceListenerS();
		if (listeners != null) {
			for (DataServiceListener listener : listeners) {
				try {
					listener.onConnectionEstablished();
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.warn("No registered listeners. Ignoring onConnectionEstablished");
		}
	}

	@Override
	public void onDisconnecting() {
		DataServiceListener[] listeners = getDataServiceListenerS();
		if (listeners != null) {
			for (DataServiceListener listener : listeners) {
				try {
					listener.onDisconnecting();
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.warn("No registered listeners. Ignoring onDisconnecting");
		}
	}

	@Override
	public void onDisconnected() {
		DataServiceListener[] listeners = getDataServiceListenerS();
		if (listeners != null) {
			for (DataServiceListener listener : listeners) {
				try {
					listener.onDisconnected();
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.warn("No registered listeners. Ignoring onDisconnected");
		}
	}

	@Override
	public void onConnectionLost(Throwable cause) {
		DataServiceListener[] listeners = getDataServiceListenerS();
		if (listeners != null) {
			for (DataServiceListener listener : listeners) {
				try {
					listener.onConnectionLost(cause);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.warn("No registered listeners. Ignoring onConnectionLost");
		}
	}

	@Override
	public void onMessageArrived(String topic, byte[] payload, int qos,
			boolean retained) {
		DataServiceListener[] listeners = getDataServiceListenerS();
		if (listeners != null) {
			for (DataServiceListener listener : listeners) {
				try {
					listener.onMessageArrived(topic, payload, qos, retained);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.warn("No registered listeners. Ignoring onMessageArrived");
		}
	}

	@Override
	public void onMessagePublished(int messageId, String topic) {
		DataServiceListener[] listeners = getDataServiceListenerS();
		if (listeners != null) {
			for (DataServiceListener listener : listeners) {
				try {
					listener.onMessagePublished(messageId, topic);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.warn("No registered listeners. Ignoring onMessagePublished");
		}
	}

	@Override
	public void onMessageConfirmed(int messageId, String topic) {
		DataServiceListener[] listeners = getDataServiceListenerS();
		if (listeners != null) {
			for (DataServiceListener listener : listeners) {
				try {
					listener.onMessageConfirmed(messageId, topic);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} 
		else {
			s_logger.warn("No registered listeners. Ignoring onMessageConfirmed");
		}
	}
	
	public void add(DataServiceListener listener) {
		synchronized (m_lock) {
			m_listeners.add(listener);
		}
	}

	public void remove(DataServiceListener listener) {
		synchronized (m_lock) {
			m_listeners.remove(listener);
		}
	}
	
	private DataServiceListener[] getDataServiceListenerS() {
		DataServiceListener[] result = null;
		synchronized (m_lock) {
			result = new DataServiceListener[m_listeners.size() +
			                                   m_serviceReferences.size()];
			
			DataServiceListener[] listeners = m_listeners.toArray(new DataServiceListener[0]);
			if (listeners != null) {
				System.arraycopy(listeners, 0, result, 0, listeners.length);
			}
			
			Collection<DataServiceListener> services = m_serviceReferences.values();
			if (services != null) {
				DataServiceListener[] serviceListeners = services.toArray(new DataServiceListener[0]);
				if (serviceListeners != null) {
					System.arraycopy(serviceListeners, 0, 
							         result, listeners.length, serviceListeners.length);
				}
			}
		}
		return result;
	}
}
