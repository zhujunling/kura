package org.eclipse.kura.core.data.transport.mqtt;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.kura.data.DataTransportListener;
import org.eclipse.kura.data.DataTransportToken;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DataTransportListenerS implements ServiceListener, DataTransportListener {
	
	private static final Logger s_logger = LoggerFactory.getLogger(DataTransportListenerS.class);
	
	private BundleContext m_ctx;
	private Map<ServiceReference<DataTransportListener>, DataTransportListener> m_serviceReferences;
	private Object m_lock;
	
	public DataTransportListenerS(BundleContext ctx) {
		m_ctx = ctx;
		m_serviceReferences = new HashMap<ServiceReference<DataTransportListener>, DataTransportListener>(); 
		m_lock = new Object();
	}
	
	public void start() {
		try {
			m_ctx.addServiceListener(this, "(objectClass="+DataTransportListener.class.getName()+")");
		} catch (InvalidSyntaxException e) {
			// should never happen
			s_logger.error("Failed to addServiceListener", e);
		}
		// get the initial service references
		Collection<ServiceReference<DataTransportListener>> srS = null;
		try {
			srS = m_ctx.getServiceReferences(DataTransportListener.class, null);
		} catch (InvalidSyntaxException e) {
			// should never happen
			s_logger.error("Failed to get ServiceReferenceS", e);
		}
		if (srS != null) {
			synchronized (m_lock) {
				for (ServiceReference<DataTransportListener> sr : srS) {
					m_serviceReferences.put(sr, m_ctx.getService(sr));
					s_logger.info("Add ServiceReference {}", sr.getProperty("component.name"));
				}
				s_logger.debug("There are {} ServiceReferenceS", m_serviceReferences.size());
			}
		}
	}
	
	public void stop() {
		m_ctx.removeServiceListener(this);
		synchronized (m_lock) {
			for (ServiceReference<DataTransportListener> sr : m_serviceReferences.keySet()) {
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
				m_serviceReferences.put((ServiceReference<DataTransportListener>) sr,
						(DataTransportListener) m_ctx.getService(sr));
				s_logger.debug("There are {} ServiceReferenceS", m_serviceReferences.size());
			}
			break;
		case ServiceEvent.UNREGISTERING:
			s_logger.info("UNREGISTERING ServiceReference {}", sr.getProperty("component.name"));
			synchronized (m_lock) {
				m_serviceReferences.remove(sr);
				s_logger.debug("There are {} ServiceReferenceS", m_serviceReferences.size());
			}
			break;
		default:
			// Ignore
			break;
		}
	}
	
	@Override
	public void onConnectionEstablished(boolean newSession) {
		DataTransportListener[] listeners = getDataTransportListenerS();
		if (listeners != null) {
			for (DataTransportListener listener : listeners) {
				try {
					listener.onConnectionEstablished(newSession);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.info("No registered services. Ignoring onConnectionEstablished");
		}
	}

	@Override
	public void onDisconnecting() {
		DataTransportListener[] listeners = getDataTransportListenerS();
		if (listeners != null) {
			for (DataTransportListener listener : listeners) {
				try {
					listener.onDisconnecting();
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.info("No registered services. Ignoring onDisconnecting");
		}
	}

	@Override
	public void onDisconnected() {
		DataTransportListener[] listeners = getDataTransportListenerS();
		if (listeners != null) {
			for (DataTransportListener listener : listeners) {
				try {
					listener.onDisconnected();
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.info("No registered services. Ignoring onDisconnected");
		}
	}

	@Override
	public void onConfigurationUpdating(boolean wasConnected) {
		DataTransportListener[] listeners = getDataTransportListenerS();
		if (listeners != null) {
			for (DataTransportListener listener : listeners) {
				try {
					listener.onConfigurationUpdating(wasConnected);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.info("No registered services. Ignoring onConfigurationUpdating");
		}
	}

	@Override
	public void onConfigurationUpdated(boolean wasConnected) {
		DataTransportListener[] listeners = getDataTransportListenerS();
		if (listeners != null) {
			for (DataTransportListener listener : listeners) {
				try {
					listener.onConfigurationUpdated(wasConnected);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.info("No registered services. Ignoring onConfigurationUpdated");
		}
	}

	@Override
	public void onConnectionLost(Throwable cause) {
		DataTransportListener[] listeners = getDataTransportListenerS();
		if (listeners != null) {
			for (DataTransportListener listener : listeners) {
				try {
					listener.onConnectionLost(cause);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.info("No registered services. Ignoring onConnectionLost");
		}
	}

	@Override
	public void onMessageArrived(String topic, byte[] payload, int qos,
			boolean retained) {
		DataTransportListener[] listeners = getDataTransportListenerS();
		if (listeners != null) {
			for (DataTransportListener listener : listeners) {
				try {
					listener.onMessageArrived(topic, payload, qos, retained);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.info("No registered services. Ignoring onMessageArrived");
		}
	}

	@Override
	public void onMessageConfirmed(DataTransportToken token) {
		DataTransportListener[] listeners = getDataTransportListenerS();
		if (listeners != null) {
			for (DataTransportListener listener : listeners) {
				try {
					listener.onMessageConfirmed(token);
				} catch (Throwable t) {
					s_logger.warn("Unexpected Throwable", t);
				}
			}
		} else {
			s_logger.info("No registered services. Ignoring onMessageConfirmed");
		}
	}

	private DataTransportListener[] getDataTransportListenerS() {
		DataTransportListener[] listeners = null;
		synchronized (m_lock) {
			Collection<DataTransportListener> services = m_serviceReferences.values();
			if (services != null) {
				listeners = services.toArray(new DataTransportListener[0]);
			}
		}
		return listeners;
	}
}
